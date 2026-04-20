# TSDC_ViniliosMobileAPP
Proyecto VINILIOS para aplicaciones móviles en ANDROID. Creado por: The Software Design Company.

# Vinilos — Mobile App

**Ingeniería de Software para Aplicaciones Móviles**  
Universidad de los Andes — MISO

Aplicación Android para navegar y gestionar un catálogo de álbumes de música en vinilo, artistas y coleccionistas.

---

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.0+ | Lenguaje principal |
| Jetpack Compose | BOM 2024+ | UI declarativa |
| Navigation Compose | 2.7.7 | Navegación entre pantallas |
| Material Icons Extended | 1.7.8 | Íconos de la navbar |
| Retrofit | — | Consumo de API REST (próximo sprint) |
| ViewModel + LiveData | — | Arquitectura MVVM |

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
│   (StateFlow / LiveData)            │
└────────────────┬────────────────────┘
                 │ solicita datos
┌────────────────▼────────────────────┐
│            MODEL                    │
│   repository/ → network/ → model/  │
│   (Retrofit + API REST)             │
└─────────────────────────────────────┘
```

---

## Estructura del proyecto _(aun en construccion)_

```
com/uniandes/vinilos/
├── MainActivity.kt
├── model/
│   ├── Album.kt
│   ├── Artist.kt
│   ├── Collector.kt
│   └── Track.kt
├── network/
│   ├── VinilosApi.kt
│   └── NetworkServiceAdapter.kt
├── repository/
│   ├── AlbumRepository.kt
│   ├── ArtistRepository.kt
│   └── CollectorRepository.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── home/
│   │   └── HomeScreen.kt
│   ├── albums/
│   │   ├── AlbumListScreen.kt
│   │   ├── AlbumDetailScreen.kt
│   │   └── AlbumViewModel.kt
│   ├── artists/
│   │   ├── ArtistListScreen.kt
│   │   ├── ArtistDetailScreen.kt
│   │   └── ArtistViewModel.kt
│   └── collectors/
│       ├── CollectorListScreen.kt
│       ├── CollectorDetailScreen.kt
│       └── CollectorViewModel.kt
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

- Android Studio Hedgehog o superior
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

---

## Backlog

Ver el backlog completo, distribución de sprints e historias de usuario en la [Wiki del repositorio](../../../wiki).

---

## Equipo

Proyecto desarrollado como parte del curso **MISO — Ingeniería de Software para Aplicaciones Móviles** de la Universidad de los Andes.

---

## Licencia

Proyecto académico — Universidad de los Andes 2026.
