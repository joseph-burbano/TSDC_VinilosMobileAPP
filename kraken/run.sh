#!/bin/bash
echo "=== Ejecutando tests E2E Kraken - Vinilos ==="
echo ""

echo "Verificando dispositivo conectado..."
adb devices
echo ""

echo "Activando tunnel ADB para backend..."
adb reverse tcp:3000 tcp:3000
echo ""

echo "Corriendo tests..."
./node_modules/kraken-node/bin/kraken-node run
echo ""

echo "=== Reporte disponible en reports/ ==="
xdg-open reports/*/index.html 2>/dev/null || echo "Abre manualmente: reports/*/index.html"
