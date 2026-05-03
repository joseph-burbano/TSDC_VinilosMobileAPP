plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

gradle.projectsEvaluated {
    subprojects {
        tasks.matching { it.name == "installDebug" }.configureEach {
            doLast {
                val adb = findAdb(rootDir)
                if (adb == null) {
                    logger.warn("[adb-reverse] adb no encontrado; ejecuta manualmente: adb reverse tcp:3000 tcp:3000")
                    return@doLast
                }
                val exit = ProcessBuilder(adb.absolutePath, "reverse", "tcp:3000", "tcp:3000")
                    .redirectErrorStream(true)
                    .start()
                    .also { it.inputStream.copyTo(System.out) }
                    .waitFor()
                if (exit == 0) {
                    logger.lifecycle("[adb-reverse] tcp:3000 tcp:3000 -> backend reachable from device")
                } else {
                    logger.warn("[adb-reverse] adb reverse exit=$exit (¿hay un device conectado?)")
                }
            }
        }
    }
}

fun findAdb(rootDir: java.io.File): java.io.File? {
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val adbName = if (isWindows) "adb.exe" else "adb"
    val sdkDir = System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: java.io.File(rootDir, "local.properties")
            .takeIf(java.io.File::exists)
            ?.readLines()
            ?.firstOrNull { it.startsWith("sdk.dir=") }
            ?.substringAfter("=")
            ?.replace("\\:", ":")
            ?.replace("\\\\", "\\")
            ?.trim()
    sdkDir?.let {
        val candidate = java.io.File(it, "platform-tools/$adbName")
        if (candidate.exists()) return candidate
    }
    return System.getenv("PATH")
        ?.split(java.io.File.pathSeparator)
        ?.map { java.io.File(it, adbName) }
        ?.firstOrNull(java.io.File::exists)
}
