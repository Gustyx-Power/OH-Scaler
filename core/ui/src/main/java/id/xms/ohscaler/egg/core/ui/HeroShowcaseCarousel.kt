package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkHeroGradient
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightHeroGradient
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary

data class CarouselSlide(
    val title: String,
    val subtitle: String,
    val tag: String,
    val imageRes: Int,
    val featureModeIndex: Int // 0: SuperRes, 1: Deblur, 2: ProStudio
)

@Composable
fun HeroShowcaseCarousel(
    isEngineReady: Boolean,
    onSelectFeatureIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { 3 })

    // Auto-scroll carousel every 4 seconds smoothly and resiliently
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % 3
                runCatching {
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    val slides = listOf(
        CarouselSlide(
            title = "Ultra 16× Super-Resolution",
            subtitle = "Dual-Pass Cascaded AI • Zero-OOM Streaming",
            tag = "16× Cascaded Neural",
            imageRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.showcase_superres,
            featureModeIndex = 0
        ),
        CarouselSlide(
            title = "Photo Deblur & Face Restore",
            subtitle = "Multi-Scale YUV • Eye & Smile Recovery",
            tag = "Multi-Scale Defocus Deblur",
            imageRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.showcase_deblur,
            featureModeIndex = 1
        ),
        CarouselSlide(
            title = "Pro Creative Workstation",
            subtitle = "Manual Anti-Aliasing (SSAA) • Lossless Export",
            tag = "SSAA De-Jaggies & Multi-Format",
            imageRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.showcase_pro,
            featureModeIndex = 2
        )
    )

    val gradientColors = if (isDarkTheme) DarkHeroGradient else LightHeroGradient
    val shadowColor = if (isDarkTheme) Color(0x33000000) else Color(0x1A1E293B)
    val borderColor = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary
    val pillBgColor = if (isDarkTheme) Color(0x2BFFFFFF) else Color(0x40CBD5E1)
    val pillTextColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val slide = slides[page]
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.98f else 1.0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                label = "carouselPress"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = shadowColor,
                        ambientColor = shadowColor
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(gradientColors))
                    .border(1.dp, borderColor, RoundedCornerShape(26.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectFeatureIndex(slide.featureModeIndex)
                        }
                    )
                    .padding(18.dp)
            ) {
                Column {
                    // Header Row: Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(pillBgColor)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = pillTextColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "100% Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = pillTextColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFFFFFFFF) else LightPrimary)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isEngineReady) "Active" else "Ready",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Before / After Split Comparison Showcase Visual
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = slide.imageRes),
                            contentDescription = slide.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Subtle Before / After Tag Pills
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xB3000000))
                                .border(1.dp, Color(0x40FFFFFF), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "BEFORE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(primaryAccent.copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "AFTER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFF0F172A) else Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = slide.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) DarkTextPrimary else LightTextPrimary,
                        letterSpacing = (-0.3).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle & Tag
                    Text(
                        text = slide.subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Row: Pager Dots + Tap Anywhere Indicator (NO RIGID BUTTON)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Pager Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { dotIndex ->
                                val isSelected = pagerState.currentPage == dotIndex
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isSelected) 22.dp else 6.dp,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "dotWidth"
                                )
                                val dotColor by animateColorAsState(
                                    targetValue = if (isSelected) primaryAccent else if (isDarkTheme) Color(0x40FFFFFF) else Color(0x30000000),
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "dotColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(dotWidth)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }

                        // Tap Hint Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TouchApp,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Tap to open studio",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
