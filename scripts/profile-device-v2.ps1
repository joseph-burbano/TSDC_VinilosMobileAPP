# Perfila la app Vinilos en un dispositivo fisico conectado via ADB.
# Version v2: misma metodologia que profile-device.ps1, pero escribe en
# profile-results-v2/ y agrega:
#   - Threads del proceso (ps -T -p <pid>) -> efecto de WhileSubscribed.
#   - CPU global por proceso (dumpsys cpuinfo) acumulado desde el reset.
#   - CPU del proceso muestreado en el tiempo (top -d 1 -n 30) -> permite
#     calcular promedio/pico, no solo el instante.
#   - Bateria REAL via USB usando "dumpsys battery unplug": el OS cree
#     que esta desconectado y empieza a acumular batterystats reales,
#     aunque el dispositivo sigue cargando fisicamente (no hay riesgo).
#     Al final se restaura con "dumpsys battery reset".
#
# Uso:
#   .\scripts\profile-device-v2.ps1 -DeviceLabel "poco-f5-pro"
#   .\scripts\profile-device-v2.ps1 -DeviceLabel "redmi-note-9-pro" -SkipInstall
#   .\scripts\profile-device-v2.ps1 -DeviceLabel "poco-f5-pro" -CpuSampleSeconds 60
#   .\scripts\profile-device-v2.ps1 -DeviceLabel "poco-f5-pro" -SkipBatteryUnplug  # si tu device no soporta unplug
#
# Salida:
#   profile-results-v2/<DeviceLabel>-<timestamp>.txt
#
# Comparacion contra baseline:
#   - profile-results/        -> snapshots PRE corrutinas (PR #72)
#   - profile-results-v2/     -> snapshots POST corrutinas (este script)
#
# Garantia de seguridad: si el script falla a mitad, el bloque finally
# restaura "dumpsys battery reset" para no dejar el dispositivo con la
# simulacion de unplug activa (lo cual interfiere con el monitoreo
# normal de bateria).

param(
    [Parameter(Mandatory = $true)]
    [string]$DeviceLabel,

    [string]$Package = "com.uniandes.vinilos",

    [int]$WarmupSeconds = 3,

    [switch]$SkipInstall,

    [string]$OutputFolder = "profile-results-v2",

    [int]$CpuSampleSeconds = 30,

    [switch]$SkipBatteryUnplug
)

$ErrorActionPreference = "Continue"

$repoRoot = Split-Path -Parent $PSScriptRoot
$resultsDir = Join-Path $repoRoot $OutputFolder
if (-not (Test-Path $resultsDir)) { New-Item -ItemType Directory -Path $resultsDir | Out-Null }

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outFile = Join-Path $resultsDir "$DeviceLabel-$timestamp.txt"

function Write-Section($title) {
    Add-Content -Path $outFile -Encoding utf8 -Value "`n=================================================="
    Add-Content -Path $outFile -Encoding utf8 -Value "  $title"
    Add-Content -Path $outFile -Encoding utf8 -Value "==================================================`n"
}

function Invoke-Adb {
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
Write-Host "[1/8] Verificando dispositivo..." -ForegroundColor Cyan
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
    Write-Host "[2/8] -SkipInstall activo; no se reinstala el APK." -ForegroundColor Yellow
} else {
    Write-Host "[2/8] Reinstalando APK debug (./gradlew installDebug)..." -ForegroundColor Cyan
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
Write-Host "[3/8] Reseteando contadores de bateria, GPU y CPU..." -ForegroundColor Cyan
Invoke-Adb -AdbArgs @("shell","dumpsys","batterystats","--reset") | Out-Null
Invoke-Adb -AdbArgs @("shell","dumpsys","gfxinfo",$Package,"reset") | Out-Null
Invoke-Adb -AdbArgs @("shell","dumpsys","cpuinfo","--reset") | Out-Null
Invoke-Adb -AdbArgs @("shell","am","force-stop",$Package) | Out-Null
Start-Sleep -Seconds 1

# 3b. Simular dispositivo desconectado para que batterystats acumule
#     datos reales aunque el USB siga conectado.
$batteryUnplugged = $false
if (-not $SkipBatteryUnplug) {
    Write-Host "[3b/8] Simulando bateria desconectada (dumpsys battery unplug)..." -ForegroundColor Cyan
    # "unplug" es atajo a "set ac 0; set usb 0; set wireless 0"
    Invoke-Adb -AdbArgs @("shell","dumpsys","battery","unplug") | Out-Null
    $batteryUnplugged = $true
    Write-Host "  El dispositivo cree que esta a bateria. Sigue cargando fisicamente." -ForegroundColor Gray
    Write-Host "  Se restaurara automaticamente al final (dumpsys battery reset)." -ForegroundColor Gray
} else {
    Write-Host "[3b/8] -SkipBatteryUnplug activo; batterystats reportara 0ms si el USB esta conectado." -ForegroundColor Yellow
}

# Bloque try/finally para garantizar restauracion del estado de bateria
# aunque el script falle a mitad o el usuario aborte (Ctrl+C).
try {

# 4. Lanzar la app
Write-Host "[4/8] Lanzando la app..." -ForegroundColor Cyan
Invoke-Adb -AdbArgs @("shell","monkey","-p",$Package,"-c","android.intent.category.LAUNCHER","1") | Out-Null
Start-Sleep -Seconds $WarmupSeconds

# 5. Recorrido manual
Write-Host ""
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "  RECORRIDO MANUAL (mismo guion que el baseline)" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "Recorre estas pantallas en orden, sin prisa (~5s en cada una):" -ForegroundColor Yellow
Write-Host "  1. Inicio (Vinilos)" -ForegroundColor Yellow
Write-Host "  2. Tab Albumes -> entra a un album -> volver" -ForegroundColor Yellow
Write-Host "  3. Tab Artistas -> entra a un artista -> volver" -ForegroundColor Yellow
Write-Host "  4. Tab Coleccionistas -> entra a un coleccionista -> volver" -ForegroundColor Yellow
Write-Host "  5. Pull-to-refresh en alguna lista (idealmente en Artistas: dispara el async paralelo de PR #72)" -ForegroundColor Yellow
Write-Host ""
Read-Host "Cuando termines el recorrido, presiona Enter para capturar metricas"

# 6. Capturar metricas
Write-Host "[5/8] Capturando memoria a $outFile..." -ForegroundColor Cyan

# Cabecera del reporte
Set-Content -Path $outFile -Encoding utf8 -Value "Vinilos Mobile App - Profile report v2 (post-corrutinas)"
Add-Content -Path $outFile -Encoding utf8 -Value "DeviceLabel: $DeviceLabel"
Add-Content -Path $outFile -Encoding utf8 -Value "Timestamp:   $timestamp"
Add-Content -Path $outFile -Encoding utf8 -Value "Model:       $($deviceModel.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "Android:     $($androidVer.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "ABI:         $($abi.Trim())"
Add-Content -Path $outFile -Encoding utf8 -Value "Package:     $Package"
Add-Content -Path $outFile -Encoding utf8 -Value "Script:      profile-device-v2.ps1"
Add-Content -Path $outFile -Encoding utf8 -Value "Comparable contra: profile-results/$DeviceLabel-*.txt"

Write-Section "1. MEMORIA (dumpsys meminfo)"
$mem = Invoke-Adb -AdbArgs @("shell","dumpsys","meminfo",$Package)
Add-Content -Path $outFile -Encoding utf8 -Value $mem

Write-Host "[6/8] Capturando GPU/render..." -ForegroundColor Cyan
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

# === SECCIONES NUEVAS DE V2 ===

Write-Host "[7/8] Capturando threads y CPU global..." -ForegroundColor Cyan

Write-Section "6. THREADS DEL PROCESO (ps -T -p <pid>)"
# ps -T lista cada thread del PID. Permite contar threads activos
# y ver el efecto de WhileSubscribed(5_000L) liberando coroutines.
if ($appPid) {
    $threads = Invoke-Adb -AdbArgs @("shell","ps","-T","-p",$appPid)
    Add-Content -Path $outFile -Encoding utf8 -Value $threads
    # Conteo agregado al final
    $threadCount = ($threads | Measure-Object -Line).Lines - 1  # menos cabecera
    Add-Content -Path $outFile -Encoding utf8 -Value ""
    Add-Content -Path $outFile -Encoding utf8 -Value "Total de threads: $threadCount"
} else {
    Add-Content -Path $outFile -Encoding utf8 -Value "(no se encontro PID de $Package)"
}

Write-Section "7. CPU GLOBAL (dumpsys cpuinfo)"
# CPU% acumulado por proceso desde el ultimo reset (paso 3).
# Permite ubicar a com.uniandes.vinilos dentro del consumo total del sistema.
$cpuinfo = Invoke-Adb -AdbArgs @("shell","dumpsys","cpuinfo")
Add-Content -Path $outFile -Encoding utf8 -Value $cpuinfo

Write-Host "[8/8] Muestreando CPU del proceso durante $CpuSampleSeconds s (1s/sample)..." -ForegroundColor Cyan
Write-Host "  Mantén la app en foreground; idealmente sigue navegando entre tabs." -ForegroundColor Gray

Write-Section "8. CPU MUESTREADO EN EL TIEMPO (top -d 1 -n $CpuSampleSeconds -p <pid>)"
# Sampling de CPU del PID a 1Hz durante CpuSampleSeconds. Permite calcular
# promedio/mediana/pico del %CPU del proceso, mas informativo que el
# instante unico capturado en la seccion 3.
if ($appPid) {
    $cpuTime = Invoke-Adb -AdbArgs @("shell","top","-d","1","-n",$CpuSampleSeconds,"-p",$appPid,"-q","-b")
    Add-Content -Path $outFile -Encoding utf8 -Value $cpuTime

    # Resumen agregado al final: extrae los valores de %CPU de las lineas
    # que matchean el PID y calcula promedio/min/max.
    $cpuValues = $cpuTime | Select-String -Pattern "^\s*$appPid\s" | ForEach-Object {
        $cols = ($_.Line -split "\s+") | Where-Object { $_ -ne "" }
        # En el output de top, %CPU esta en la columna 9 (S[%CPU]).
        # Indice 0=PID, 1=USER, 2=PR, 3=NI, 4=VIRT, 5=RES, 6=SHR, 7=S, 8=%CPU
        if ($cols.Length -ge 9) {
            try { [double]$cols[8] } catch { $null }
        }
    } | Where-Object { $_ -ne $null }

    if ($cpuValues -and $cpuValues.Count -gt 0) {
        $avg = ($cpuValues | Measure-Object -Average).Average
        $min = ($cpuValues | Measure-Object -Minimum).Minimum
        $max = ($cpuValues | Measure-Object -Maximum).Maximum
        Add-Content -Path $outFile -Encoding utf8 -Value ""
        Add-Content -Path $outFile -Encoding utf8 -Value "Resumen CPU del PID $appPid sobre $($cpuValues.Count) muestras:"
        Add-Content -Path $outFile -Encoding utf8 -Value ("  promedio: {0:N2} %" -f $avg)
        Add-Content -Path $outFile -Encoding utf8 -Value ("  minimo:   {0:N2} %" -f $min)
        Add-Content -Path $outFile -Encoding utf8 -Value ("  maximo:   {0:N2} %" -f $max)
    }
} else {
    Add-Content -Path $outFile -Encoding utf8 -Value "(no se encontro PID de $Package; no se hizo sampling)"
}

Write-Host ""
Write-Host "Listo. Reporte v2 en: $outFile" -ForegroundColor Green
Write-Host ""
Write-Host "Tamano del reporte:" -ForegroundColor Cyan
Get-Item $outFile | Select-Object Name, @{N='SizeKB';E={[math]::Round($_.Length / 1KB, 1)}}

Write-Host ""
Write-Host "Para comparar contra baseline:" -ForegroundColor Cyan
Write-Host "  Baseline:    profile-results/$DeviceLabel-*.txt" -ForegroundColor Gray
Write-Host "  Post-PR#72:  $outFile" -ForegroundColor Gray

}
finally {
    # Restauracion de bateria: critico que se ejecute aunque el script aborte.
    if ($batteryUnplugged) {
        Write-Host ""
        Write-Host "Restaurando estado de bateria del dispositivo (dumpsys battery reset)..." -ForegroundColor Cyan
        Invoke-Adb -AdbArgs @("shell","dumpsys","battery","reset") | Out-Null
        Write-Host "  Listo. El monitoreo de bateria del OS volvio a la normalidad." -ForegroundColor Green
    }
}
