#!/bin/bash
echo "=== Setup Kraken - Vinilos ==="
echo ""

echo "1. Verificando prerrequisitos..."
node --version || { echo "ERROR: Node.js no instalado"; exit 1; }
npm --version || { echo "ERROR: npm no instalado"; exit 1; }
appium --version || { echo "ERROR: Appium no instalado. Corre: sudo npm install -g appium@2.11.5"; exit 1; }
adb devices || { echo "ERROR: ADB no disponible"; exit 1; }
echo ""

echo "2. Instalando dependencias Node..."
npm install
echo ""

echo "3. Aplicando parches de compatibilidad Appium 2.x en kraken-node..."

ANDROID_CLIENT="node_modules/kraken-node/lib/clients/AndroidClient.js"

# Parche 1: stderr → onStdout
if grep -q "this.proc.stderr.on('data', this.onStderr.bind(this));" "$ANDROID_CLIENT"; then
    sed -i "s/this.proc.stderr.on('data', this.onStderr.bind(this));/this.proc.stderr.on('data', this.onStdout.bind(this));/" "$ANDROID_CLIENT"
    echo "  ✓ Parche stderr aplicado"
else
    echo "  - Parche stderr ya aplicado"
fi

# Parche 2: /wd/hub → /
if grep -q "path: '/wd/hub'" "$ANDROID_CLIENT"; then
    sed -i "s|path: '/wd/hub'|path: '/'|" "$ANDROID_CLIENT"
    echo "  ✓ Parche path aplicado"
else
    echo "  - Parche path ya aplicado"
fi

# Parche 3: detección mensaje arranque
if grep -q '"started on 0.0.0.0:" + this.port' "$ANDROID_CLIENT"; then
    sed -i 's/dataText.includes("started on 0.0.0.0:" + this.port)/dataText.includes("0.0.0.0:" + this.port)/' "$ANDROID_CLIENT"
    echo "  ✓ Parche detección arranque aplicado"
else
    echo "  - Parche detección arranque ya aplicado"
fi

# Parche 4: capabilities con prefijo appium:
if grep -q 'deviceName: "Android Emulator"' "$ANDROID_CLIENT"; then
    sed -i 's/return __assign({ platformName: "Android", deviceName: "Android Emulator", app: this.app, appPackage: this.appPackage, appActivity: this.appActivity, automationName: "UiAutomator2", udid: this.deviceId }/return __assign({ platformName: "Android", "appium:deviceName": "Android Emulator", "appium:app": this.app, "appium:appPackage": this.appPackage, "appium:appActivity": this.appActivity, "appium:automationName": "UiAutomator2", "appium:udid": this.deviceId }/' "$ANDROID_CLIENT"
    echo "  ✓ Parche capabilities aplicado"
else
    echo "  - Parche capabilities ya aplicado"
fi
echo ""

echo "4. Instalando/actualizando driver UIAutomator2..."
appium driver install uiautomator2 2>/dev/null || appium driver update uiautomator2 --unsafe 2>/dev/null || echo "  - UIAutomator2 ya actualizado."
echo ""

echo "5. Instalando UIAutomator2 server en el dispositivo..."
SERVER_APK=$(find ~/.appium/node_modules -name "appium-uiautomator2-server-v*.apk" 2>/dev/null | grep -v "androidTest" | head -1)
TEST_APK=$(find ~/.appium/node_modules -name "*androidTest*.apk" 2>/dev/null | head -1)

if [ -n "$SERVER_APK" ]; then
    echo "  Instalando server APK: $SERVER_APK"
    adb install -r "$SERVER_APK"
else
    echo "  WARN: No se encontró el server APK"
fi

if [ -n "$TEST_APK" ]; then
    echo "  Instalando test APK: $TEST_APK"
    adb install -r "$TEST_APK"
else
    echo "  WARN: No se encontró el test APK"
fi

echo "=== Setup completo. Ya puedes correr: ./run.sh ==="
