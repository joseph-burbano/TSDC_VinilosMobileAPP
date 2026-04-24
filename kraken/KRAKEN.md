# Tests E2E con Kraken — Vinilos

Tests de caja negra automatizados usando Kraken + Appium + UIAutomator2.

---

## Prerrequisitos

| Herramienta         | Versión    | Instalación                                 |
| ------------------- | ---------- | ------------------------------------------- |
| Node.js             | ≥ 12       | [nodejs.org](https://nodejs.org)            |
| Appium              | 2.11.5     | `sudo npm install -g appium@2.11.5`         |
| UIAutomator2 driver | 2.29.0     | `appium driver install uiautomator2@2.29.0` |
| Android SDK + ADB   | cualquiera | Android Studio                              |
| Dispositivo Android | API 24+    | físico o emulador                           |

---

## Setup inicial

Solo se hace una vez por máquina.

**1. Genera el APK de la app:**

```bash
cd ..
./gradlew assembleDebug
cd kraken
```

**2. Conecta el dispositivo por USB** con depuración ADB activada y verifica:

```bash
adb devices
```

**3. Corre el script de setup:**

```bash
chmod +x setup.sh
./setup.sh
```

El script realiza automáticamente:

- Verifica que Node.js, Appium y ADB estén instalados
- Instala las dependencias de Node (`npm install`)
- Aplica los parches de compatibilidad de Kraken con Appium 2.x
- Instala el servidor UIAutomator2 en el dispositivo

---

## Configuración de red

Los tests usan `adb reverse` para conectar el dispositivo al backend que corre en Docker en tu máquina. El script `run.sh` lo activa automáticamente.

Si el backend no está corriendo, inícialo primero:

```bash
cd ../backend
docker-compose up
```

---

## Ejecutar los tests

```bash
chmod +x run.sh
./run.sh
```

---

## Escenarios disponibles

### `artist_list.feature`

Verifica el listado de artistas:

- Navega a la pantalla de Artists desde la navbar
- Verifica que aparece el header "Archivo de" y "Artistas"
- Verifica que aparecen los artistas del catálogo

### `navbar.feature`

Verifica la navegación principal:

- Verifica que las 4 tabs de la navbar son visibles (Vinyl, Álbumes, Artists, People)
- Verifica que cada tab es navegable

---

## Ver el reporte

Después de correr los tests, el reporte HTML se genera en `reports/`:

```bash
xdg-open reports/*/index.html
```

El reporte incluye el resultado de cada step y screenshots de cada acción.

---

## Notas técnicas

### Compatibilidad Kraken + Appium 2.x

Kraken 1.0.24 fue diseñado para Appium 1.x. El `setup.sh` aplica estos parches automáticamente a `node_modules/kraken-node/lib/clients/AndroidClient.js`:

| Parche                            | Razón                                                        |
| --------------------------------- | ------------------------------------------------------------ |
| `stderr` → `onStdout`             | Appium 2.x envía logs por stderr                             |
| `path: '/wd/hub'` → `path: '/'`   | Appium 2.x eliminó el path `/wd/hub`                         |
| Detección de `0.0.0.0:PORT`       | El mensaje de arranque llega en chunks separados             |
| Prefijo `appium:` en capabilities | W3C WebDriver requiere prefijo para capabilities no estándar |

### `skipServerInstallation: true`

El servidor UIAutomator2 se instala manualmente con `setup.sh` porque la instalación vía Appium tarda más de 60 segundos en dispositivos físicos con Android 13.

### `ignoreHiddenApiPolicyError: true`

Android 13 no permite modificar `hidden_api_policy` sin permisos root. Esta capability hace que Appium ignore ese error y continúe.
