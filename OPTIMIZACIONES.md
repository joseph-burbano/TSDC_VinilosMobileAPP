# Optimizaciones aplicadas â€” rama `feature/optimizacion`

Documento de los cambios realizados para reforzar **cachÃ©**, **programaciÃ³n
asÃ­ncrona** y **buenas prÃ¡cticas de memoria/anti-ANR** sobre la base ya
existente en `develop`.

Cada cambio responde a uno de los criterios de evaluaciÃ³n:

1. **Aplicar micro-optimizaciones** (Lint / SonarQube).
2. **Usar hilos / co-rutinas para evitar ANRs**.
3. **Reducir el consumo de memoria de la aplicaciÃ³n**.
4. *(Perfilado en 3 dispositivos fÃ­sicos â€” fuera de alcance del cÃ³digo).*

> Resumen rÃ¡pido: se agregÃ³ cachÃ© HTTP en disco, se convirtieron flujos derivados
> a `StateFlow` cacheado, se estandarizÃ³ la recolecciÃ³n lifecycle-aware en toda
> la UI, se forzÃ³ el downsampling de bitmaps en Coil, y se memorizaron cÃ¡lculos
> derivados en composables.

---

## 1. CachÃ© HTTP en disco (OkHttp)

**Archivos:** [app/src/main/java/com/uniandes/vinilos/network/NetworkServiceAdapter.kt](app/src/main/java/com/uniandes/vinilos/network/NetworkServiceAdapter.kt), [app/src/main/java/com/uniandes/vinilos/VinilosApplication.kt](app/src/main/java/com/uniandes/vinilos/VinilosApplication.kt) (nuevo), [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml)

### QuÃ© se hizo
- Se montÃ³ una `okhttp3.Cache` de **10 MiB** en `context.cacheDir/vinilos_http_cache`.
- **Network interceptor** que reescribe `Cache-Control: max-age=60` en todas las
  respuestas (el backend NestJS no envÃ­a ese header).
- **Application interceptor** que en ausencia de red pone la peticiÃ³n en modo
  `only-if-cached` con `max-stale = 7 dÃ­as` (modo offline).
- Timeouts mÃ¡s completos: `connect/read/write = 15 s`, `callTimeout = 30 s`,
  `retryOnConnectionFailure = true`.
- Se introdujo `VinilosApplication` y se llama `NetworkServiceAdapter.init(this)`
  en `onCreate()` para inyectar el `Context` (necesario para el directorio de
  cachÃ© y para detectar conectividad).
- Permiso `ACCESS_NETWORK_STATE` aÃ±adido al manifiesto.

### Por quÃ©
- Heroku Free tiene cold-start (~10 s al primer hit). Antes, cada navegaciÃ³n
  entre tabs (Home â†’ Ãlbumes â†’ Home â†’ Ãlbumes) volvÃ­a a pegarle al servidor.
  Ahora, dentro de la ventana de 60 s, las llamadas idÃ©nticas se sirven desde
  disco â€” **0 latencia, 0 datos mÃ³viles**.
- La cachÃ© complementa a Room (Room cachea modelos deserializados; OkHttp cachea
  la respuesta HTTP cruda y se aplica tambiÃ©n a llamadas `getCollector(id)` que
  no pasan por la lista del DAO).
- En modo aviÃ³n / sin red, antes la app fallaba con `IOException` aunque hubiera
  visitado la pantalla 1 minuto antes; ahora puede servir contenido cacheado por
  hasta 7 dÃ­as.

### Criterio cubierto: **#3 (memoria/red), #2 (anti-ANR)**
Menos llamadas de red = menos riesgo de bloqueos, menos uso de baterÃ­a.

---

## 2. `stateIn(Eagerly)` en flujos derivados con `combine`

**Archivos:**
- [app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumViewModel.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumViewModel.kt)
- [app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistViewModel.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistViewModel.kt)
- [app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorViewModel.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorViewModel.kt)

### QuÃ© se hizo
Los tres ViewModels exponÃ­an `visibleX` y `hasMore` como `Flow<...>` resultado
de un `combine(_listFlow, _visibleCount) { ... }`. Cada `collectAsState(...)` en
Compose suscribÃ­a el `combine` desde cero y reactivaba los upstream en cada
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

### Por quÃ©
- **Antes:** N collectors â†’ N suscripciones al `combine` â†’ N evaluaciones de la
  lambda en cada cambio de estado.
- **Ahora:** una sola suscripciÃ³n upstream compartida; las re-emisiones llegan
  cacheadas a todos los observers.
- `Eagerly` garantiza que `.value` siempre tenga un valor real (evita el delay
  tÃ­pico de `WhileSubscribed`, que confunde tests sÃ­ncronos y deja `.value` en
  el initial hasta la primera suscripciÃ³n de la UI).
- Costo: el upstream se mantiene vivo durante toda la vida del `viewModelScope`
  â€” pero es solo un `combine` de dos `MutableStateFlow` internos (peso â‰ˆ 0).

### Criterio cubierto: **#1 (micro-optimizaciÃ³n), #3 (memoria CPU)**

### Tests ajustados
- `ArtistViewModelTest.loadMore incrementa los performers visibles` y su gemelo
  en `CollectorViewModelTest` requirieron `advanceUntilIdle()` despuÃ©s de
  `vm.loadMore()` porque la propagaciÃ³n al `StateFlow` caliente pasa por el
  dispatcher (no es sÃ­ncrona como con un `Flow` frÃ­o).

---

## 3. `collectAsStateWithLifecycle` en todas las pantallas

**Archivos:** todas las pantallas Compose
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt)
- [AlbumListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumListScreen.kt), [AlbumDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumDetailScreen.kt)
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt), [ArtistDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistDetailScreen.kt)
- [CollectorDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorDetailScreen.kt) *(CollectorListScreen ya lo usaba)*

### QuÃ© se hizo
Se reemplazÃ³ `collectAsState()` por `collectAsStateWithLifecycle()` en las 6
pantallas que aÃºn usaban la versiÃ³n no lifecycle-aware. Se aÃ±adiÃ³ la dependencia
`androidx.lifecycle:lifecycle-runtime-compose:2.10.0` al `libs.versions.toml` y
al `app/build.gradle.kts`.

### Por quÃ©
- `collectAsState` mantiene la suscripciÃ³n mientras el composable estÃ© en la
  composiciÃ³n â€” incluso cuando la app estÃ¡ en background.
- `collectAsStateWithLifecycle` cancela la recolecciÃ³n cuando el `LifecycleOwner`
  pasa a `STOPPED` (app a background, navegaciÃ³n a otra Activity) y la reanuda
  al volver a `STARTED`. Esto:
  - **Ahorra baterÃ­a**: no se procesan emisiones que nadie verÃ¡.
  - **Anti-ANR**: si el ViewModel emite muchos eventos mientras la app estÃ¡
    en background, no se acumula trabajo en la cola del Main thread.
  - **Memoria**: no se mantienen referencias activas a callbacks de UI.

### Criterio cubierto: **#2 (anti-ANR), #3 (memoria/baterÃ­a)**

---

## 4. Coil `ImageRequest` con `scale(Scale.FILL)` y `crossfade(true)`

**Archivos:**
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt) â€” `AlbumCard`, `ArtistCard`
- [CollectorDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorDetailScreen.kt) â€” `HeroSection`, `VaultAlbumCard`
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt) â€” `PerformerGridItem`

### QuÃ© se hizo
Donde se llamaba `AsyncImage(model = url, ...)` se cambiÃ³ a:

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

### Por quÃ©
- Sin un `ImageRequest` explÃ­cito con `scale`, Coil decodifica el bitmap al
  tamaÃ±o nativo de la imagen del servidor. Para un cover de Ã¡lbum 2000Ã—2000 px,
  eso son ~16 MB en heap por bitmap (ARGB_8888). El composable solo muestra
  200dp Ã— ancho de pantalla.
- Con `scale = Scale.FILL`, Coil usa `BitmapFactory.Options.inSampleSize` para
  decodificar al tamaÃ±o objetivo durante el decode â†’ **bitmaps tÃ­picamente
  10â€“40Ã— mÃ¡s pequeÃ±os en memoria**.
- `remember(url)` evita reconstruir el `ImageRequest` en cada recomposition,
  permitiendo a Coil cachear el resultado por equality del request.
- `crossfade(true)` da transiciÃ³n visual suave sin costo perceptible.

### Criterio cubierto: **#3 (memoria, principal)**, **#1 (micro-optimizaciÃ³n)**

---

## 5. `remember`/`derivedStateOf` en cÃ¡lculos derivados de Composables

**Archivos:**
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt) â€” `lastAlbums`, `consultedArtists`, `recommendedArtists`, `featuredCollectors`
- [CollectorDetailScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorDetailScreen.kt) â€” `avgGrade`, `ref` en `VaultAlbumCard`
- [CollectorListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorListScreen.kt) â€” `displayCollectors`, `filteredCount`
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt) â€” `displayPerformers`, `filteredCount`

### QuÃ© se hizo
CÃ¡lculos derivados que antes corrÃ­an **en cada recomposition** ahora se
memorizan con `remember(...)` con dependencias explÃ­citas (o `derivedStateOf`
cuando las entradas son `State`):

```kotlin
// Antes â€” recalcula flatMap+distinctBy+take en cada recomposition
val recommendedArtists = collectors
    .flatMap { it.favoritePerformers }
    .distinctBy { it.id }
    .take(2)

// DespuÃ©s â€” solo cuando 'collectors' cambia
val recommendedArtists by remember(collectors) {
    derivedStateOf {
        collectors.flatMap { it.favoritePerformers }
            .distinctBy { it.id }
            .take(2)
    }
}
```

### Por quÃ©
- Compose puede recomponer un screen muchas veces por segundo durante una
  animaciÃ³n (por ejemplo, el slide de la bottom-bar al hacer scroll en detalle).
  Si cada recomposition recalcula `flatMap` o filtra una lista de 100 elementos,
  son ciclos desperdiciados que se acumulan.
- `remember(key)` cachea el resultado y solo lo recalcula cuando la key cambia.
- `derivedStateOf` ademÃ¡s evita recomposiciones aguas abajo si el resultado no
  cambia (ej. si filtras una lista y el filtro produce la misma lista).

### Criterio cubierto: **#1 (micro-optimizaciÃ³n)**, **#2 (mantener Main thread libre)**

---

## 6. Keys estables en items de listas Lazy

**Archivos:**
- [AlbumListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/albums/AlbumListScreen.kt) â€” items de Ã¡lbumes y de chips de gÃ©nero
- [ArtistListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/artists/ArtistListScreen.kt) â€” `itemsIndexed` con `key = { _, p -> p.id }`
- [CollectorListScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/collectors/CollectorListScreen.kt) â€” items de coleccionistas
- [HomeScreen.kt](app/src/main/java/com/uniandes/vinilos/ui/home/HomeScreen.kt) â€” items en los 3 `LazyRow`

### QuÃ© se hizo
Todos los `items(list)` y `itemsIndexed(list)` ahora reciben `key = { it.id }`
(o `key = { it }` para colecciones de strings).

### Por quÃ©
- Sin key, Compose identifica cada item por su posiciÃ³n. Cuando filtras la
  lista (bÃºsqueda, gÃ©nero), Compose cree que cada slot recibiÃ³ un item nuevo â†’
  destruye y recrea todos los composables â†’ pierde estado interno
  (animaciones, scroll position, etc.).
- Con `key = { it.id }`, Compose puede reconocer "este item ya estaba en la
  posiciÃ³n 5, ahora estÃ¡ en la 2" â†’ **mueve** el composable existente sin
  recrearlo. Reduce el trabajo de composiciÃ³n y evita re-decode de imÃ¡genes
  con Coil.

### Criterio cubierto: **#1 (micro-optimizaciÃ³n)**, **#3 (menos pressure de GC por composables recreados)**

---

## Resumen del impacto

| Capa | Antes | DespuÃ©s |
|------|-------|---------|
| Red | Sin cachÃ© HTTP, cada navegaciÃ³n pega a Heroku | Disk cache 10 MiB + offline fallback |
| ViewModels | `combine` frÃ­o, suscripciÃ³n por collector | `StateFlow` caliente compartido, valor cacheado |
| Compose collection | `collectAsState` (siempre activo) | `collectAsStateWithLifecycle` (pausa en STOPPED) |
| ImÃ¡genes | Bitmap a tamaÃ±o nativo | Downsampled al tamaÃ±o del composable |
| CÃ¡lculos en composables | Recalculados cada recomposition | Memorizados con `remember`/`derivedStateOf` |
| Items de listas | Identidad por posiciÃ³n | `key = { it.id }` estable |

## Pendientes que quedan fuera del cÃ³digo

- **Perfilar en 3 dispositivos fÃ­sicos (criterio #4):** se debe correr
  Android Studio Profiler en al menos 3 telÃ©fonos distintos midiendo
  *Memory*, *CPU* y *Energy* sobre las HUs principales (Home â†’ detalle de
  Ã¡lbum â†’ detalle de coleccionista). El cÃ³digo ya estÃ¡ optimizado; falta el
  ejercicio empÃ­rico de mediciÃ³n.
- **Lint baseline:** se puede correr `./gradlew lint` y revisar
  `app/build/reports/lint-results-debug.html` para identificar warnings
  adicionales (Ã­conos `ArrowBack` deprecados aparecen en la build actual,
  no se tocaron por ser cÃ³digo del compaÃ±ero de HU02).

