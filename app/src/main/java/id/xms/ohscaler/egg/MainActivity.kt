package id.xms.ohscaler.egg

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.ohscaler.egg.core.designsystem.OHScalerTheme
import id.xms.ohscaler.egg.feature.upscaler.HomeScreen
import id.xms.ohscaler.egg.feature.upscaler.UpscalerViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: UpscalerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OHScalerTheme {
                viewModel = viewModel()
                val incomingUri = extractUri(intent)
                android.util.Log.d("OHScaler", "onCreate incomingUri: $incomingUri")
                LaunchedEffect(incomingUri) {
                    if (incomingUri != null) {
                        viewModel.onImageSelected(incomingUri)
                    }
                }
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val incomingUri = extractUri(intent)
        if (incomingUri != null && ::viewModel.isInitialized) {
            viewModel.onImageSelected(incomingUri)
        }
    }

    private fun extractUri(intent: Intent?): Uri? {
        return intent?.data ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }
}
