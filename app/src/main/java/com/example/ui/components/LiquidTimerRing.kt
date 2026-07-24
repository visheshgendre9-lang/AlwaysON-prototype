package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeepAwakeSession
import com.example.ui.theme.CyanLiquid
import com.example.ui.theme.EmeraldLiquid
import com.example.ui.theme.GlassSurfaceBorder
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.IndigoLiquid

@Composable
fun LiquidTimerRing(
    session: KeepAwakeSession,
    onAdjustTime: (deltaMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (session.isInfinite) 1f else session.progressRatio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "liquidPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateAngle"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .scale(if (session.isActive) pulseScale else 1f),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glass Ring Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLx = (size.width - diameter) / 2
            val topLy = (size.height - diameter) / 2

            // Track background
            drawArc(
                color = Color(0x22FFFFFF),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLx, topLy),
                size = androidx.compose.ui.geometry.Size(diameter, diameter)
            )

            // Animated Active Sweep Arc
            val activeBrush = Brush.sweepGradient(
                colors = if (session.isActive) {
                    listOf(CyanLiquid, IndigoLiquid, EmeraldLiquid, CyanLiquid)
                } else {
                    listOf(Color(0x6638BDF8), Color(0x3338BDF8), Color(0x6638BDF8))
                }
            )

            val sweep = if (session.isInfinite) 360f else (animatedProgress * 360f)

            drawArc(
                brush = activeBrush,
                startAngle = -90f + if (session.isInfinite && session.isActive) rotateAngle else 0f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLx, topLy),
                size = androidx.compose.ui.geometry.Size(diameter, diameter)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (session.isActive) EmeraldLiquid.copy(alpha = 0.2f) else GlassSurfaceBorder.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (session.isActive) EmeraldLiquid.copy(alpha = 0.6f) else GlassSurfaceBorder.copy(alpha = 0.3f),
                        RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (session.isActive) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = null,
                        tint = if (session.isActive) EmeraldLiquid else GlassTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (session.isActive) "SCREEN LOCKED ON" else "READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (session.isActive) EmeraldLiquid else GlassTextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Time Display / Infinite Icon
            if (session.isInfinite) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AllInclusive,
                        contentDescription = "Infinite Always On",
                        tint = CyanLiquid,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = "ALWAYS ON",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanLiquid,
                    letterSpacing = 1.5.sp
                )
            } else {
                Text(
                    text = session.formattedRemainingTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTextPrimary,
                    letterSpacing = (-1).sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (session.isInfinite) "No screen timeout" else "Preset: ${session.presetName}",
                fontSize = 12.sp,
                color = GlassTextSecondary
            )

            // Quick adjustment controls when not infinite
            if (!session.isInfinite) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { onAdjustTime(-5) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Subtract 5 mins",
                            tint = GlassTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "± 5m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlassTextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onAdjustTime(5) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add 5 mins",
                            tint = GlassTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
