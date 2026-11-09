package id.xms.ohscaler.egg.core.ai

data class TuningConfig(
    val scaleFactor: Float = 2.0f,
    val sharpness: Float = 0.5f,
    val antiAliasing: Float = 0.5f
)
