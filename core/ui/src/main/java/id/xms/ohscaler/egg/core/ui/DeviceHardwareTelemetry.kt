package id.xms.ohscaler.egg.core.ui

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.Build
import android.util.Log
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

data class HardwareTelemetryData(
    val cpuGhz: String,
    val cpuCores: Int,
    val cpuModel: String,
    val gpuName: String,
    val npuTops: String,
    val npuThroughputGflops: String,
    val ramUsedGb: String,
    val ramTotalGb: String,
    val appRamMb: Int,
    val freeRamGb: String
)

object DeviceHardwareTelemetry {

    private const val TAG = "DeviceTelemetry"

    @Volatile
    private var cachedGpu: String? = null
    @Volatile
    private var cachedCpuModel: String? = null
    @Volatile
    private var cachedNpuTops: String? = null

    fun getTelemetry(
        context: Context,
        elapsedSeconds: Float = 0f,
        currentTile: Int = 0
    ): HardwareTelemetryData {
        // 1. CPU Clock Speed (Live sysfs query across all active cores)
        val cpuFreqGhz = readCpuFreqGhz()
        val cpuCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val cpuStr = if (cpuFreqGhz > 0f) {
            String.format(Locale.US, "%.2f GHz", cpuFreqGhz)
        } else {
            "2.84 GHz"
        }

        // CPU Model / Architecture from /proc/cpuinfo
        if (cachedCpuModel == null) {
            cachedCpuModel = readCpuModelFromProc()
        }
        val cpuModelStr = cachedCpuModel ?: "ARMv8"

        // 2. Real GPU Device Renderer Detection via EGL14 / GLES20
        if (cachedGpu == null) {
            cachedGpu = detectRealGpuRenderer()
        }
        val gpuStr = cachedGpu ?: detectGpuFromSystemProps()

        // 3. NPU TOPS Detection (Based on detected SoC, GPU, and NNAPI)
        if (cachedNpuTops == null) {
            cachedNpuTops = detectNpuHardwareTops(gpuStr)
        }
        val npuTopsStr = cachedNpuTops ?: "32 TOPS"

        // Live Throughput Calculation (Real-ESRGAN Compact ~33.6 GFLOPs per 256x256 tile)
        val liveGflopsStr = if (currentTile > 0 && elapsedSeconds > 0.1f) {
            val avgTimePerTileSec = elapsedSeconds / currentTile
            val gflops = (33.6f / avgTimePerTileSec).roundToInt()
            "$gflops GFLOPS"
        } else {
            "NNAPI Active"
        }

        // 4. Real System & App RAM Usage
        val actMan = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actMan?.getMemoryInfo(memInfo)
        val totalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availGb = memInfo.availMem / (1024f * 1024f * 1024f)
        val usedGb = (totalGb - availGb).coerceAtLeast(0f)

        val rt = Runtime.getRuntime()
        val appHeapBytes = rt.totalMemory() - rt.freeMemory()
        val appMb = (appHeapBytes / (1024 * 1024)).toInt().coerceAtLeast(1)

        return HardwareTelemetryData(
            cpuGhz = cpuStr,
            cpuCores = cpuCores,
            cpuModel = cpuModelStr,
            gpuName = gpuStr,
            npuTops = npuTopsStr,
            npuThroughputGflops = liveGflopsStr,
            ramUsedGb = String.format(Locale.US, "%.1f", usedGb),
            ramTotalGb = String.format(Locale.US, "%.0f", totalGb),
            appRamMb = appMb,
            freeRamGb = String.format(Locale.US, "%.1f", availGb)
        )
    }

    private fun readCpuFreqGhz(): Float {
        val coreCount = Runtime.getRuntime().availableProcessors()
        var highestFreqKHz = 0L

        // Scan all CPU cores for active scaling frequency
        for (i in 0 until coreCount) {
            val freq = readFreqFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                ?: readFreqFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_cur_freq")
                ?: readFreqFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq")
                ?: readFreqFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            if (freq != null && freq > highestFreqKHz) {
                highestFreqKHz = freq
            }
        }

        if (highestFreqKHz > 0) {
            return highestFreqKHz / 1_000_000f
        }

        // Fallback: parse /proc/cpuinfo line "cpu MHz"
        try {
            val file = File("/proc/cpuinfo")
            if (file.exists() && file.canRead()) {
                val mhzLine = file.useLines { lines ->
                    lines.firstOrNull { it.startsWith("cpu MHz", ignoreCase = true) }
                }
                if (mhzLine != null) {
                    val mhz = mhzLine.substringAfter(":").trim().toFloatOrNull()
                    if (mhz != null && mhz > 0f) {
                        val khz = (mhz * 1000).toLong()
                        if (khz > highestFreqKHz) highestFreqKHz = khz
                    }
                }
            }
        } catch (_: Throwable) {}

        if (highestFreqKHz > 0) {
            return highestFreqKHz / 1_000_000f
        }

        return 3.20f
    }

    private fun readFreqFile(path: String): Long? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readText().trim().toLongOrNull()
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun readCpuModelFromProc(): String {
        try {
            val file = File("/proc/cpuinfo")
            if (file.exists() && file.canRead()) {
                val matched = file.useLines { lines ->
                    lines.firstOrNull {
                        it.startsWith("model name", ignoreCase = true) || it.startsWith("Hardware", ignoreCase = true)
                    }
                }
                if (matched != null) {
                    val raw = matched.substringAfter(":").trim()
                    if (raw.isNotBlank()) {
                        return cleanModelName(raw)
                    }
                }
            }
        } catch (_: Throwable) {}

        // Fallback from Build
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else ""
        if (soc.isNotBlank()) return soc
        return if (Build.HARDWARE.isNotBlank()) Build.HARDWARE else "ARMv8"
    }

    private fun cleanModelName(raw: String): String {
        val cleaned = raw.replace("Intel(R) Core(TM)", "Intel", ignoreCase = true)
            .replace("Qualcomm Technologies, Inc", "Snapdragon", ignoreCase = true)
            .replace("CPU @", "@", ignoreCase = true)
            .trim()
        val parts = cleaned.split(" ").filter { it.isNotBlank() }
        return if (parts.size >= 2) {
            "${parts[0]} ${parts[1]}"
        } else {
            cleaned.take(12)
        }
    }

    private fun detectRealGpuRenderer(): String? {
        try {
            val dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (dpy == EGL14.EGL_NO_DISPLAY) return null

            val major = IntArray(1)
            val minor = IntArray(1)
            if (!EGL14.eglInitialize(dpy, major, 0, minor, 0)) return null

            val configAttr = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            val configChosen = EGL14.eglChooseConfig(dpy, configAttr, 0, configs, 0, 1, numConfigs, 0)
            if (!configChosen || numConfigs[0] <= 0 || configs[0] == null) {
                EGL14.eglTerminate(dpy)
                return null
            }

            val ctxAttr = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val ctx = EGL14.eglCreateContext(dpy, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0)
            if (ctx == EGL14.EGL_NO_CONTEXT) {
                EGL14.eglTerminate(dpy)
                return null
            }

            val surfAttr = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE
            )
            val surf = EGL14.eglCreatePbufferSurface(dpy, configs[0], surfAttr, 0)
            if (surf == EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroyContext(dpy, ctx)
                EGL14.eglTerminate(dpy)
                return null
            }

            EGL14.eglMakeCurrent(dpy, surf, surf, ctx)
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR)

            EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(dpy, surf)
            EGL14.eglDestroyContext(dpy, ctx)
            EGL14.eglTerminate(dpy)

            if (!renderer.isNullOrBlank()) {
                Log.d(TAG, "EGL14 detected GPU: $renderer by $vendor")
                return formatGpuName(renderer)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "EGL14 GPU detection error: ${e.message}")
        }
        return null
    }

    private fun detectGpuFromSystemProps(): String {
        val eglProp = getSystemProperty("ro.hardware.egl")
        val boardProp = getSystemProperty("ro.board.platform")
        val chipProp = getSystemProperty("ro.chipname")
        val socProp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else ""

        val raw = when {
            eglProp.isNotBlank() && !eglProp.equals("emulation", ignoreCase = true) -> eglProp
            socProp.isNotBlank() -> socProp
            boardProp.isNotBlank() -> boardProp
            chipProp.isNotBlank() -> chipProp
            else -> Build.HARDWARE
        }
        return formatGpuName(raw)
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val m = c.getMethod("get", String::class.java)
            (m.invoke(null, key) as? String)?.trim() ?: ""
        } catch (_: Throwable) {
            ""
        }
    }

    private fun formatGpuName(raw: String): String {
        val cleaned = raw.replace("(TM)", "", ignoreCase = true)
            .replace("(R)", "", ignoreCase = true)
            .replace("Qualcomm", "", ignoreCase = true)
            .replace("Corporation", "", ignoreCase = true)
            .trim()

        val lower = cleaned.lowercase(Locale.US)
        return when {
            lower.contains("adreno") -> {
                val num = cleaned.filter { it.isDigit() }
                if (num.isNotBlank()) "Adreno $num" else "Adreno GPU"
            }
            lower.contains("mali") -> {
                val parts = cleaned.split(" ").filter { it.isNotBlank() }
                if (parts.isNotEmpty()) parts.first() else "Mali GPU"
            }
            lower.contains("kona") -> "Adreno 650"
            lower.contains("lahaina") -> "Adreno 660"
            lower.contains("taro") -> "Adreno 730"
            lower.contains("kalama") -> "Adreno 740"
            lower.contains("pineapple") -> "Adreno 750"
            lower.contains("sun") -> "Adreno 830"
            lower.contains("intel") -> "Intel GPU"
            lower.contains("geforce") || lower.contains("nvidia") -> "NVIDIA GPU"
            else -> cleaned.take(14)
        }
    }

    private fun detectNpuHardwareTops(gpu: String): String {
        val lower = gpu.lowercase(Locale.US)
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL.lowercase(Locale.US) else ""
        val hw = Build.HARDWARE.lowercase(Locale.US)

        return when {
            // Snapdragon 8 Elite / Adreno 830
            lower.contains("830") || soc.contains("sm8750") -> "73 TOPS"
            // Snapdragon 8 Gen 3 / Adreno 750 / Dimensity 9300
            lower.contains("750") || soc.contains("sm8650") || soc.contains("mt6989") -> "45 TOPS"
            // Snapdragon 8 Gen 2 / Adreno 740 / Dimensity 9200
            lower.contains("740") || soc.contains("sm8550") || soc.contains("mt6985") -> "32 TOPS"
            // Snapdragon 8 Gen 1 / 8+ Gen 1 / Adreno 730 / Dimensity 9000
            lower.contains("730") || soc.contains("sm8450") || soc.contains("sm8475") || soc.contains("mt6983") -> "27 TOPS"
            // Snapdragon 888 / Adreno 660 / Exynos 2200
            lower.contains("660") || soc.contains("sm8350") -> "26 TOPS"
            // Snapdragon 865/870 / Adreno 650 / kona
            lower.contains("650") || hw.contains("kona") -> "15 TOPS"
            // Google Tensor G3/G4
            lower.contains("tensor") || lower.contains("g715") || lower.contains("g720") -> "25 TOPS"
            // Intel OpenVINO / GNA AI
            lower.contains("intel") -> "11 TOPS"
            // Baseline modern mobile neural engine
            else -> "30 TOPS"
        }
    }
}
