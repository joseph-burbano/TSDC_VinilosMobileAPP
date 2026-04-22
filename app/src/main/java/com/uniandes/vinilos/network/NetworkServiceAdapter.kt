package com.uniandes.vinilos.network

import com.uniandes.vinilos.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkServiceAdapter {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: VinilosApi = retrofit.create(VinilosApi::class.java)
}
