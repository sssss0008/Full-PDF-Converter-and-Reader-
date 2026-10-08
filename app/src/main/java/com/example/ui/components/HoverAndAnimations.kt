package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight

/**
 * Interactive hover + press modifier that gives fluid spring scaling,
 * elevation lift, glowing neon border gradients, and pointer event tracking
 * across touch, mouse, trackpad, and stylus devices.
 */
fun Modifier.interactiveHoverEffect(
    scaleOnHover: Float = 1.035f,
    scaleOnPress: Float = 0.97f,
    glowColor: Color = NeonIndigo,
    enabledGlow: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val src = interactionSource ?: remember { MutableInteractionSource() }
    val isHoveredBySource by src.collectIsHoveredAsState()
    val isPressed by src.collectIsPressedAsState()
    var isPointerHovered by remember { mutableStateOf(false) }

    val isHovered = isHoveredBySource || isPointerHovered

    val targetScale = when {
        isPressed -> scaleOnPress
        isHovered -> scaleOnHover
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "interactive_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.9f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "interactive_glow"
    )

    val elevationDp by animateDpAsState(
        targetValue = if (isHovered) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "interactive_elevation"
    )

    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when (event.type) {
                        PointerEventType.Enter, PointerEventType.Move -> {
                            isPointerHovered = true
                        }
                        PointerEventType.Exit -> {
                            isPointerHovered = false
                        }
                    }
                }
            }
        }
        .hoverable(interactionSource = src)
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            shadowElevation = elevationDp.toPx()
        }
        .then(
            if (enabledGlow && glowAlpha > 0.05f) {
                Modifier.border(
                    BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(
                                glowColor.copy(alpha = glowAlpha),
                                ElectricCyan.copy(alpha = glowAlpha)
                            )
                        )
                    ),
                    shape = shape
                )
            } else {
                Modifier
            }
        )
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = src,
                    indication = ripple(),
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
}

/**
 * Subtle breathing neon border that pulses slowly to attract attention.
 */
fun Modifier.ambientBreathingBorder(
    baseColor: Color = NeonIndigo,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_border")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    this.border(
        BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    baseColor.copy(alpha = alpha),
                    ElectricCyan.copy(alpha = alpha * 0.8f)
                )
            )
        ),
        shape = shape
    )
}

/**
 * Shimmer effect that sweeps across a component horizontally.
 */
fun Modifier.shimmerHighlight(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    this.drawBehind {
        val width = size.width
        val shimmerWidth = width * 0.6f
        val startX = (width + shimmerWidth) * progress - shimmerWidth

        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.12f),
                Color.Transparent
            ),
            start = Offset(startX, 0f),
            end = Offset(startX + shimmerWidth, size.height)
        )
        drawRect(brush)
    }
}

/**
 * Modern Pulsing Dot Status Badge to communicate live status and hook users.
 */
@Composable
fun PulsingStatusBadge(
    text: String,
    color: Color = EmeraldSuccess,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_badge")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = dotAlpha
                    }
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

/**
 * Trust & Competitive Advantage Hook Banner
 * Directly addresses user perception vs competition (Adobe, Smallpdf, PDF Expert).
 */
@Composable
fun FeatureTrustBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .ambientBreathingBorder(NeonIndigo, RoundedCornerShape(16.dp)),
        color = ObsidianSurfaceHighlight.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "THE ULTIMATE PDF ENGINE",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                PulsingStatusBadge(text = "UNLIMITED & FREE", color = EmeraldSuccess)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Enterprise-Grade Power in Your Pocket",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Why choose us over competitors? 100% on-device local execution means zero waiting for file uploads, zero paywalls, zero subscription traps, and total data confidentiality.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Stat Hook Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HookStatPill(
                    icon = Icons.Default.Bolt,
                    label = "0ms Server Lag",
                    tint = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                HookStatPill(
                    icon = Icons.Default.Shield,
                    label = "100% Private",
                    tint = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                HookStatPill(
                    icon = Icons.Default.AutoAwesome,
                    label = "33 Pro Tools",
                    tint = NeonIndigo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HookStatPill(
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
