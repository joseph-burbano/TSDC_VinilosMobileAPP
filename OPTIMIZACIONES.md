# Optimizaciones aplicadas — rama `feature/optimizacion`

Documento de los cambios realizados para reforzar **caché**, **programación
asíncrona** y **buenas prácticas de memoria/anti-ANR** sobre la base ya
existente en `develop`.

Cada cambio responde a uno de los criterios de evaluación:

1. **Aplicar micro-optimizaciones** (Lint / SonarQube).
2. **Usar hilos / co-rutinas para evitar ANRs**.
3. **Reducir el consumo de memoria de la aplicación**.
4. *(Perfilado en 3 dispositivos físicos — ver sección final).*

> Resumen rápido: se agregó caché HTTP en disco, se convirtieron flujos derivados
> a `StateFlow` cacheado, se estandarizó la recolección lifecycle-aware en toda
> la UI, se forzó el downsampling de bitmaps en Coil, se memorizaron cálculos
> derivados en composables, y se limpió lo accionable del lint.

---

## 1. Caché HTTP en disco (OkHttp)

**Archivos:** [app/src/main/java/com/uniandes/vinilos/network/NetworkServiceAdapter.kt](app/src/main/java/com/uniandes/vinilos/network/NetworkServiceAdapter.kt), [app/src/main/java/com/uniandes/vinilos/VinilosApplication.kt](app/src/main/java/com/uniandes/vinilos/VinilosApplication.kt) (nuevo), [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml)

### Qué se hizo
- Se montó una `okhttp3.Cache` de **10 MiB** en `context.cacheDir/vinilos_http_cache`.
- **Network interceptor** que reescribe `Cache-Control: max-age=60` en todas las
  respuestas (el backend NestJS no envía ese header).
- **Application interceptor** que en ausencia de red pone la petición en modo
  `only-if-cached` con `max-stale = 7 días` (modo offline).
- Timeouts más completos: `connect/read/write = 15 s`, `callTimeout = 30 s`,
  `retryOnConnectionFailure = true`.
- Se introdujo `VinilosApplication` y se llama `NetworkServiceAdapter.init(this)`
  en `onCreate()` para inyectar el `Context` (necesario para el directorio de
  caché y para detectar conectividad).
- Permiso `ACCESS_NETWORK_STATE` añadido al manifiesto.

### Por qué
- Heroku Free tiene cold-start (~10 s al primer hit). Antes, cada navegación
  entre tabs (Home → Álbumes → Home → Álbumes) volvía a pegarle al servidor.
  Ahora, dentro de la ventana de 60 s, las llamadas idénticas se sirven desde
  disco — **0 latencia, 0 datos móviles**.
- La caché complementa a Room (Room cachea modelos deserializados; OkHttp cachea
  la respuesta HTTP cruda y se aplica también a llamadas `getCollector(id)` que
  no pasan por la lista del DAO).
- En modo avión / sin red, antes la app fallaba con `IOException` aunque hubiera
  visitado la pantalla 1 minuto antes; ahora puede servir contenido cacheado por
  hasta 7 días.

### Criterio cubierto: **#3 (memoria/red), #2 (anti-ANR)**
Menos llamadas de red = menos riesgo de bloqueos, menos uso de batería.

---

## 2. `stateIn(Eagerly)` en flujos derivados con `combine`

**Archivos:**
- [app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumViewModel.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumViewModel.kt)
- [app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistViewModel.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistViewModel.kt)
- [app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorViewModel.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorViewModel.kt)

### Qué se hizo
Los tres ViewModels exponían `visibleX` y `hasMore` como `Flow<...>` resultado
de un `combine(_listFlow, _visibleCount) { ... }`. Cada `collectAsState(...)` en
Compose suscribía el `combine` desde cero y reactivaba los upstream en cada
recomposition. Ahora se exponen como `StateFlow<...>` cacheados en memoria:

```kotlin
val visibleAlbums: StateFlow<List<Album>> =
    combine(_uiState, _visibleCount) { state, count -> ... }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
```

### Por qué
- **Antes:** N collectors → N suscripciones al `combine` → N evaluaciones de la
  lambda en cada cambio de estado.
- **Ahora:** una sola suscripción upstream compartida; las re-emisiones llegan
  cacheadas a todos los observers.
- `Eagerly` garantiza que `.value` siempre tenga un valor real (evita el delay
  típico de `WhileSubscribed`, que confunde tests síncronos y deja `.value` en
  el initial hasta la primera suscripción de la UI).
- Costo: el upstream se mantiene vivo durante toda la vida del `viewModelScope`
  — pero es solo un `combine` de dos `MutableStateFlow` internos (peso ≈ 0).

### Criterio cubierto: **#1 (micro-optimización), #3 (memoria CPU)**

### Tests ajustados
- `ArtistViewModelTest.loadMore incrementa los performers visibles` y su gemelo
  en `CollectorViewModelTest` requirieron `advanceUntilIdle()` después de
  `vm.loadMore()` porque la propagación al `StateFlow` caliente pasa por el
  dispatcher (no es síncrona como con un `Flow` frío).

---

## 3. `collectAsStateWithLifecycle` en todas las pantallas

**Archivos:** todas las pantallas Compose
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt)
- [AlbumListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumListScreen.kt), [AlbumDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumDetailScreen.kt)
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt), [ArtistDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistDetailScreen.kt)
- [CollectorDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorDetailScreen.kt) *(CollectorListScreen ya lo usaba)*

### Qué se hizo
Se reemplazó `collectAsState()` por `collectAsStateWithLifecycle()` en las 6
pantallas que aún usaban la versión no lifecycle-aware. Se añadió la dependencia
`androidx.lifecycle:lifecycle-runtime-compose:2.10.0` al `libs.versions.toml` y
al `app/build.gradle.kts`.

### Por qué
- `collectAsState` mantiene la suscripción mientras el composable esté en la
  composición — incluso cuando la app está en background.
- `collectAsStateWithLifecycle` cancela la recolección cuando el `LifecycleOwner`
  pasa a `STOPPED` (app a background, navegación a otra Activity) y la reanuda
  al volver a `STARTED`. Esto:
  - **Ahorra batería**: no se procesan emisiones que nadie verá.
  - **Anti-ANR**: si el ViewModel emite muchos eventos mientras la app está
    en background, no se acumula trabajo en la cola del Main thread.
  - **Memoria**: no se mantienen referencias activas a callbacks de UI.

### Criterio cubierto: **#2 (anti-ANR), #3 (memoria/batería)**

---

## 4. Coil `ImageRequest` con `scale(Scale.FILL)` y `crossfade(true)`

**Archivos:**
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt) — `AlbumCard`, `ArtistCard`
- [CollectorDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorDetailScreen.kt) — `HeroSection`, `VaultAlbumCard`
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt) — `PerformerGridItem`

### Qué se hizo
Donde se llamaba `AsyncImage(model = url, ...)` se cambió a:

```kotlin
val context = LocalContext.current
AsyncImage(
    model = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .scale(Scale.FILL)
            .build()
    },
    ...
)
```

### Por qué
- Sin un `ImageRequest` explícito con `scale`, Coil decodifica el bitmap al
  tamaño nativo de la imagen del servidor. Para un cover de álbum 2000×2000 px,
  eso son ~16 MB en heap por bitmap (ARGB_8888). El composable solo muestra
  200dp × ancho de pantalla.
- Con `scale = Scale.FILL`, Coil usa `BitmapFactory.Options.inSampleSize` para
  decodificar al tamaño objetivo durante el decode → **bitmaps típicamente
  10–40× más pequeños en memoria**.
- `remember(url)` evita reconstruir el `ImageRequest` en cada recomposition,
  permitiendo a Coil cachear el resultado por equality del request.
- `crossfade(true)` da transición visual suave sin costo perceptible.

### Criterio cubierto: **#3 (memoria, principal)**, **#1 (micro-optimización)**

---

## 5. `remember`/`derivedStateOf` en cálculos derivados de Composables

**Archivos:**
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt) — `lastAlbums`, `consultedArtists`, `recommendedArtists`, `featuredCollectors`
- [CollectorDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorDetailScreen.kt) — `avgGrade`, `ref` en `VaultAlbumCard`
- [CollectorListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorListScreen.kt) — `displayCollectors`, `filteredCount`
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt) — `displayPerformers`, `filteredCount`

### Qué se hizo
Cálculos derivados que antes corrían **en cada recomposition** ahora se
memorizan con `remember(...)` con dependencias explícitas (o `derivedStateOf`
cuando las entradas son `State`):

```kotlin
// Antes - recalcula flatMap+distinctBy+take en cada recomposition
val recommendedArtists = collectors
    .flatMap { it.favoritePerformers }
    .distinctBy { it.id }
    .take(2)

// Despues - solo cuando 'collectors' cambia
val recommendedArtists by remember(collectors) {
    derivedStateOf {
        collectors.flatMap { it.favoritePerformers }
            .distinctBy { it.id }
            .take(2)
    }
}
```

### Por qué
- Compose puede recomponer un screen muchas veces por segundo durante una
  animación (por ejemplo, el slide de la bottom-bar al hacer scroll en detalle).
  Si cada recomposition recalcula `flatMap` o filtra una lista de 100 elementos,
  son ciclos desperdiciados que se acumulan.
- `remember(key)` cachea el resultado y solo lo recalcula cuando la key cambia.
- `derivedStateOf` además evita recomposiciones aguas abajo si el resultado no
  cambia (ej. si filtras una lista y el filtro produce la misma lista).

### Criterio cubierto: **#1 (micro-optimización)**, **#2 (mantener Main thread libre)**

---

## 6. Keys estables en items de listas Lazy

**Archivos:**
- [AlbumListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumListScreen.kt) — items de álbumes y de chips de género
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt) — `itemsIndexed` con `key = { _, p -> p.id }`
- [CollectorListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorListScreen.kt) — items de coleccionistas
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt) — items en los 3 `LazyRow`

### Qué se hizo
Todos los `items(list)` y `itemsIndexed(list)` ahora reciben `key = { it.id }`
(o `key = { it }` para colecciones de strings).

### Por qué
- Sin key, Compose identifica cada item por su posición. Cuando filtras la
  lista (búsqueda, género), Compose cree que cada slot recibió un item nuevo →
  destruye y recrea todos los composables → pierde estado interno
  (animaciones, scroll position, etc.).
- Con `key = { it.id }`, Compose puede reconocer "este item ya estaba en la
  posición 5, ahora está en la 2" → **mueve** el composable existente sin
  recrearlo. Reduce el trabajo de composición y evita re-decode de imágenes
  con Coil.

### Criterio cubierto: **#1 (micro-optimización)**, **#3 (menos pressure de GC por composables recreados)**

---

## 7. Limpieza derivada del lint

**Archivos:** [app/src/main/res/values/colors.xml](app/src/main/res/values/colors.xml), [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml)

### Qué se hizo
Se corrió `./gradlew lint` y se analizó `app/build/reports/lint-results-debug.xml`. De los 21 warnings se actuó sobre los accionables y de bajo riesgo:

- **`UnusedResources` en `colors.xml`** — Se eliminaron los 7 colores legacy del
  template de Android Studio (`purple_200/500/700`, `teal_200/700`, `black`,
  `white`) que no se referencian en ningún lado: la app usa el theme M3
  directamente. Reduce el `R.class` generado y el tamaño del APK (poco, pero
  alineado con el criterio #3).
- **`RedundantLabel` en MainActivity** — Se eliminó `android:label="@string/app_name"`
  del `<activity>` del manifiesto: ya está declarado en `<application>` y se hereda.
  Una línea menos en el manifest, mismo comportamiento.

### Lo que NO se tocó (justificado)
- **Versiones de dependencias obsoletas** (AGP 9.1.1 → 9.2.0, Compose BOM, Room,
  Kotlin, Mockk, OkHttp): cambios amplios con riesgo de regresión, fuera del
  alcance de "optimización".
- **`Aligned16KB` en `libmockkjvmtiagent.so`**: viene de `mockk-agent-android`,
  solo se usa en `androidTest`. No afecta al APK release ni a la performance del
  usuario.
- **`UnusedResources` en `ic_launcher_background.xml` / `ic_launcher_foreground.xml`**:
  el linter marca falso-positivo. El adaptive icon en
  `mipmap-anydpi-v26/ic_launcher.xml` usa el `@color/ic_launcher_background` y el
  `@mipmap/ic_launcher_foreground` (.webp), pero los XML drawables sirven como
  fallback editable desde Android Studio. Borrarlos rompería el wizard de íconos.

### Criterio cubierto: **#1 (micro-optimización)**

---

## Resumen del impacto

| Capa | Antes | Después |
|------|-------|---------|
| Red | Sin caché HTTP, cada navegación pega a Heroku | Disk cache 10 MiB + offline fallback |
| ViewModels | `combine` frío, suscripción por collector | `StateFlow` caliente compartido, valor cacheado |
| Compose collection | `collectAsState` (siempre activo) | `collectAsStateWithLifecycle` (pausa en STOPPED) |
| Imágenes | Bitmap a tamaño nativo | Downsampled al tamaño del composable |
| Cálculos en composables | Recalculados cada recomposition | Memorizados con `remember`/`derivedStateOf` |
| Items de listas | Identidad por posición | `key = { it.id }` estable |
| Recursos / manifest | 7 colores legacy y label redundante | Limpios |

---

## Perfilado en dispositivos físicos (criterio #4)

**Script:** [scripts/profile-device.ps1](scripts/profile-device.ps1)

### Cómo correrlo
1. Conecta un teléfono Android por USB con depuración activada (`adb devices`
   debe listarlo).
2. Desde la raíz del repo, en PowerShell:
   ```powershell
   .\scripts\profile-device.ps1 -DeviceLabel "samsung-a52"
   ```
   El `DeviceLabel` es libre (úsalo para distinguir los reportes — ej.
   `samsung-a52`, `pixel-7`, `xiaomi-redmi`).
3. El script:
   - Reinstala el APK debug (`./gradlew installDebug`).
   - Resetea contadores de batería y GPU.
   - Lanza la app y te pide hacer un recorrido manual por las HUs principales
     (Home → Álbumes → detalle → Artistas → detalle → Coleccionistas → detalle,
     con un pull-to-refresh).
   - Cuando presionas Enter, captura `dumpsys meminfo`, `dumpsys gfxinfo`,
     `dumpsys batterystats --charged` y `top` filtrado al PID de la app.
4. El reporte queda en `profile-results/<DeviceLabel>-<timestamp>.txt` (carpeta
   ignorada por git para no contaminar el repo).

### Qué métricas mira el reporte
- **Memoria** (`meminfo`): desglose por categoría — Java heap, Native heap,
  Code, Stack, Graphics, GL mtrack. Útil para validar el efecto del downsampling
  de Coil (criterio #3).
- **GPU / jank** (`gfxinfo`): porcentaje de frames > 16ms (perdidos), > 50ms
  (jank notorio) y > 100ms (frozen). Útil para validar
  `collectAsStateWithLifecycle` y `key={}` en listas.
- **CPU instantáneo** (`top`): %CPU del proceso en el momento del snapshot.
- **Energía / batería** (`batterystats`): tiempo de CPU acumulado por la app,
  wakelocks, estimación de mAh consumidos. Útil para validar el efecto del cache
  HTTP (menos red = menos energía).

### Cómo me lo pasas
Con que me adjuntes los 3 archivos `.txt` generados (uno por dispositivo) basta
— los analizo y te armo una tabla comparativa para meter en la entrega.
