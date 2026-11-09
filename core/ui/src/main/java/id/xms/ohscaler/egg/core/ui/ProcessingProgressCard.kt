package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.ElectricViolet
import id.xms.ohscaler.egg.core.designsystem.EmeraldSuccess
import id.xms.ohscaler.egg.core.designsystem.SurfaceDark
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary
import id.xms.ohscaler.egg.core.designsystem.TextSecondary

@Composable
fun ProcessingProgressCard(
    progress: UpscaleProgress,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.processing_title),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.processing_subtitle),
                color = TextSecondary,
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
                color = ElectricViolet,
                trackColor = BorderDark
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
                    color = CyanAccent,
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
                        color = TextMuted,
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
                icon = Icons.Rounded.PhotoFilter
            )
            Spacer(modifier = Modifier.height(10.dp))
            StepItem(
                stepIndex = 2,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_ai_inference),
                icon = Icons.Rounded.Memory
            )
            Spacer(modifier = Modifier.height(10.dp))
            StepItem(
                stepIndex = 3,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_stitching),
                icon = Icons.Rounded.Tune
            )
            Spacer(modifier = Modifier.height(10.dp))
            StepItem(
                stepIndex = 4,
                currentStep = currentStep,
                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.step_completed),
                icon = Icons.Rounded.CheckCircle
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
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
    icon: ImageVector
) {
    val isDone = currentStep > stepIndex
    val isCurrent = currentStep == stepIndex

    val bgTint = when {
        isDone -> EmeraldSuccess
        isCurrent -> ElectricViolet
        else -> BorderDark
    }

    val textColor = when {
        isDone -> EmeraldSuccess
        isCurrent -> TextPrimary
        else -> TextMuted
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
                    color = ElectricViolet
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
