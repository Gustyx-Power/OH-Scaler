package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhotoFilter
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.DarkBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainer
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainerHigh
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.EmeraldSuccess
import id.xms.ohscaler.egg.core.designsystem.LightBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightSurface
import id.xms.ohscaler.egg.core.designsystem.LightSurfaceContainerHigh
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary

@Composable
fun ProcessingProgressCard(
    progress: UpscaleProgress,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val currentStep = when (progress) {
        is UpscaleProgress.ReadingPixels -> 1
        is UpscaleProgress.Processing -> 2
        is UpscaleProgress.Stitching -> 3
        is UpscaleProgress.Completed -> 4
        else -> 0
    }

    val progressFraction = when (progress) {
        is UpscaleProgress.ReadingPixels -> 0.1f
        is UpscaleProgress.Processing -> 0.1f + (progress.progressFloat * 0.75f)
        is UpscaleProgress.Stitching -> 0.9f
        is UpscaleProgress.Completed -> 1.0f
        else -> 0.0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 300),
        label = "progressAnimation"
    )

    val cardBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val cardBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val surfaceVar = if (isDarkTheme) DarkSurfaceContainerHigh else LightSurfaceContainerHigh
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary
    val textPrimaryColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondaryColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textMutedColor = if (isDarkTheme) DarkTextMuted else LightTextMuted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(26.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with AI Sparkle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(surfaceVar)
                    .border(1.dp, primaryAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_ai_sparkle_custom),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.processing_title),
                color = textPrimaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.processing_subtitle),
                color = textSecondaryColor,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = primaryAccent,
                trackColor = cardBorder
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage & Tile status text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val percentageInt = (animatedProgress * 100).toInt()
                Text(
                    text = "$percentageInt%",
                    color = primaryAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (progress is UpscaleProgress.Processing) {
                    Text(
                        text = stringResource(
                            id.xms.ohscaler.egg.core.designsystem.R.string.tile_counter,
                            progress.currentTile,
                            progress.totalTiles
                        ),
                        color = textMutedColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4-step pipeline list
            StepItem(
                stepIndex = 1,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_read_pixels),
                icon = Icons.Rounded.PhotoFilter,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(10.dp))
            StepItem(
                stepIndex = 2,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_ai_inference),
                icon = Icons.Rounded.Memory,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(10.dp))
            StepItem(
                stepIndex = 3,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_stitching),
                icon = Icons.Rounded.Tune,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(10.dp))
            StepItem(
                stepIndex = 4,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_completed),
                icon = Icons.Rounded.CheckCircle,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, cardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textSecondaryColor)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_cancel),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StepItem(
    stepIndex: Int,
    currentStep: Int,
    title: String,
    icon: ImageVector,
    isDarkTheme: Boolean
) {
    val isDone = currentStep > stepIndex
    val isCurrent = currentStep == stepIndex

    val bgTint = when {
        isDone -> EmeraldSuccess
        isCurrent -> if (isDarkTheme) DarkPrimary else LightPrimary
        else -> if (isDarkTheme) DarkBorder else LightBorder
    }

    val textColor = when {
        isDone -> EmeraldSuccess
        isCurrent -> if (isDarkTheme) DarkTextPrimary else LightTextPrimary
        else -> if (isDarkTheme) DarkTextMuted else LightTextMuted
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(bgTint.copy(alpha = 0.2f))
                .border(1.dp, bgTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (isDarkTheme) DarkPrimary else LightPrimary
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = bgTint,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
