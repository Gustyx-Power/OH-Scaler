package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.ElectricViolet
import id.xms.ohscaler.egg.core.designsystem.EmeraldSuccess
import id.xms.ohscaler.egg.core.designsystem.ObsidianBlack
import id.xms.ohscaler.egg.core.designsystem.SurfaceDark
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary
import id.xms.ohscaler.egg.core.designsystem.TextSecondary
import id.xms.ohscaler.egg.core.ui.ComparisonSlider
import id.xms.ohscaler.egg.core.ui.ResolutionBadge

@Composable
fun ResultScreen(
    sourceBitmap: Bitmap,
    upscaledBitmap: Bitmap,
    scaleFactor: Float,
    isSaving: Boolean,
    onSave: ((Boolean) -> Unit) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedToastMsg = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_saved)
    val failedToastMsg = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_save_failed)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }

            ResolutionBadge(
                srcWidth = sourceBitmap.width,
                srcHeight = sourceBitmap.height,
                scaleFactor = scaleFactor
            )

            // Zoom Hint Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ZoomIn,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Center View: Split-Screen Comparison Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
        ) {
            ComparisonSlider(
                beforeBitmap = sourceBitmap,
                afterBitmap = upscaledBitmap
            )
        }

        // Bottom Action Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.zoom_hint),
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_upscale_another),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        onSave { success ->
                            val msg = if (success) savedToastMsg else failedToastMsg
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = TextPrimary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TextPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_save_gallery),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    sourceBitmap: Bitmap,
    upscaledBitmap: Bitmap,
    scaleFactor: Int,
    isSaving: Boolean,
    onSave: ((Boolean) -> Unit) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    ResultScreen(
        sourceBitmap = sourceBitmap,
        upscaledBitmap = upscaledBitmap,
        scaleFactor = scaleFactor.toFloat(),
        isSaving = isSaving,
        onSave = onSave,
        onReset = onReset,
        modifier = modifier
    )
}

