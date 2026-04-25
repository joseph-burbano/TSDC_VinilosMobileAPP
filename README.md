# Vinilos — Mobile App

<<<<<<< HEAD
**Ingeniería de Software para Aplicaciones Móviles** — Universidad de los Andes (MISO).
=======
**Ingeniería de Software para Aplicaciones Móviles** — Universidad de los Andes (MISO).  
>>>>>>> develop
The Software Design Company.

Aplicación Android para navegar y gestionar un catálogo de álbumes en vinilo, artistas y coleccionistas. Este README cubre **cómo correr la app y los tres niveles de pruebas** (unit, instrumentación, E2E).

---

## Tabla de contenido

1. [Stack](#stack)
<<<<<<< HEAD
2. [Prerrequisitos](#prerrequisitos)
3. [Levantar el backend](#levantar-el-backend)
4. [Correr la app](#correr-la-app)
5. [Pruebas unitarias (JVM)](#pruebas-unitarias-jvm)
6. [Pruebas instrumentadas (Compose / Espresso)](#pruebas-instrumentadas-compose--espresso)
7. [Pruebas E2E con Kraken](#pruebas-e2e-con-kraken)
8. [Estructura del repo](#estructura-del-repo)
9. [Solución de problemas](#solución-de-problemas)
=======
2. [Arquitectura](#arquitectura)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Prerrequisitos](#prerrequisitos)
5. [Levantar el backend](#levantar-el-backend)
6. [Correr la app](#correr-la-app)
7. [Pruebas unitarias (JVM)](#pruebas-unitarias-jvm)
8. [Pruebas instrumentadas (Compose / Espresso)](#pruebas-instrumentadas-compose--espresso)
9. [Pruebas E2E con Kraken](#pruebas-e2e-con-kraken)
10. [Notas para Windows](#notas-para-windows)
11. [Solución de problemas](#solución-de-problemas)

---

## Stack

| Capa         | Herramienta                             | Versión              |
| ------------ | --------------------------------------- | -------------------- |
| Lenguaje     | Kotlin                                  | 2.2.10               |
| Build        | AGP / Gradle wrapper                    | 9.1.1                |
| UI           | Jetpack Compose (BOM)                   | 2026.02.01           |
| Navegación   | Navigation Compose                      | 2.8.0                |
| Red          | Retrofit + Gson + OkHttp Logging        | 3.0.0 / 4.12.0       |
| Persistencia | Room + KSP                              | 2.7.0 / 2.2.10-2.0.2 |
| Imágenes     | Coil                                    | 2.7.0                |
| Async        | Coroutines                              | 1.10.2               |
| Mocks JVM    | MockK                                   | 1.13.17              |
| Tests HTTP   | MockWebServer                           | 5.3.2                |
| Tests E2E    | Kraken-Node + Appium 2.x + UIAutomator2 | 1.0.24 / 2.11.5+     |
>>>>>>> develop

---

## Stack

<<<<<<< HEAD
| Capa | Herramienta | Versión |
| ---- | ----------- | ------- |
| Lenguaje | Kotlin | 2.2.10 |
| Build | AGP / Gradle wrapper | 9.1.1 |
| UI | Jetpack Compose (BOM) | 2026.02.01 |
| Navegación | Navigation Compose | 2.8.0 |
| Red | Retrofit + Gson + OkHttp Logging | 3.0.0 / 4.12.0 |
| Persistencia | Room + KSP | 2.7.0 / 2.2.10-2.0.2 |
| Imágenes | Coil | 2.7.0 |
| Async | Coroutines | 1.10.2 |
| Mocks JVM | MockK | 1.13.17 |
| Tests HTTP | MockWebServer | 5.3.2 |
| Tests E2E | Kraken-Node + Appium 2.x + UIAutomator2 | 1.0.24 / 2.11.5+ |
=======
El proyecto sigue el patrón **MVVM (Model - View - ViewModel)** con organización **por feature**.

Para más detalles sobre la arquitectura MVVM en Android, consultar (usar traductor ya que la fuente está en francés):  
[Comprendre l'architecture MVVM sur Android](https://medium.com/androidmood/comprendre-larchitecture-mvvm-sur-android-aa285e4fe9dd)

### Diagrama de capas

```
┌─────────────────────────────────────┐
│              VIEW                   │
│   ui/[feature]/[Feature]Screen.kt   │
│   (Jetpack Compose)                 │
└────────────────┬────────────────────┘
                 │ observa
┌────────────────▼────────────────────┐
│           VIEWMODEL                 │
│   ui/[feature]/[Feature]ViewModel   │
│   (StateFlow)                       │
└────────────────┬────────────────────┘
                 │ solicita datos
┌────────────────▼────────────────────┐
│           REPOSITORY                │
│   repository/[Feature]Repository    │
│   cache-first: Room → API REST      │
└──────────┬─────────────┬────────────┘
           │             │
┌──────────▼───┐   ┌─────▼──────────┐
│   ROOM DB    │   │   RETROFIT     │
│  database/   │   │   network/     │
│  DAO + Entity│   │   VinilosApi   │
└──────────────┘   └────────────────┘
```
>>>>>>> develop

---

## Prerrequisitos

<<<<<<< HEAD
| Herramienta | Versión | Para qué |
| ----------- | ------- | -------- |
| JDK | 17 | Compilar la app |
| Android Studio | Otter 3 Feature Drop (2025.2.3) o superior | Recomendado |
| Android SDK + ADB | platform-tools | Instalar APK / `adb` |
| Emulador o device | API 24+ (Android 7+) | Correr la app |
| Docker + Docker Compose | cualquiera | Backend NestJS |
| Node.js | ≥ 12 | Solo para Kraken |
| Appium | 2.11.5 o 3.x | Solo para Kraken (`npm install -g appium`) |

> **JDK 17 obligatorio**. CI lo pinea a Temurin 17. El wrapper `./gradlew` se incluye — usa siempre ese, no un Gradle de sistema.

---

## Levantar el backend

El backend NestJS + Postgres vive en [back/BackVynils/](back/BackVynils/) (es un repo vendoreado).
=======
```
TSDC_VinilosMobileAPP/
├── app/
│   ├── build.gradle.kts                # deps + plugin KSP + Room schema
│   └── src/
│       ├── main/java/com/uniandes/vinilos/
│       │   ├── MainActivity.kt
│       │   ├── database/               # Room: entities, DAOs, mappers
│       │   ├── model/                  # data classes de dominio
│       │   ├── network/                # Retrofit (VinilosApi, NetworkServiceAdapter)
│       │   ├── repository/             # cache-first repositories
│       │   ├── ui/{albums,artists,collectors,home,navigation,theme}
│       │   └── util/                   # Constants, FakeData
│       ├── test/                       # JVM unit tests (MockK + MockWebServer)
│       └── androidTest/                # Compose / Espresso tests
├── back/BackVynils/                    # backend NestJS + Postgres (vendoreado)
├── kraken/                             # tests E2E (Node + Appium)
├── gradle/libs.versions.toml           # catálogo único de versiones
├── build.gradle.kts                    # root: hook de adb reverse
├── settings.gradle.kts
└── gradle.properties                   # android.disallowKotlinSourceSets=false (KSP + AGP 9)
```

### ¿Por qué organización por feature?

En lugar de agrupar todos los `Screen.kt` juntos y todos los `ViewModel.kt` juntos, cada feature tiene su propia carpeta con todo lo que necesita. Esto permite navegar el proyecto de forma más intuitiva, modificar una feature sin tocar otras carpetas, y escalar el equipo asignando features a personas distintas.

---

## Prerrequisitos

| Herramienta             | Versión                                    | Para qué                                   |
| ----------------------- | ------------------------------------------ | ------------------------------------------ |
| JDK                     | 17                                         | Compilar la app                            |
| Android Studio          | Otter 3 Feature Drop (2025.2.3) o superior | Recomendado                                |
| Android SDK + ADB       | platform-tools                             | Instalar APK / `adb`                       |
| Emulador o device       | API 24+ (Android 7+)                       | Correr la app                              |
| Docker + Docker Compose | cualquiera                                 | Backend NestJS                             |
| Node.js                 | ≥ 12                                       | Solo para Kraken                           |
| Appium                  | 2.11.5 o 3.x                               | Solo para Kraken (`npm install -g appium`) |

> **JDK 17 obligatorio**. CI lo pinea a Temurin 17. El wrapper `./gradlew` se incluye — usa siempre ese, no un Gradle de sistema.

---

## Levantar el backend

El backend NestJS + Postgres vive en `back/BackVynils/` (es un repo vendoreado).
>>>>>>> develop

```bash
cd back/BackVynils
docker-compose up -d            # arranca Postgres + NestJS en background
docker-compose logs -f web      # opcional: ver los logs
docker-compose ps               # estado
docker-compose down             # detener
docker-compose down -v          # detener y borrar volumen (datos limpios)
```

Comprobar que responde desde tu PC:

```bash
curl http://localhost:3000/albums
curl http://localhost:3000/albums/1
```

**Configuración de red según donde corras la app:**

<<<<<<< HEAD
| Entorno | URL que ve la app | Configuración |
| ------- | ----------------- | ------------- |
| Emulador Android Studio (default) | `http://10.0.2.2:3000/` | Funciona automáticamente — `10.0.2.2` es el alias QEMU al host |
| Dispositivo físico (USB) | `http://localhost:3000/` | Requiere `adb reverse tcp:3000 tcp:3000` (ya se aplica solo en `installDebug`, ver hook abajo) |
| Dispositivo físico en LAN | `http://<IP-LAN-de-tu-PC>:3000/` | Edita `Constants.BASE_URL` localmente (no commitear) |

La URL base vive en [app/src/main/java/com/uniandes/vinilos/util/Constants.kt](app/src/main/java/com/uniandes/vinilos/util/Constants.kt).
=======
| Entorno                           | URL que ve la app                | Configuración                                                               |
| --------------------------------- | -------------------------------- | --------------------------------------------------------------------------- |
| Emulador Android Studio (default) | `http://10.0.2.2:3000/`          | Funciona automáticamente — `10.0.2.2` es el alias QEMU al host              |
| Dispositivo físico (USB)          | `http://localhost:3000/`         | Requiere `adb reverse tcp:3000 tcp:3000` (se aplica solo en `installDebug`) |
| Dispositivo físico en LAN         | `http://<IP-LAN-de-tu-PC>:3000/` | Edita `Constants.BASE_URL` localmente (no commitear)                        |

La URL base vive en `app/src/main/java/com/uniandes/vinilos/util/Constants.kt`.
>>>>>>> develop

---

## Correr la app

Con backend arriba y emulador o device conectado:

```bash
./gradlew installDebug             # instala el debug APK
./gradlew assembleDebug            # solo compila APK -> app/build/outputs/apk/debug/
```

<<<<<<< HEAD
O **Run** desde Android Studio (dispara `installDebug`). Cada `installDebug` además ejecuta automáticamente:

```
adb reverse tcp:3000 tcp:3000
```

vía el hook configurado en [build.gradle.kts](build.gradle.kts) — útil cuando trabajas en device físico, inocuo en emulador.
=======
O **Run** desde Android Studio (dispara `installDebug`). Cada `installDebug` ejecuta automáticamente `adb reverse tcp:3000 tcp:3000` vía el hook configurado en `build.gradle.kts` — útil en device físico, inocuo en emulador.
>>>>>>> develop

---

## Pruebas unitarias (JVM)

<<<<<<< HEAD
Ubicación: [app/src/test/java/com/uniandes/vinilos/](app/src/test/java/com/uniandes/vinilos/).
=======
Ubicación: `app/src/test/java/com/uniandes/vinilos/`.

>>>>>>> develop
**No requieren** emulador, device, ni backend. Tardan < 5 segundos.

### Qué cubren

<<<<<<< HEAD
| Suite | Archivo | Casos | Qué valida |
| ----- | ------- | ----- | ---------- |
| AlbumRepositoryTest | `repository/AlbumRepositoryTest.kt` | 8 | Patrón cache-first (DAO → API), mapeo DTO → modelo, `releaseDate` ISO truncado al año, renombrado `performers` → `artists`, `refreshAlbums()` borra y refetchea |
| AlbumViewModelTest | `ui/albums/AlbumViewModelTest.kt` | 9 | Estados Loading/Success/Error, `IOException` → "Sin conexión...", `HttpException` → código HTTP, refresh sin parpadeo, `findById` |
| VinilosApiContractTest | `network/VinilosApiContractTest.kt` | 3 | Contrato HTTP del endpoint `GET /albums` con MockWebServer (path, deserialización, tolerancia a nulls) |
| ExampleUnitTest | `ExampleUnitTest.kt` | 1 | Smoke del runner |

**Total: 21 tests JVM, 0 fallos.**
=======
| Suite                  | Archivo                              | Casos | Qué valida                                                                                   |
| ---------------------- | ------------------------------------ | ----- | -------------------------------------------------------------------------------------------- |
| ArtistRepositoryTest   | `repository/ArtistRepositoryTest.kt` | 4     | Cache-first (DAO → API), combina músicos y bandas, refresh borra y refetchea                 |
| AlbumRepositoryTest    | `repository/AlbumRepositoryTest.kt`  | 8     | Patrón cache-first, mapeo DTO → modelo, `releaseDate` ISO truncado al año, `refreshAlbums()` |
| AlbumViewModelTest     | `ui/albums/AlbumViewModelTest.kt`    | 9     | Estados Loading/Success/Error, `IOException`, `HttpException`, refresh, `findById`           |
| VinilosApiContractTest | `network/VinilosApiContractTest.kt`  | 3     | Contrato HTTP `GET /albums` con MockWebServer                                                |
| ExampleUnitTest        | `ExampleUnitTest.kt`                 | 1     | Smoke del runner                                                                             |
>>>>>>> develop

### Cómo correrlas

```bash
<<<<<<< HEAD
./gradlew testDebugUnitTest                                        # todos
./gradlew testDebugUnitTest --tests "*AlbumRepositoryTest"         # una clase
./gradlew testDebugUnitTest --tests "*AlbumViewModelTest.*refresh*"  # un método (glob)
./gradlew testDebugUnitTest --rerun-tasks                          # forzar re-ejecución
=======
./gradlew testDebugUnitTest                                          # todos
./gradlew testDebugUnitTest --tests "*ArtistRepositoryTest"          # una clase
./gradlew testDebugUnitTest --tests "*AlbumViewModelTest.*refresh*"  # un método (glob)
./gradlew testDebugUnitTest --rerun-tasks                            # forzar re-ejecución
>>>>>>> develop
```

### Dónde ver los resultados

```
<<<<<<< HEAD
app/build/reports/tests/testDebugUnitTest/index.html      # HTML
app/build/test-results/testDebugUnitTest/*.xml             # JUnit XML para CI
```

=======
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/*.xml
```

### Tecnologías de testing

| Librería        | Versión | Uso                         |
| --------------- | ------- | --------------------------- |
| JUnit 4         | 4.13.2  | Framework base              |
| MockK           | 1.13.17 | Mocking en Kotlin           |
| Coroutines Test | 1.10.2  | Soporte para `runTest`      |
| MockWebServer   | 5.3.2   | Simulación de servidor HTTP |

>>>>>>> develop
---

## Pruebas instrumentadas (Compose / Espresso)

<<<<<<< HEAD
Ubicación: [app/src/androidTest/java/com/uniandes/vinilos/](app/src/androidTest/java/com/uniandes/vinilos/).
=======
Ubicación: `app/src/androidTest/java/com/uniandes/vinilos/`.

>>>>>>> develop
**Requieren** emulador o device conectado. **No** requieren backend (la API se mockea con MockK).

### Qué cubren

<<<<<<< HEAD
| HU | Suite | Archivo | Casos | Qué valida |
| -- | ----- | ------- | ----- | ---------- |
| HU01 | AlbumListScreenInstrumentedTest | `ui/albums/AlbumListScreenInstrumentedTest.kt` | 5 | Skeleton durante carga, render del listado con sus nombres, mensaje de error con texto correcto, botón "Reintentar" dispara nuevo fetch, estado vacío |
| HU02 | AlbumDetailScreenInstrumentedTest | `ui/albums/AlbumDetailScreenInstrumentedTest.kt` | 5 | Spinner mientras carga, render completo del detalle (título, género, año, sello, secciones ARTISTAS y CANCIONES), botón Volver invoca `onBack`, mensaje de error sin red, "Álbum no encontrado" cuando el id no existe |

Estos tests usan `createComposeRule()` y construyen el `AlbumViewModel` con un `AlbumRepository` mockeado vía MockK (mismo patrón que `ArtistListScreenTest` del compañero). Identifican nodos con los `testTag` definidos en `AlbumListTestTags` y `AlbumDetailTestTags`.
=======
| Suite                           | Casos | Qué valida                                                                                     |
| ------------------------------- | ----- | ---------------------------------------------------------------------------------------------- |
| ArtistListScreenTest            | 4     | Spinner durante carga, nombres de artistas, grilla con testTags, mensaje de error              |
| AlbumListScreenInstrumentedTest | 5     | Skeleton durante carga, render del listado, mensaje de error, botón "Reintentar", estado vacío |
>>>>>>> develop

### Cómo correrlas

```bash
adb devices                                                # verificar device disponible
<<<<<<< HEAD

./gradlew connectedDebugAndroidTest                        # full suite

./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.uniandes.vinilos.ui.albums.AlbumListScreenInstrumentedTest

=======
./gradlew connectedDebugAndroidTest                        # full suite
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.uniandes.vinilos.ui.albums.AlbumListScreenInstrumentedTest
>>>>>>> develop
./gradlew assembleDebugAndroidTest                         # solo compilar APK de tests, sin ejecutar
```

### Dónde ver los resultados

```
app/build/reports/androidTests/connected/debug/index.html
app/build/outputs/androidTest-results/connected/debug/*.xml
```

---

## Pruebas E2E con Kraken

<<<<<<< HEAD
Ubicación: [kraken/](kraken/). Ver detalles técnicos en [kraken/KRAKEN.md](kraken/KRAKEN.md).
Pruebas de **caja negra** sobre el APK real, dirigidas con Cucumber + Kraken-Node + Appium 2.x + UIAutomator2.
**Requieren**: device conectado, Appium corriendo, backend levantado.

> **Importante:** mantén la pantalla del device activa durante la ejecución.
=======
Ubicación: `kraken/`. Ver detalles técnicos en `kraken/KRAKEN.md`.

Pruebas de **caja negra** sobre el APK real, dirigidas con Cucumber + Kraken-Node + Appium 2.x + UIAutomator2.

**Requieren**: device conectado, Appium corriendo, backend levantado.

> **Importante:** mantén la pantalla del device activa durante la ejecución.  
>>>>>>> develop
> Activa **Ajustes → Opciones de desarrollador → Mantener pantalla activa**.

### Qué cubren

<<<<<<< HEAD
| HU | Feature | Archivo | Qué valida |
| -- | ------- | ------- | ---------- |
| HU01 | Listado de álbumes | `kraken/features/album_list.feature` | Navegar al catálogo, ver el header ("SELECCIÓN DE", "Álbumes", "encontrados") y confirmar que un álbum del catálogo aparece en la lista |
| HU02 | Detalle de álbum | `kraken/features/album_detail.feature` | Tap en un álbum desde el catálogo, verificar las secciones del detalle (género "Salsa", "ARTISTAS", "CANCIONES") y volver con el botón "Volver" |
| HU03 | Listado de artistas | `kraken/features/artist_list.feature` | Listado de artistas (HU03 del compañero) |
| — | Navegación principal | `kraken/features/navbar.feature` | Las 4 tabs son visibles y navegables |
=======
| Feature                    | Archivo                               | Qué valida                                                    |
| -------------------------- | ------------------------------------- | ------------------------------------------------------------- |
| Listado de álbumes (HU01)  | `kraken/features/album_list.feature`  | Navegar al catálogo, ver el header, abrir detalle de un álbum |
| Listado de artistas (HU03) | `kraken/features/artist_list.feature` | Navega al listado de artistas y verifica su contenido         |
| Navegación principal       | `kraken/features/navbar.feature`      | Las 4 tabs son visibles y navegables                          |
>>>>>>> develop

### Setup inicial (una sola vez por máquina)

```bash
cd kraken
chmod +x setup.sh run.sh
./setup.sh
```

`setup.sh` instala dependencias Node, parchea `kraken-node` 1.0.24 para Appium 2.x e instala el server APK de UIAutomator2 en el device.

### Correr los tests

```bash
# 1. Backend arriba (en otra terminal)
cd back/BackVynils && docker-compose up -d

# 2. APK de la app compilado
./gradlew assembleDebug

<<<<<<< HEAD
# 3. Appium corriendo (en otra terminal)
appium

# 4. Ejecutar
=======
# 3. Ejecutar
>>>>>>> develop
cd kraken
./run.sh
```

`run.sh` aplica `adb reverse tcp:3000 tcp:3000` automáticamente y lanza la suite.

### Dónde ver los resultados

```
kraken/reports/<timestamp>/index.html
```

El reporte HTML incluye cada step ejecutado y screenshots automáticos.

---

<<<<<<< HEAD
## Estructura del repo

```
TSDC_VinilosMobileAPP/
├── app/                                # módulo Android (lo que se compila)
│   ├── build.gradle.kts                # deps + plugin KSP + Room schema
│   └── src/
│       ├── main/java/com/uniandes/vinilos/
│       │   ├── MainActivity.kt
│       │   ├── database/               # Room: entities, DAOs, mappers, converters
│       │   ├── model/                  # data classes de dominio
│       │   ├── network/                # Retrofit (VinilosApi, NetworkServiceAdapter, dto/)
│       │   ├── repository/             # AlbumRepository (cache-first)
│       │   ├── ui/{albums,artists,collectors,home,navigation,components,theme}
│       │   └── util/Constants.kt       # BASE_URL
│       ├── test/                       # JVM unit tests (MockK + MockWebServer)
│       └── androidTest/                # Compose / Espresso tests
├── back/BackVynils/                    # backend NestJS + Postgres (vendoreado)
├── kraken/                             # tests E2E (Node + Appium)
├── gradle/libs.versions.toml           # catálogo único de versiones
├── build.gradle.kts                    # root: hook de adb reverse
├── settings.gradle.kts
└── gradle.properties                   # android.disallowKotlinSourceSets=false (KSP + AGP 9)
```

---

## Notas para Windows

### Shell

PowerShell **no ejecuta** comandos Unix (`chmod`, `./script.sh`, etc.) ni los scripts de Kraken. Para todo lo de la sección Kraken usa **Git Bash** (viene con Git for Windows) o WSL. Para los comandos `./gradlew` puedes usar PowerShell sin problema.

```powershell
# En PowerShell (Windows) los comandos Gradle van igual:
=======
## Notas para Windows

PowerShell **no ejecuta** comandos Unix (`chmod`, `./script.sh`, etc.) ni los scripts de Kraken. Para todo lo de la sección Kraken usa **Git Bash** (viene con Git for Windows) o WSL. Para los comandos `./gradlew` puedes usar PowerShell sin problema.

```powershell
# En PowerShell los comandos Gradle van igual:
>>>>>>> develop
./gradlew testDebugUnitTest
./gradlew installDebug
```

<<<<<<< HEAD
Para Kraken, abre **Git Bash** (clic derecho en una carpeta → "Git Bash Here"):

```bash
cd kraken
./setup.sh        # solo la primera vez
=======
Para Kraken, abre **Git Bash** (clic derecho en carpeta → "Git Bash Here"):

```bash
cd kraken
./setup.sh   # solo la primera vez
>>>>>>> develop
./run.sh
```

### `adb` no se reconoce / `ANDROID_HOME not exported`

<<<<<<< HEAD
Setea `ANDROID_HOME` y agrega `platform-tools` al PATH del usuario, una sola vez. Desde PowerShell:

```powershell
[Environment]::SetEnvironmentVariable(
  "ANDROID_HOME",
  "$env:LOCALAPPDATA\Android\Sdk",
  "User"
)
[Environment]::SetEnvironmentVariable(
  "Path",
  $env:Path + ";$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:LOCALAPPDATA\Android\Sdk\emulator",
  "User"
)
# cierra y reabre TODAS las terminales (incluyendo la de Appium si la tienes abierta)
adb devices
echo $env:ANDROID_HOME
```

Solo para la sesión actual de Git Bash (sin tocar el PATH del sistema):
=======
```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:LOCALAPPDATA\Android\Sdk\emulator", "User")
# cierra y reabre TODAS las terminales
adb devices
```

Solo para la sesión actual de Git Bash:
>>>>>>> develop

```bash
export ANDROID_HOME="/c/Users/Usuario/AppData/Local/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
```

<<<<<<< HEAD
Sin tocar nada, llamando con la ruta completa:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

### File lock al compilar (`bundleDebugClassesToCompileJar` con "El proceso no tiene acceso al archivo")

Pasa cuando un Gradle daemon previo dejó abierto un `.jar`. Solución:
=======
### File lock al compilar

Pasa cuando un Gradle daemon previo dejó abierto un `.jar`:
>>>>>>> develop

```powershell
./gradlew --stop
./gradlew testDebugUnitTest
```
<<<<<<< HEAD

Si persiste, también ayuda cerrar Android Studio (que tiene su propio daemon Gradle) y reintentar.
=======
>>>>>>> develop

---

## Solución de problemas

### "Failed to connect to /127.0.0.1:3000" en logcat

<<<<<<< HEAD
Significa que la app está usando `127.0.0.1` y no hay `adb reverse` activo. La constante en `Constants.kt` debe ser `http://10.0.2.2:3000/` para emulador. Si tu compañero la sobreescribe a `127.0.0.1`, restaura `10.0.2.2` localmente o aplica `adb reverse tcp:3000 tcp:3000` después de cada reinicio del adb daemon.

### El listado se queda vacío y no muestra error

La caché de Room guardó una respuesta vacía de un fetch anterior. Limpia los datos:

```bash
adb shell pm clear com.uniandes.vinilos
# o
adb uninstall com.uniandes.vinilos
=======
La constante en `Constants.kt` debe ser `http://10.0.2.2:3000/` para emulador. Si se sobreescribió a `127.0.0.1`, restáurala o aplica `adb reverse tcp:3000 tcp:3000` después de cada reinicio del adb daemon.

### El listado se queda vacío y no muestra error

La caché de Room guardó una respuesta vacía. Limpia los datos:

```bash
adb shell pm clear com.uniandes.vinilos
```

### KSP rompe con AGP 9 ("Using kotlin.sourceSets DSL is not allowed")

`gradle.properties` ya tiene `android.disallowKotlinSourceSets=false`. Si lo borras, vuelve a ponerlo.

### Tests instrumentados: diálogo "Android App Compatibility / 16 KB"

No es un error — la app corre en "page size compatible mode" por una dependencia de MockK. Toca **"Don't Show Again"** una vez y vuelve a correr la suite.

### Kraken: "Cannot find module" o falla al conectar Appium

Re-ejecuta `kraken/setup.sh`. Verifica que Appium esté corriendo en otra terminal y que `adb devices` muestre tu device.

### Kraken: `Could not find a driver for automationName 'UiAutomator2'`

```bash
appium driver install uiautomator2@2.29.0   # versión compatible con Appium 2.11.5
appium driver list --installed
appium
```

### Kraken: el feature siguiente falla porque la app quedó en otra pantalla

`hooks.js` usa `appium:noReset: true`, así que **la app NO se cierra entre escenarios**. Cada feature debe terminar dejando la app en HomeScreen. Como atajo:

```bash
adb shell am force-stop com.uniandes.vinilos
cd kraken && ./run.sh
```

### Kraken: orden alfabético entre features

Cucumber/Kraken corre los `.feature` en orden alfabético: `album_list.feature` → `artist_list.feature` → `navbar.feature`. Prefiere la convención de terminar en Home antes de usar prefijos como `z_*`.

### Kraken: `'one unique @user tag for each scenario'`

Cada `Scenario:` debe tener un tag `@userN` distinto. Si solo tienes un device, mantén **un único Scenario por feature**.

### Kraken: `The instrumentation process cannot be initialized`

El UIAutomator2 server APK no está instalado. Re-ejecuta `kraken/setup.sh` o instala manualmente:

```bash
SERVER_APK=$(find "$HOME/.appium" -name "appium-uiautomator2-server-v*.apk" | grep -v androidTest | head -1)
TEST_APK=$(find "$HOME/.appium" -name "*androidTest*.apk" | head -1)
adb install -r "$SERVER_APK"
adb install -r "$TEST_APK"
>>>>>>> develop
```

### "Failed to open APK ... base.apk: I/O error" en logcat (de `com.google.android.apps.wellbeing`)

<<<<<<< HEAD
Es ruido del sistema (Digital Wellbeing leyendo metadatos de instalaciones anteriores). **No afecta tu app.** Filtra logcat por tu paquete:

```bash
adb logcat --pid=$(adb shell pidof com.uniandes.vinilos)
adb logcat -s okhttp.OkHttpClient            # solo requests de Retrofit
```

### KSP rompe con AGP 9 ("Using kotlin.sourceSets DSL is not allowed")

`gradle.properties` ya tiene `android.disallowKotlinSourceSets=false`. Si lo borras a propósito, vuelve a ponerlo.
=======
## Backlog y equipo

Backlog completo, sprints e historias de usuario en la [Wiki del repositorio](../../../wiki).
>>>>>>> develop

### Kraken: "Cannot find module" o falla al conectar Appium

Re-ejecuta `kraken/setup.sh`. Verifica que Appium esté corriendo en otra terminal (`appium`) y que `adb devices` muestre tu device.

### Kraken: `Could not find a driver for automationName 'UiAutomator2'`

El driver de Appium no quedó instalado (el `setup.sh` silencia errores con `2>/dev/null`). Instálalo manualmente, una sola vez por máquina:

```bash
# detén Appium con Ctrl+C en su terminal antes
appium driver install uiautomator2@2.29.0   # versión compatible con Appium 2.11.5
appium driver list --installed              # debe mostrar uiautomator2@2.29.0 [installed]
appium                                       # vuelve a arrancar
```

Después relanza `./run.sh`.

### Kraken: `'uiautomator2' cannot be installed because the server version it requires (^3.0.0-rc.2) does not meet the currently installed one (2.11.5)`

Mismo problema, diferente síntoma: estás instalando la última versión del driver, que ya requiere Appium 3.x. Forza la versión compatible con Appium 2.11.5:

```bash
appium driver install uiautomator2@2.29.0
```

(combinación validada por el compañero — ver [kraken/KRAKEN.md](kraken/KRAKEN.md)).

### Tests instrumentados se ejecutan pero todos fallan

Verifica que el device esté desbloqueado y la pantalla activa. Los `testTag` los define el código de UI; si renombras un tag en producción debes actualizar también el test.

### Tests instrumentados: diálogo "Android App Compatibility / 16 KB"

En emuladores con Android moderno (page size 16 KB) aparece un diálogo del sistema porque la `libmockkjvmtiagent.so` que trae MockK no está alineada a 16 KB. **No es un error**: la app corre en "page size compatible mode". Toca **"Don't Show Again"** una vez y vuelve a correr la suite. Si te molesta del todo, usa un AVD con Android 14 (API 34) o crea uno con page size 4 KB en Advanced Settings.

### Kraken: el feature siguiente falla porque la app quedó en otra pantalla

El `hooks.js` usa `appium:noReset: true` y `appium:dontStopAppOnReset: true`, así que **la app NO se cierra entre escenarios**. Si un feature termina dejando la app en una pantalla de detalle (donde el bottom nav está oculto) o en un estado parcial, el siguiente feature arranca ahí y los `I see the text "Vinilos"` fallan.

**Convención:** cada feature debe **terminar dejando la app en HomeScreen**. El último step típico es:

```gherkin
Then I tap on element with accessibility id "Volver"     # si quedaste en un detalle
Then I wait
Then I tap on element with accessibility id "nav_vinilos"
Then I wait
```

Como atajo entre corridas manuales:

```bash
adb shell am force-stop com.uniandes.vinilos
cd kraken && ./run.sh
```

### Kraken: orden alfabético entre features

Cucumber/Kraken corre los `.feature` en orden alfabético. Hoy se ejecutan: `album_list.feature` → `artist_list.feature` → `navbar.feature`. Si nombras tu feature con prefijo `z_*` quedará al final, pero es un hack — prefiere la convención de "terminar en Home" descrita arriba.

### Kraken: `'one unique @user tag for each scenario'`

Cada `Scenario:` dentro del mismo `.feature` debe tener un tag `@userN` distinto (`@user1`, `@user2`, …). Kraken asigna un device por scenario. Si solo tienes un device, mantén **un único Scenario por feature** y combina los pasos.

### Kraken: server APK no instalado / driver UIAutomator2 no compatible

Causa común tras un `cold boot` del emulador o tras actualizar Appium:

```bash
# driver compatible con Appium 2.11.5:
appium driver install uiautomator2@2.29.0
appium driver list --installed

# reinstalar el server APK en el device:
cd kraken && ./setup.sh
```

### Kraken: `The instrumentation process cannot be initialized`

El UIAutomator2 server APK no está instalado (probablemente por cold boot del emulador). Re-ejecuta `kraken/setup.sh` con `ANDROID_HOME` exportado, o instala manualmente:

```bash
SERVER_APK=$(find "$HOME/.appium" -name "appium-uiautomator2-server-v*.apk" | grep -v androidTest | head -1)
TEST_APK=$(find "$HOME/.appium" -name "*androidTest*.apk" | head -1)
adb install -r "$SERVER_APK"
adb install -r "$TEST_APK"
```


## Backlog y equipo

Backlog completo, sprints e historias de usuario en la [Wiki del repositorio](../../../wiki).
Proyecto académico — Universidad de los Andes 2026.
