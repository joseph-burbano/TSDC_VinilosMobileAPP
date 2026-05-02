# Perfila la app Vinilos en un dispositivo fisico conectado via ADB.
#
# Uso:
#   .\scripts\profile-device.ps1 -DeviceLabel "samsung-a52"
#   .\scripts\profile-device.ps1 -DeviceLabel "pixel-7" -SkipInstall
#
# Que hace:
#   1. Verifica que haya un dispositivo conectado.
#   2. Reinstala el APK debug (./gradlew installDebug). Skipeable con -SkipInstall.
#   3. Resetea contadores de bateria y de GPU para tener una linea base limpia.
#   4. Lanza la app y te pide que recorras manualmente las HUs principales.
#   5. Cuando presionas Enter, captura snapshots de:
#        - meminfo  (memoria por categoria: Java/Native/Graphics/Code/Stack)
#        - gfxinfo  (jank %, frames pintados > 16ms / 50ms / 100ms)
#        - batterystats (CPU time, wakelocks, energia estimada)
#        - top de procesos (CPU% en el momento)
#   6. Guarda todo en profile-results/<DeviceLabel>-<timestamp>.txt
#
# Pre-requisitos: ADB en PATH, dispositivo conectado con USB debugging activado.
#
# Nota PowerShell 5.1: NO usamos `2>&1` en comandos nativos (adb, gradlew) porque
# el host wraps cada linea de stderr como ErrorRecord, y combinado con Stop
# aborta el script aunque el comando haya terminado con exit 0. Usamos `*>$null`
# para descartar todos los streams cuando solo nos importa el side-effect.

param(
    [Parameter(Mandatory = $true)]
    [string]$DeviceLabel,

    [string]$Package = "com.uniandes.vinilos",

    [int]$WarmupSeconds = 3,

    [switch]$SkipInstall
)

# Continue (no Stop): adb suele escribir warnings en stderr ("daemon started",
# "args: [...]") aunque la operacion sea exitosa. Solo abortamos manualmente
# tras revisar $LASTEXITCODE en los pasos criticos.
$ErrorActionPreference = "Continue"

$repoRoot = Split-Path -Parent $PSScriptRoot
$resultsDir = Join-Path $repoRoot "profile-results"
if (-not (Test-Path $resultsDir)) { New-Item -ItemType Directory -Path $resultsDir | Out-Null }

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outFile = Join-Path $resultsDir "$DeviceLabel-$timestamp.txt"

function Write-Section($title) {
    Add-Content -Path $outFile -Encoding utf8 -Value "`n=================================================="
    Add-Content -Path $outFile -Encoding utf8 -Value "  $title"
    Add-Content -Path $outFile -Encoding utf8 -Value "==================================================`n"
}

function Invoke-Adb {
    # Ejecuta adb capturando stdout y descartando stderr para que PowerShell 5.1
    # no convierta lineas informativas en ErrorRecord.
    param([Parameter(Mandatory = $true)][string[]]$AdbArgs)
    $tmpErr = [System.IO.Path]::GetTempFileName()
    try {
        $output = & adb @AdbArgs 2>$tmpErr
        return $output
    } finally {
        Remove-Item $tmpErr -ErrorAction SilentlyContinue
    }
}

# 1. Verificar dispositivo
Write-Host "[1/6] Verificando dispositivo..." -ForegroundColor Cyan
$devicesRaw = Invoke-Adb -AdbArgs @("devices")
$devices = $devicesRaw | Select-String -Pattern "device$" | Where-Object { $_ -notmatch "List of" }
if (-not $devices -or $devices.Count -eq 0) {
    Write-Host "ERROR: No hay dispositivos conectados. Conecta un telefono via USB con depuracion activada." -ForegroundColor Red
    exit 1
}
$deviceModel = (Invoke-Adb -AdbArgs @("shell","getprop","ro.product.model")) -join ""
$androidVer  = (Invoke-Adb -AdbArgs @("shell","getprop","ro.build.version.release")) -join ""
$abi         = (Invoke-Adb -AdbArgs @("shell","getprop","ro.product.cpu.abi")) -join ""
Write-Host "  Dispositivo: $($deviceModel.Trim()) (Android $($androidVer.Trim()), $($abi.Trim()))" -ForegroundColor Green

# 2. Instalar APK debug fresco
if ($SkipInstall) {
    Write-Host "[2/6] -SkipInstall activo; no se reinstala el APK." -ForegroundColor Yellow
} else {
    Write-Host "[2/6] Reinstalando APK debug (./gradlew installDebug)..." -ForegroundColor Cyan
    Push-Location $repoRoot
    try {
        & .\gradlew.bat installDebug | Select-Object -Last 5
        if ($LASTEXITCODE -ne 0) {
            Write-Host "ERROR: installDebug fallo (exit code $LASTEXITCODE)." -ForegroundColor Red
            exit 1
        }
    } finally {
        Pop-Location
    }
}

# 3. Reset de contadores
Write-Host "[3/6] Reseteando contadores de bateria y GPU..." -ForegroundColor Cyan
Invoke-Adb -AdbArgs @("shell","dumpsys","batterystats","--reset") | Out-Null
Invoke-Adb -AdbArgs @("shell","dumpsys","gfxinfo",$Package,"reset") | Out-Null
Invoke-Adb -AdbArgs @("shell","am","force-stop",$Package) | Out-Null
Start-Sleep -Seconds 1

# 4. Lanzar la app
Write-Host "[4/6] Lanzando la app..." -ForegroundColor Cyan
Invoke-Adb -AdbArgs @("shell","monkey","-p",$Package,"-c","android.intent.category.LAUNCHER","1") | Out-Null
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

# Cabecera del reporte
Set-Content -Path $outFile -Encoding utf8 -Value "Vinilos Mobile App - Profile report"
Add-Content -Path $outFile -Encoding utf8 -Value "DeviceLabel: $DeviceLabel"
Add-Content -Path $outFile -Encoding utf8 -Value "Timestamp:   $timestamp"
Add-Content -Path $outFile -Encoding utf8 -Value "Model:       $($deviceModel.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "Android:     $($androidVer.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "ABI:         $($abi.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "Package:     $Package"

Write-Section "1. MEMORIA (dumpsys meminfo)"
$mem = Invoke-Adb -AdbArgs @("shell","dumpsys","meminfo",$Package)
Add-Content -Path $outFile -Encoding utf8 -Value $mem

Write-Section "2. GPU / RENDER (dumpsys gfxinfo - jank, frame stats)"
$gfx = Invoke-Adb -AdbArgs @("shell","dumpsys","gfxinfo",$Package)
Add-Content -Path $outFile -Encoding utf8 -Value $gfx

Write-Section "3. CPU INSTANTANEO (top -n 1 -p del PID de la app)"
$appPidRaw = Invoke-Adb -AdbArgs @("shell","pidof",$Package)
$appPid = ($appPidRaw -join "").Trim()
if ($appPid) {
    $top = Invoke-Adb -AdbArgs @("shell","top","-n","1","-p",$appPid)
    Add-Content -Path $outFile -Encoding utf8 -Value $top
} else {
    Add-Content -Path $outFile -Encoding utf8 -Value "(no se encontro PID de $Package)"
}

Write-Section "4. BATERIA / ENERGIA (dumpsys batterystats - filtrado al package)"
$bat = Invoke-Adb -AdbArgs @("shell","dumpsys","batterystats","--charged",$Package)
Add-Content -Path $outFile -Encoding utf8 -Value $bat

Write-Section "5. PROPIEDADES DEL DISPOSITIVO"
$brand   = Invoke-Adb -AdbArgs @("shell","getprop","ro.product.brand")
$sdk     = Invoke-Adb -AdbArgs @("shell","getprop","ro.build.version.sdk")
$wmSize  = Invoke-Adb -AdbArgs @("shell","wm","size")
$wmDens  = Invoke-Adb -AdbArgs @("shell","wm","density")
Add-Content -Path $outFile -Encoding utf8 -Value "model:    $($deviceModel.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "brand:    $(($brand -join '').Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "abi:      $($abi.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "android:  $($androidVer.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "sdk:      $(($sdk -join '').Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "wm size:  $(($wmSize -join '').Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "wm dens:  $(($wmDens -join '').Trim())"

Write-Host ""
Write-Host "Listo. Reporte en: $outFile" -ForegroundColor Green
Write-Host ""
Write-Host "Tamano del reporte:" -ForegroundColor Cyan
Get-Item $outFile | Select-Object Name, @{N='SizeKB';E={[math]::Round($_.Length / 1KB, 1)}}
