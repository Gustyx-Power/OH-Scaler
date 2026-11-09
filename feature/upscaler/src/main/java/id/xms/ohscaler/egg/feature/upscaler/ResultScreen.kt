package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import id.xms.ohscaler.egg.core.designsystem.DarkBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainer
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainerLow
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.EmeraldSuccess
import id.xms.ohscaler.egg.core.designsystem.LightBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightSurfaceContainerLow
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary
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
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val savedToastMsg = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_saved)
    val failedToastMsg = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_save_failed)

    val glassBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val glassBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val textPrimary = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textMuted = if (isDarkTheme) DarkTextMuted else LightTextMuted
    val sliderBg = if (isDarkTheme) DarkSurfaceContainer else LightSurfaceContainerLow
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Floating Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .shadow(if (isDarkTheme) 0.dp else 4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x1F1E293B))
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            ResolutionBadge(
                srcWidth = sourceBitmap.width,
                srcHeight = sourceBitmap.height,
                scaleFactor = scaleFactor,
                isDarkTheme = isDarkTheme
            )

            // Zoom Hint Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(if (isDarkTheme) 0.dp else 4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x1F1E293B))
                    .clip(RoundedCornerShape(12.dp))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ZoomIn,
                    contentDescription = null,
                    tint = primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center View: Split-Screen Comparison Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .shadow(if (isDarkTheme) 0.dp else 6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1F1E293B))
                .clip(RoundedCornerShape(20.dp))
                .background(sliderBg)
                .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
        ) {
            ComparisonSlider(
                beforeBitmap = sourceBitmap,
                afterBitmap = upscaledBitmap,
                isDarkTheme = isDarkTheme
            )
        }

        // Bottom Action Bar (Frosted Glass Container)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(if (isDarkTheme) 0.dp else 8.dp, spotColor = Color(0x1F1E293B))
                .background(glassBg)
                .border(BorderStroke(1.dp, glassBorder))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.zoom_hint),
                fontSize = 12.sp,
                color = textMuted
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textSecondary),
                    border = BorderStroke(1.dp, glassBorder)
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
                        contentColor = Color.White
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
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
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    ResultScreen(
        sourceBitmap = sourceBitmap,
        upscaledBitmap = upscaledBitmap,
        scaleFactor = scaleFactor.toFloat(),
        isSaving = isSaving,
        onSave = onSave,
        onReset = onReset,
        modifier = modifier,
        isDarkTheme = isDarkTheme
    )
}
