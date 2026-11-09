package id.xms.ohscaler.egg.core.ai

enum class ExportFormat {
    PNG,
    WEBP,
    JPEG
}

data class TuningConfig(
    val scaleFactor: Float = 2.0f,
    val sharpness: Float = 0.5f,
    val antiAliasing: Float = 0.5f,
    val deblurRadius: Float = 6.0f,
    val clarityStrength: Float = 0.8f,
    val antiHaloClamp: Float = 30.0f,
    val preDeblurEnabled: Boolean = false,
    val faceAwareEnabled: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.PNG,
    val exportQuality: Int = 95
)
