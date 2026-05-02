# Perfila la app Vinilos en un dispositivo físico conectado via ADB.
#
# Uso:
#   .\scripts\profile-device.ps1 -DeviceLabel "samsung-a52"
#
# Que hace:
#   1. Verifica que haya un dispositivo conectado.
#   2. Reinstala el APK debug (./gradlew installDebug).
#   3. Resetea contadores de batería y de GPU para tener una linea base limpia.
#   4. Lanza la app y te pide que recorras manualmente las HUs principales.
#   5. Cuando presionas Enter, captura snapshots de:
#        - meminfo  (memoria por categoría: Java/Native/Graphics/Code/Stack)
#        - gfxinfo  (jank %, frames pintados > 16ms / 50ms / 100ms)
#        - batterystats (CPU time, wakelocks, energía estimada)
#        - top de procesos (CPU% en el momento)
#   6. Guarda todo en profile-results/<DeviceLabel>-<timestamp>.txt
#
# Pre-requisitos: ADB en PATH, dispositivo conectado con USB debugging activado.

param(
    [Parameter(Mandatory = $true)]
    [string]$DeviceLabel,

    [string]$Package = "com.uniandes.vinilos",

    [int]$WarmupSeconds = 3
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$resultsDir = Join-Path $repoRoot "profile-results"
if (-not (Test-Path $resultsDir)) { New-Item -ItemType Directory -Path $resultsDir | Out-Null }

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outFile = Join-Path $resultsDir "$DeviceLabel-$timestamp.txt"

function Write-Section($title) {
    Add-Content $outFile "`n=================================================="
    Add-Content $outFile "  $title"
    Add-Content $outFile "==================================================`n"
}

# 1. Verificar dispositivo
Write-Host "[1/6] Verificando dispositivo..." -ForegroundColor Cyan
$devices = adb devices | Select-String -Pattern "device$" | Where-Object { $_ -notmatch "List of" }
if ($devices.Count -eq 0) {
    Write-Host "ERROR: No hay dispositivos conectados. Conecta un telefono via USB con depuracion activada." -ForegroundColor Red
    exit 1
}
$deviceModel = adb shell getprop ro.product.model
$androidVer  = adb shell getprop ro.build.version.release
$abi         = adb shell getprop ro.product.cpu.abi
Write-Host "  Dispositivo: $deviceModel (Android $androidVer, $abi)" -ForegroundColor Green

# 2. Instalar APK debug fresco
Write-Host "[2/6] Reinstalando APK debug (./gradlew installDebug)..." -ForegroundColor Cyan
Push-Location $repoRoot
try {
    & .\gradlew.bat installDebug 2>&1 | Select-Object -Last 5
    if ($LASTEXITCODE -ne 0) { throw "Falló installDebug" }
} finally {
    Pop-Location
}

# 3. Reset de contadores
Write-Host "[3/6] Reseteando contadores de bateria y GPU..." -ForegroundColor Cyan
adb shell dumpsys batterystats --reset 2>&1 | Out-Null
adb shell dumpsys gfxinfo $Package reset 2>&1 | Out-Null
adb shell am force-stop $Package 2>&1 | Out-Null
Start-Sleep -Seconds 1

# 4. Lanzar la app
Write-Host "[4/6] Lanzando la app..." -ForegroundColor Cyan
adb shell monkey -p $Package -c android.intent.category.LAUNCHER 1 2>&1 | Out-Null
Start-Sleep -Seconds $WarmupSeconds

# 5. Recorrido manual
Write-Host ""
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "  RECORRIDO MANUAL" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "Recorre estas pantallas en orden, sin prisa (~5s en cada una):" -ForegroundColor Yellow
Write-Host "  1. Inicio (Vinilos)" -ForegroundColor Yellow
Write-Host "  2. Tab Albumes -> entra a un album -> volver" -ForegroundColor Yellow
Write-Host "  3. Tab Artistas -> entra a un artista -> volver" -ForegroundColor Yellow
Write-Host "  4. Tab Coleccionistas -> entra a un coleccionista -> volver" -ForegroundColor Yellow
Write-Host "  5. Pull-to-refresh en alguna lista" -ForegroundColor Yellow
Write-Host ""
Read-Host "Cuando termines el recorrido, presiona Enter para capturar metricas"

# 6. Capturar metricas
Write-Host "[6/6] Capturando metricas a $outFile..." -ForegroundColor Cyan

Add-Content $outFile "Vinilos Mobile App - Profile report"
Add-Content $outFile "DeviceLabel: $DeviceLabel"
Add-Content $outFile "Timestamp:   $timestamp"
Add-Content $outFile "Model:       $deviceModel"
Add-Content $outFile "Android:     $androidVer"
Add-Content $outFile "ABI:         $abi"
Add-Content $outFile "Package:     $Package"

Write-Section "1. MEMORIA (dumpsys meminfo)"
adb shell dumpsys meminfo $Package | Out-File -FilePath $outFile -Encoding utf8 -Append

Write-Section "2. GPU / RENDER (dumpsys gfxinfo - jank, frame stats)"
adb shell dumpsys gfxinfo $Package | Out-File -FilePath $outFile -Encoding utf8 -Append

Write-Section "3. CPU INSTANTANEO (top -n 1 -p del PID de la app)"
$appPid = (adb shell pidof $Package).Trim()
if ($appPid) {
    adb shell top -n 1 -p $appPid | Out-File -FilePath $outFile -Encoding utf8 -Append
} else {
    Add-Content $outFile "(no se encontro PID de $Package)"
}

Write-Section "4. BATERIA / ENERGIA (dumpsys batterystats - filtrado al package)"
adb shell dumpsys batterystats --charged $Package | Out-File -FilePath $outFile -Encoding utf8 -Append

Write-Section "5. PROPIEDADES DEL DISPOSITIVO"
adb shell getprop ro.product.model     | Out-File -FilePath $outFile -Encoding utf8 -Append
adb shell getprop ro.product.brand     | Out-File -FilePath $outFile -Encoding utf8 -Append
adb shell getprop ro.product.cpu.abi   | Out-File -FilePath $outFile -Encoding utf8 -Append
adb shell getprop ro.build.version.release | Out-File -FilePath $outFile -Encoding utf8 -Append
adb shell getprop ro.build.version.sdk     | Out-File -FilePath $outFile -Encoding utf8 -Append
adb shell wm size                      | Out-File -FilePath $outFile -Encoding utf8 -Append
adb shell wm density                   | Out-File -FilePath $outFile -Encoding utf8 -Append

Write-Host ""
Write-Host "Listo. Reporte en: $outFile" -ForegroundColor Green
Write-Host ""
Write-Host "Tamaño del reporte:" -ForegroundColor Cyan
Get-Item $outFile | Select-Object Name, @{N='SizeKB';E={[math]::Round($_.Length / 1KB, 1)}}
