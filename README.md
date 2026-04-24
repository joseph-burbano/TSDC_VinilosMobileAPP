# TSDC_ViniliosMobileAPP

Proyecto VINILIOS para aplicaciones móviles en ANDROID. Creado por: The Software Design Company.

# Vinilos — Mobile App

**Ingeniería de Software para Aplicaciones Móviles**  
Universidad de los Andes — MISO

Aplicación Android para navegar y gestionar un catálogo de álbumes de música en vinilo, artistas y coleccionistas.

---

## Tecnologías

| Tecnología                 | Versión              | Uso                        |
| -------------------------- | -------------------- | -------------------------- |
| Kotlin                     | 2.2.10               | Lenguaje principal         |
| AGP                        | 9.1.1                | Android Gradle Plugin      |
| Jetpack Compose            | BOM 2026.02.01       | UI declarativa             |
| Navigation Compose         | 2.8.0                | Navegación entre pantallas |
| Retrofit                   | 3.0.0                | Consumo de API REST        |
| Room + KSP                 | 2.7.0 / 2.2.10-2.0.2 | Persistencia local         |
| ViewModel + StateFlow      | 2.10.0               | Arquitectura MVVM          |
| Coroutines                 | 1.10.2               | Programación asíncrona     |
| Espresso + Compose Testing | BOM                  | Tests instrumentados       |

---

## Arquitectura

El proyecto sigue el patrón **MVVM (Model - View - ViewModel)** con organización **por feature**.

Para más detalles sobre la arquitectura MVVM en Android, consultar (usar traductor ya que la fuente esta en frances) :  
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
com/uniandes/vinilos/
├── MainActivity.kt
├── database/
│   ├── dao/
│   │   └── PerformerDao.kt
│   ├── entities/
│   │   └── PerformerEntity.kt
│   ├── Mappers.kt
│   └── VinilosDatabase.kt
├── model/
│   ├── Album.kt
│   ├── Collector.kt
│   ├── CollectorAlbum.kt
│   ├── Comment.kt
│   ├── Performer.kt
│   └── Track.kt
├── network/
│   ├── VinilosApi.kt
│   └── NetworkServiceAdapter.kt
├── repository/
│   └── ArtistRepository.kt
├── ui/
│   ├── theme/
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── home/
│   │   └── HomeScreen.kt
│   ├── albums/
│   │   └── AlbumListScreen.kt
│   ├── artists/
│   │   ├── ArtistListScreen.kt
│   │   └── ArtistViewModel.kt
│   └── collectors/
│       └── CollectorListScreen.kt
└── util/
    ├── Constants.kt
    └── FakeData.kt
```

### ¿Por qué organización por feature?

En lugar de agrupar todos los `Screen.kt` juntos y todos los `ViewModel.kt` juntos, cada feature tiene su propia carpeta con todo lo que necesita. Esto permite:

- Navegar el proyecto de forma más intuitiva
- Modificar una feature sin tocar otras carpetas
- Escalar el equipo asignando features a personas distintas

---

## Cómo correr el proyecto

### Prerrequisitos

- Android Studio Otter 3 Feature Drop (2025.2.3) o superior
- JDK 17+
- Dispositivo Android (API 24+) o emulador

### Pasos

```bash
# Clona el repositorio
git clone https://github.com/joseph-burbano/TSDC_VinilosMobileAPP
cd TSDC_VinilosMobileAPP

# Abre en Android Studio y sincroniza Gradle
# Conecta un dispositivo o inicia el emulador
# Presiona [Play] Run
```

### Tests E2E con Kraken

```bash
# Instala dependencias
npm install kraken-node --save
npm install -g appium
appium driver install uiautomator2

# Verifica configuración
npx kraken-node doctor

# Genera el APK
./gradlew assembleDebug

# Configura mobile.json con la ruta al APK generado
# Corre los tests
npx kraken-node run
```

---

## Configuración de red

La URL base del API está en `util/Constants.kt`.

| Entorno                 | URL                          |
| ----------------------- | ---------------------------- |
| Emulador Android Studio | `http://10.0.2.2:3000/`      |
| Dispositivo físico      | `http://<tu-IP-local>:3000/` |

El repositorio usa la URL del emulador por defecto. Si corres en dispositivo físico reemplaza la IP en `Constants.kt` sin commitear el cambio.

## Backlog

Ver el backlog completo, distribución de sprints e historias de usuario en la [Wiki del repositorio](../../../wiki).

---

## Equipo

Proyecto desarrollado como parte del curso **MISO — Ingeniería de Software para Aplicaciones Móviles** de la Universidad de los Andes.

---

## Licencia

Proyecto académico — Universidad de los Andes 2026.
