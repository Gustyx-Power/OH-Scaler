package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary

@Composable
fun StudioTopBar(
    title: String,
    iconRes: Int,
    onNavigateBack: () -> Unit,
    onSelectNewImage: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val glassBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val glassBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                .size(40.dp)
                .clip(CircleShape)
                .background(glassBg)
                .border(1.dp, glassBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = titleColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Mode Tag Capsule
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                .clip(CircleShape)
                .background(glassBg)
                .border(1.dp, glassBorder, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
        }

        IconButton(
            onClick = onSelectNewImage,
            modifier = Modifier
                .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                .size(40.dp)
                .clip(CircleShape)
                .background(glassBg)
                .border(1.dp, glassBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.AddPhotoAlternate,
                contentDescription = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_change_photo),
                tint = titleColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StudioViewportStage(
    sourceBitmap: Bitmap,
    scaleFactor: Float,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    customBadgeText: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val glassBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (zoomScale * zoomChange).coerceIn(1f, 5f)
        zoomScale = newScale
        if (newScale > 1f) {
            panOffset += panChange
        } else {
            panOffset = Offset.Zero
        }
    }

    val targetWidth = (sourceBitmap.width * scaleFactor).toInt()
    val targetHeight = (sourceBitmap.height * scaleFactor).toInt()
    val scaleLabel = if (scaleFactor % 1f == 0f) "${scaleFactor.toInt()}×" else "${scaleFactor}×"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .shadow(if (isDarkTheme) 0.dp else 6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x1F1E293B))
            .clip(RoundedCornerShape(22.dp))
            .background(if (isDarkTheme) Color(0xFF0F121A) else Color(0xFFE2E8F0))
            .border(1.dp, glassBorder, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (zoomScale > 1.05f) {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        } else {
                            zoomScale = 2.5f
                        }
                    }
                )
            }
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = remember(sourceBitmap) { id.xms.ohscaler.egg.core.ui.BitmapDisplayUtils.toDisplaySafeBitmap(sourceBitmap).asImageBitmap() },
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                },
            contentScale = ContentScale.Fit
        )

        // Top-Left: Original Spec Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color(0xB3151821) else Color(0xCCFFFFFF))
                .border(1.dp, if (isDarkTheme) Color(0x33FFFFFF) else Color(0x40CBD5E1), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "${sourceBitmap.width} × ${sourceBitmap.height} px • Original",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF334155)
            )
        }

        // Top-Right: Zoom Reset Pill Button
        if (zoomScale > 1.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xD9151821) else Color(0xEBFFFFFF))
                    .border(1.dp, if (isDarkTheme) Color(0x40FFFFFF) else Color(0x60CBD5E1), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ZoomOutMap,
                        contentDescription = "Reset Zoom",
                        tint = primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Reset 1×",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent
                    )
                }
            }
        }

        // Bottom-Right: Target Output Resolution Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color(0xD9151821) else Color(0xEBFFFFFF))
                .border(1.dp, if (isDarkTheme) Color(0x40FFFFFF) else Color(0x60CBD5E1), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(primaryAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = customBadgeText ?: scaleLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                    )
                }
                Text(
                    text = "Target: ${targetWidth} × ${targetHeight}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                )
            }
        }
    }
}
