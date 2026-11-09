package id.xms.ohscaler.egg.core.storage

import android.content.Context
import org.json.JSONObject
import java.io.File

class UpscaleRecoveryManager(private val context: Context) {

    private val checkpointFile: File
        get() = File(context.noBackupFilesDir, "upscale_checkpoint.json")

    fun saveCheckpoint(checkpoint: UpscaleCheckpoint) {
        try {
            val json = JSONObject().apply {
                put("sourceUriString", checkpoint.sourceUriString)
                put("scaleFactor", checkpoint.scaleFactor.toDouble())
                put("sharpness", checkpoint.sharpness.toDouble())
                put("antiAliasing", checkpoint.antiAliasing.toDouble())
                put("deblurRadius", checkpoint.deblurRadius.toDouble())
                put("clarityStrength", checkpoint.clarityStrength.toDouble())
                put("faceAwareEnabled", checkpoint.faceAwareEnabled)
                put("antiHaloClamp", checkpoint.antiHaloClamp.toDouble())
                put("preDeblurEnabled", checkpoint.preDeblurEnabled)
                put("exportFormat", checkpoint.exportFormat)
                put("exportQuality", checkpoint.exportQuality)
                put("totalTiles", checkpoint.totalTiles)
                put("completedTiles", checkpoint.completedTiles)
                put("featureMode", checkpoint.featureMode)
                put("timestamp", checkpoint.timestamp)
            }
            checkpointFile.writeText(json.toString())
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Failed to save upscale checkpoint", e)
        }
    }

    fun updateTileProgress(completedTiles: Int, totalTiles: Int) {
        try {
            val current = getPendingCheckpoint() ?: return
            val updated = current.copy(completedTiles = completedTiles, totalTiles = totalTiles)
            saveCheckpoint(updated)
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Failed to update tile progress", e)
        }
    }

    fun getPendingCheckpoint(): UpscaleCheckpoint? {
        return try {
            if (!checkpointFile.exists()) return null
            val text = checkpointFile.readText()
            val json = JSONObject(text)

            val timestamp = json.optLong("timestamp", 0L)
            val now = System.currentTimeMillis()
            // Auto-expire checkpoints older than 24 hours (86,400,000 ms)
            if (now - timestamp > 86_400_000L) {
                clearCheckpoint()
                return null
            }

            UpscaleCheckpoint(
                sourceUriString = json.getString("sourceUriString"),
                scaleFactor = json.optDouble("scaleFactor", 2.0).toFloat(),
                sharpness = json.optDouble("sharpness", 0.5).toFloat(),
                antiAliasing = json.optDouble("antiAliasing", 0.5).toFloat(),
                deblurRadius = json.optDouble("deblurRadius", 8.0).toFloat(),
                clarityStrength = json.optDouble("clarityStrength", 0.85).toFloat(),
                faceAwareEnabled = json.optBoolean("faceAwareEnabled", true),
                antiHaloClamp = json.optDouble("antiHaloClamp", 30.0).toFloat(),
                preDeblurEnabled = json.optBoolean("preDeblurEnabled", false),
                exportFormat = json.optString("exportFormat", "PNG"),
                exportQuality = json.optInt("exportQuality", 95),
                totalTiles = json.optInt("totalTiles", 0),
                completedTiles = json.optInt("completedTiles", 0),
                featureMode = json.optString("featureMode", "SuperResolution"),
                timestamp = timestamp
            )
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Failed to read checkpoint", e)
            null
        }
    }

    fun clearCheckpoint() {
        try {
            if (checkpointFile.exists()) {
                checkpointFile.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Failed to clear checkpoint", e)
        }
    }
}
