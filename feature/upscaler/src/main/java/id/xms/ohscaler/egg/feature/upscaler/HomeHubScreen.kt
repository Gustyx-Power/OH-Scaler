package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.AmberWarning
import id.xms.ohscaler.egg.core.designsystem.DarkAmbientBlob1
import id.xms.ohscaler.egg.core.designsystem.DarkAmbientBlob2
import id.xms.ohscaler.egg.core.designsystem.DarkBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainerLow
import id.xms.ohscaler.egg.core.designsystem.DarkTertiary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightAmbientBlob1
import id.xms.ohscaler.egg.core.designsystem.LightAmbientBlob2
import id.xms.ohscaler.egg.core.designsystem.LightBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightSecondary
import id.xms.ohscaler.egg.core.designsystem.LightSurfaceContainerLow
import id.xms.ohscaler.egg.core.designsystem.LightTertiary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary
import id.xms.ohscaler.egg.core.storage.ImageDecoderHelper
import id.xms.ohscaler.egg.core.ui.DribbbleHeroCard
import id.xms.ohscaler.egg.core.ui.DribbbleToolCard
import id.xms.ohscaler.egg.core.ui.EmptyCreationsCard
import id.xms.ohscaler.egg.core.ui.FloatingCapsuleDock

@Composable
fun HomeHubScreen(
    uiState: UpscalerUiState,
    onSelectFeature: (FeatureMode) -> Unit,
    onQuickPick: () -> Unit,
    onSelectRecent: (Uri) -> Unit,
    onOpenTutorial: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current
    var selectedTopTab by remember { mutableIntStateOf(0) } // 0 = Enhance, 1 = Creations
    var activeFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = SuperRes, 2 = Deblur, 3 = Studio

    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondaryColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val mutedColor = if (isDarkTheme) DarkTextMuted else LightTextMuted
    val glassBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val glassBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Android 17 Ambient Frosted Depth Blobs (diffuse ambient lighting behind frosted cards)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blob1 = if (isDarkTheme) DarkAmbientBlob1 else LightAmbientBlob1
            val blob2 = if (isDarkTheme) DarkAmbientBlob2 else LightAmbientBlob2
            drawCircle(
                color = blob1,
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.9f, size.height * 0.15f)
            )
            drawCircle(
                color = blob2,
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.1f, size.height * 0.65f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // ================================================================
                // Top Bar: Logo + Capsule Segmented Switch (Enhance / Creations) + Theme Toggle
                // ================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brand Logo Squircle
                    Box(
                        modifier = Modifier
                            .shadow(if (isDarkTheme) 0.dp else 4.dp, RoundedCornerShape(13.dp), spotColor = Color(0x1F1E293B))
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(glassBg)
                            .border(1.dp, glassBorder, RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_ai_sparkle_custom),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Center Segmented Capsule Toggle (Modeled after Android 17 pill switch)
                    Row(
                        modifier = Modifier
                            .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                            .clip(CircleShape)
                            .background(glassBg)
                            .border(1.dp, glassBorder, CircleShape)
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tab_enhance),
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tab_creations)
                        )
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTopTab == index
                            val activeBg = if (isDarkTheme) DarkPrimary else LightPrimary
                            val activeText = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                            val inactiveText = mutedColor

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) activeBg else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedTopTab = index
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeText else inactiveText
                                )
                            }
                        }
                    }

                    // Theme Switcher Button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleTheme()
                        },
                        modifier = Modifier
                            .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(glassBg)
                            .border(1.dp, glassBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_toggle_theme),
                            tint = textSecondaryColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ================================================================
                // Content Tab 0: Enhance (Home Hub)
                // ================================================================
                if (selectedTopTab == 0) {
                    // Featured Hero Card (Frosted Glass & Ambient Glow)
                    DribbbleHeroCard(
                        isEngineReady = uiState.isEngineReady,
                        onSelectPhoto = onQuickPick,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Filter Category Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val filters = listOf(
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.filter_all) to "3",
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.filter_super_res) to "8×",
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.filter_deblur) to "HD",
                            stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.filter_studio) to "Pro"
                        )

                        filters.forEachIndexed { index, (label, count) ->
                            val isSelected = activeFilter == index
                            val filterBg = if (isSelected) {
                                if (isDarkTheme) DarkPrimary else LightPrimary
                            } else {
                                glassBg
                            }
                            val filterText = if (isSelected) {
                                if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                            } else {
                                textSecondaryColor
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .shadow(if (isDarkTheme || isSelected) 0.dp else 2.dp, CircleShape, spotColor = Color(0x141E293B))
                                    .clip(CircleShape)
                                    .background(filterBg)
                                    .border(1.dp, if (isSelected) Color.Transparent else glassBorder, CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            activeFilter = index
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = filterText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = count,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) filterText.copy(alpha = 0.8f) else mutedColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tools List Cards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (activeFilter == 0 || activeFilter == 1) {
                            DribbbleToolCard(
                                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_title),
                                description = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_desc),
                                badgeText = "1.5× – 8×",
                                iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_super_resolution,
                                accentColor = if (isDarkTheme) DarkPrimary else LightPrimary,
                                onClick = { onSelectFeature(FeatureMode.SuperResolution) },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        if (activeFilter == 0 || activeFilter == 2) {
                            DribbbleToolCard(
                                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_title),
                                description = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_desc),
                                badgeText = "Laplacian",
                                iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_lens_restore,
                                accentColor = if (isDarkTheme) DarkSecondary else LightSecondary,
                                onClick = { onSelectFeature(FeatureMode.DeblurRestore) },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        if (activeFilter == 0 || activeFilter == 3) {
                            DribbbleToolCard(
                                title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_pro_studio_title),
                                description = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_pro_studio_desc),
                                badgeText = "Equalizer",
                                iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_tuning_studio_custom,
                                accentColor = if (isDarkTheme) DarkTertiary else LightTertiary,
                                onClick = { onSelectFeature(FeatureMode.ProStudio) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                // Recent Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.section_recent),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    if (uiState.recentCreations.isNotEmpty()) {
                        Text(
                            text = "${uiState.recentCreations.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.recentCreations.isEmpty()) {
                    EmptyCreationsCard(onSelectPhoto = onQuickPick)
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.recentCreations) { uri ->
                            RecentCreationThumbnail(
                                uri = uri,
                                onClick = { onSelectRecent(uri) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            } else {
                // ============================================================
                // Content Tab 1: Creations Gallery
                // ============================================================
                if (uiState.recentCreations.isEmpty()) {
                    EmptyCreationsCard(onSelectPhoto = onQuickPick)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.recentCreations.forEach { uri ->
                            RecentCreationListItem(
                                uri = uri,
                                onClick = { onSelectRecent(uri) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }

            // Bottom space so content can scroll completely above the floating dock
            Spacer(modifier = Modifier.height(110.dp))
        }

        // True Floating Capsule Dock Overlay
        FloatingCapsuleDock(
            currentTab = selectedTopTab,
            onSelectTab = { selectedTopTab = it },
            onToggleTheme = onToggleTheme,
            onOpenHelp = onOpenTutorial,
            isDarkTheme = isDarkTheme,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun RecentCreationThumbnail(
    uri: Uri,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = ImageDecoderHelper.decodeBitmapFromUri(context, uri, maxDimension = 300)
    }

    val cardBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val borderCol = if (isDarkTheme) DarkGlassBorder else LightGlassBorder

    Box(
        modifier = Modifier
            .shadow(if (isDarkTheme) 0.dp else 4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x1F1E293B))
            .size(100.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(18.dp))
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
                painter = painterResource(id = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_ai_sparkle_custom),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RecentCreationListItem(
    uri: Uri,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = ImageDecoderHelper.decodeBitmapFromUri(context, uri, maxDimension = 400)
    }

    val cardBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val borderCol = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val titleCol = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val descCol = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDarkTheme) 0.dp else 4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x1F1E293B))
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkTheme) Color(0xFF10121A) else Color(0xFFE2E8F0)),
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
                    painter = painterResource(id = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_ai_sparkle_custom),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Enhanced Creation",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = titleCol
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ready to inspect and share",
                fontSize = 12.sp,
                color = descCol
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isDarkTheme) Color(0x33FFFFFF) else Color(0x18000000))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "View",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = titleCol
            )
        }
    }
}
