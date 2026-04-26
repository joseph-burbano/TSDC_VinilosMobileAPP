# Vinilos — Mobile App

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="100" alt="Vinilos icon"/>
</p>

<p align="center">
  <img src="https://github.com/TU_ORG/TU_REPO/actions/workflows/NOMBRE_WORKFLOW.yml/badge.svg" alt="CI"/>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen" alt="API 21+"/>
</p>

**Ingeniería de Software para Aplicaciones Móviles** — Universidad de los Andes (MISO).  
The Software Design Company.

Aplicación Android para navegar y gestionar un catálogo de álbumes de música en vinilo, artistas y coleccionistas.

---

## Tabla de contenido

1. [Stack](#stack)
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

---

## Arquitectura

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

---

## Estructura del proyecto

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

El backend NestJS + Postgres es un repositorio independiente. Clónalo en una carpeta paralela al proyecto:

```bash
git clone https://github.com/TheSoftwareDesignLab/BackVynils
cd BackVynils
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

| Entorno                           | URL que ve la app                | Configuración                                                               |
| --------------------------------- | -------------------------------- | --------------------------------------------------------------------------- |
| Emulador Android Studio (default) | `http://10.0.2.2:3000/`          | Funciona automáticamente — `10.0.2.2` es el alias QEMU al host              |
| Dispositivo físico (USB)          | `http://localhost:3000/`         | Requiere `adb reverse tcp:3000 tcp:3000` (se aplica solo en `installDebug`) |
| Dispositivo físico en LAN         | `http://<IP-LAN-de-tu-PC>:3000/` | Edita `Constants.BASE_URL` localmente (no commitear)                        |

La URL base vive en `app/src/main/java/com/uniandes/vinilos/util/Constants.kt`.

---

## Correr la app

Con backend arriba y emulador o device conectado:

```bash
./gradlew installDebug             # instala el debug APK
./gradlew assembleDebug            # solo compila APK -> app/build/outputs/apk/debug/
```

O **Run** desde Android Studio (dispara `installDebug`). Cada `installDebug` ejecuta automáticamente `adb reverse tcp:3000 tcp:3000` vía el hook configurado en `build.gradle.kts` — útil en device físico, inocuo en emulador.

---

## Pruebas unitarias (JVM)

Ubicación: `app/src/test/java/com/uniandes/vinilos/`.

**No requieren** emulador, device, ni backend. Tardan < 5 segundos.

### Qué cubren

| Suite                  | Archivo                              | Casos | Qué valida                                                                                   |
| ---------------------- | ------------------------------------ | ----- | -------------------------------------------------------------------------------------------- |
| ArtistRepositoryTest   | `repository/ArtistRepositoryTest.kt` | 4     | Cache-first (DAO → API), combina músicos y bandas, refresh borra y refetchea                 |
| AlbumRepositoryTest    | `repository/AlbumRepositoryTest.kt`  | 8     | Patrón cache-first, mapeo DTO → modelo, `releaseDate` ISO truncado al año, `refreshAlbums()` |
| AlbumViewModelTest     | `ui/albums/AlbumViewModelTest.kt`    | 9     | Estados Loading/Success/Error, `IOException`, `HttpException`, refresh, `findById`           |
| VinilosApiContractTest | `network/VinilosApiContractTest.kt`  | 3     | Contrato HTTP `GET /albums` con MockWebServer                                                |
| ExampleUnitTest        | `ExampleUnitTest.kt`                 | 1     | Smoke del runner                                                                             |

### Cómo correrlas

```bash
./gradlew testDebugUnitTest                                          # todos
./gradlew testDebugUnitTest --tests "*ArtistRepositoryTest"          # una clase
./gradlew testDebugUnitTest --tests "*AlbumViewModelTest.*refresh*"  # un método (glob)
./gradlew testDebugUnitTest --rerun-tasks                            # forzar re-ejecución
```

### Dónde ver los resultados

```
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

---

## Pruebas instrumentadas (Compose / Espresso)

Ubicación: `app/src/androidTest/java/com/uniandes/vinilos/`.

**Requieren** emulador o device conectado. **No** requieren backend (la API se mockea con MockK).

### Qué cubren

| HU   | Suite                             | Casos | Qué valida                                                                                                                                                              |
| ---- | --------------------------------- | ----- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| HU01 | AlbumListScreenInstrumentedTest   | 5     | Skeleton durante carga, render del listado, mensaje de error, botón "Reintentar", estado vacío                                                                          |
| HU02 | AlbumDetailScreenInstrumentedTest | 5     | Spinner mientras carga, render completo del detalle (título, género, año, sello, secciones ARTISTAS y CANCIONES), botón Volver, mensaje de error, "Álbum no encontrado" |
| HU03 | ArtistListScreenTest              | 4     | Spinner durante carga, nombres de artistas, grilla con testTags, mensaje de error                                                                                       |

### Cómo correrlas

```bash
adb devices                                                # verificar device disponible
./gradlew connectedDebugAndroidTest                        # full suite
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.uniandes.vinilos.ui.albums.AlbumListScreenInstrumentedTest
./gradlew assembleDebugAndroidTest                         # solo compilar APK de tests, sin ejecutar
```

### Dónde ver los resultados

```
app/build/reports/androidTests/connected/debug/index.html
app/build/outputs/androidTest-results/connected/debug/*.xml
```

---

## Pruebas E2E con Kraken

Ubicación: `kraken/`. Ver detalles técnicos en `kraken/README.md`.

Pruebas de **caja negra** sobre el APK real, dirigidas con Cucumber + Kraken-Node + Appium 2.x + UIAutomator2.

**Requieren**: device conectado, backend levantado.

> **Importante:** mantén la pantalla del device activa durante la ejecución.  
> Activa **Ajustes → Opciones de desarrollador → Mantener pantalla activa**.

### Qué cubren

| HU   | Feature              | Archivo                                | Qué valida                                                                           |
| ---- | -------------------- | -------------------------------------- | ------------------------------------------------------------------------------------ |
| HU01 | Listado de álbumes   | `kraken/features/album_list.feature`   | Navegar al catálogo, ver el header y confirmar que un álbum aparece en la lista      |
| HU02 | Detalle de álbum     | `kraken/features/album_detail.feature` | Tap en un álbum, verificar género, secciones ARTISTAS y CANCIONES, volver con Volver |
| HU03 | Listado de artistas  | `kraken/features/artist_list.feature`  | Navega al listado de artistas y verifica su contenido                                |
| —    | Navegación principal | `kraken/features/navbar.feature`       | Las 4 tabs son visibles y navegables                                                 |

### Setup inicial (una sola vez por máquina)

```bash
cd kraken
chmod +x setup.sh run.sh
./setup.sh
```

`setup.sh` instala dependencias Node, parchea `kraken-node` 1.0.24 para Appium 2.x e instala el server APK de UIAutomator2 en el device.

### Correr los tests

```bash
# 1. Backend arriba (en otra terminal, ver sección "Levantar el backend")

# 2. APK de la app compilado
./gradlew assembleDebug

# 3. Ejecutar
cd kraken
./run.sh
```

`run.sh` reinicia la app, aplica `adb reverse tcp:3000 tcp:3000` y lanza la suite.

### Dónde ver los resultados

```
kraken/reports/<timestamp>/index.html
```

El reporte HTML incluye cada step ejecutado y screenshots automáticos.

---

## Notas para Windows

PowerShell **no ejecuta** comandos Unix (`chmod`, `./script.sh`, etc.) ni los scripts de Kraken. Para todo lo de la sección Kraken usa **Git Bash** (viene con Git for Windows) o WSL. Para los comandos `./gradlew` puedes usar PowerShell sin problema.

```powershell
# En PowerShell los comandos Gradle van igual:
./gradlew testDebugUnitTest
./gradlew installDebug
```

Para Kraken, abre **Git Bash** (clic derecho en carpeta → "Git Bash Here"):

```bash
cd kraken
./setup.sh   # solo la primera vez
./run.sh
```

### `adb` no se reconoce / `ANDROID_HOME not exported`

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:LOCALAPPDATA\Android\Sdk\emulator", "User")
# cierra y reabre TODAS las terminales
adb devices
```

Solo para la sesión actual de Git Bash:

```bash
export ANDROID_HOME="/c/Users/Usuario/AppData/Local/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
```

### File lock al compilar

Pasa cuando un Gradle daemon previo dejó abierto un `.jar`:

```powershell
./gradlew --stop
./gradlew testDebugUnitTest
```

---

## Solución de problemas

### "Failed to connect to /127.0.0.1:3000" en logcat

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

Re-ejecuta `kraken/setup.sh`. Verifica que `adb devices` muestre tu device.

### Kraken: `Could not find a driver for automationName 'UiAutomator2'`

```bash
appium driver install uiautomator2@2.29.0
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

Cucumber/Kraken corre los `.feature` en orden alfabético: `album_detail.feature` → `album_list.feature` → `artist_list.feature` → `navbar.feature`. Prefiere la convención de terminar en Home.

### Kraken: `'one unique @user tag for each scenario'`

Cada `Scenario:` debe tener un tag `@userN` distinto. Si solo tienes un device, mantén **un único Scenario por feature**.

### Kraken: `The instrumentation process cannot be initialized`

El UIAutomator2 server APK no está instalado. Re-ejecuta `kraken/setup.sh` o instala manualmente:

```bash
SERVER_APK=$(find "$HOME/.appium" -name "appium-uiautomator2-server-v*.apk" | grep -v androidTest | head -1)
TEST_APK=$(find "$HOME/.appium" -name "*androidTest*.apk" | head -1)
adb install -r "$SERVER_APK"
adb install -r "$TEST_APK"
```

---

## Backlog y equipo

Backlog completo, sprints e historias de usuario en la [Wiki del repositorio](../../../wiki).

Proyecto académico — Universidad de los Andes 2026.
