package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.AmberWarning
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.CyanGlow
import id.xms.ohscaler.egg.core.designsystem.ElectricViolet
import id.xms.ohscaler.egg.core.designsystem.ElectricVioletLight
import id.xms.ohscaler.egg.core.designsystem.EmeraldSuccess
import id.xms.ohscaler.egg.core.designsystem.SurfaceDark
import id.xms.ohscaler.egg.core.designsystem.SurfaceVariantDark
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary
import id.xms.ohscaler.egg.core.designsystem.TextSecondary
import id.xms.ohscaler.egg.core.storage.ImageDecoderHelper

@Composable
fun HomeHubScreen(
    uiState: UpscalerUiState,
    onSelectFeature: (FeatureMode) -> Unit,
    onSelectRecent: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.app_name),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.app_tagline),
                    fontSize = 12.sp,
                    color = CyanAccent
                )
            }

            // Offline Pill Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceVariantDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100% Offline",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Info & AI Engine Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(SurfaceDark, SurfaceVariantDark)
                    )
                )
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricVioletLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.home_hero_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.home_hero_desc),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Engine status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (uiState.isEngineReady) Color(0x1F10B981) else Color(0x1FFFFFFF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (uiState.isEngineReady) EmeraldSuccess else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isEngineReady) {
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.engine_status_active)
                        } else {
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.engine_status_loading)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (uiState.isEngineReady) EmeraldSuccess else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section: AI Image Tools
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.section_tools),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 1: AI Super-Resolution
        FeatureToolCard(
            title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_title),
            description = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_desc),
            badge = "1.5× – 8× Multi-Scale",
            icon = Icons.Rounded.AutoAwesome,
            accentColor = ElectricViolet,
            iconTint = ElectricVioletLight,
            onClick = { onSelectFeature(FeatureMode.SuperResolution) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 2: Photo Deblur & Restore
        FeatureToolCard(
            title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_title),
            description = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_desc),
            badge = "Laplacian Edge Boost",
            icon = Icons.Rounded.AutoFixHigh,
            accentColor = CyanAccent,
            iconTint = CyanGlow,
            onClick = { onSelectFeature(FeatureMode.DeblurRestore) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 3: Pro Tuning Studio
        FeatureToolCard(
            title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_pro_studio_title),
            description = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_pro_studio_desc),
            badge = "Anti-Aliasing & Sharpness",
            icon = Icons.Rounded.Tune,
            accentColor = AmberWarning,
            iconTint = AmberWarning,
            onClick = { onSelectFeature(FeatureMode.ProStudio) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Section: Recent Creations
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.section_recent),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.recentCreations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Collections,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.recent_empty),
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentCreations) { uri ->
                    RecentThumbnailCard(
                        uri = uri,
                        onClick = { onSelectRecent(uri) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureToolCard(
    title: String,
    description: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyanGlow
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RecentThumbnailCard(
    uri: Uri,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = ImageDecoderHelper.decodeBitmapFromUri(context, uri, maxDimension = 300)
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = bitmapState.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Collections,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
