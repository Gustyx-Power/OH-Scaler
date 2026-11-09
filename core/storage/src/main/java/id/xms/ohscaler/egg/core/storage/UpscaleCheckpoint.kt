package id.xms.ohscaler.egg.core.storage

data class UpscaleCheckpoint(
    val sourceUriString: String,
    val scaleFactor: Float,
    val sharpness: Float,
    val antiAliasing: Float,
    val deblurRadius: Float,
    val clarityStrength: Float,
    val faceAwareEnabled: Boolean,
    val antiHaloClamp: Float,
    val preDeblurEnabled: Boolean,
    val exportFormat: String = "PNG",
    val exportQuality: Int = 95,
    val totalTiles: Int = 0,
    val completedTiles: Int = 0,
    val featureMode: String = "SuperResolution",
    val timestamp: Long = System.currentTimeMillis()
)
