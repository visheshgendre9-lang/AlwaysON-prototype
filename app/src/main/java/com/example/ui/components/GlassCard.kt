package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassSurfaceBorder
import com.example.ui.theme.GlassSurfaceDark

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = GlassSurfaceBorder,
    glowColor: Color = Color.Transparent,
    backgroundColor: Color = GlassSurfaceDark,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = if (glowColor != Color.Transparent) 16.dp else 8.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.45f),
                        backgroundColor.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.7f),
                        borderColor.copy(alpha = 0.15f),
                        borderColor.copy(alpha = 0.4f)
                    )
                ),
                shape = shape
            )
            .then(clickableModifier)
            .padding(16.dp),
        content = content
    )
}
