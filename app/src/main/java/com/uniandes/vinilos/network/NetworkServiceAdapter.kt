package com.uniandes.vinilos.network

import android.content.Context
import com.uniandes.vinilos.util.Constants
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object NetworkServiceAdapter {

    private const val HTTP_CACHE_DIR = "vinilos_http_cache"
    private const val HTTP_CACHE_SIZE_BYTES = 10L * 1024L * 1024L // 10 MiB
    private const val MAX_AGE_SECONDS = 60                       // respuestas frescas 60s
    private const val MAX_STALE_SECONDS = 60 * 60 * 24 * 7       // tolerable hasta 7 días sin red

    @Volatile private var initialized = false
    private lateinit var httpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    /**
     * Inicializa el adaptador con el contexto de la aplicación para poder montar
     * la caché HTTP en disco. Es idempotente y seguro de llamar múltiples veces.
     */
    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            httpClient = buildClient(context.applicationContext)
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            initialized = true
        }
    }

    val api: VinilosApi
        get() {
            check(initialized) {
                "NetworkServiceAdapter no inicializado. Llama a init(context) en Application.onCreate()."
            }
            return retrofit.create(VinilosApi::class.java)
        }

    private fun buildClient(context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, HTTP_CACHE_DIR)
        val cache = Cache(cacheDir, HTTP_CACHE_SIZE_BYTES)

        // El backend (NestJS) no envía Cache-Control. Lo forzamos del lado del cliente:
        // - Si hay red: respuestas válidas durante MAX_AGE_SECONDS (evita golpear Heroku
        //   en cada navegación entre pestañas dentro del minuto siguiente).
        // - Si no hay red: aceptamos cualquier respuesta cacheada hasta MAX_STALE_SECONDS.
        val rewriteCacheHeaders = okhttp3.Interceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header(
                    "Cache-Control",
                    CacheControl.Builder()
                        .maxAge(MAX_AGE_SECONDS, TimeUnit.SECONDS)
                        .build()
                        .toString()
                )
                .removeHeader("Pragma")
                .build()
        }
        val offlineFallback = okhttp3.Interceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(context)) {
                request = request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(MAX_STALE_SECONDS, TimeUnit.SECONDS)
                            .build()
                    )
                    .build()
            }
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineFallback)
            .addNetworkInterceptor(rewriteCacheHeaders)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun isNetworkAvailable(context: Context): Boolean = try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as android.net.ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (_: Exception) {
        false
    }
}
