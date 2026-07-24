package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DurationPreset
import com.example.ui.theme.CyanLiquid
import com.example.ui.theme.GlassSurfaceBorder
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.IndigoLiquid

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetSelector(
    selectedMinutes: Int,
    isInfinite: Boolean,
    onSelectPreset: (minutes: Int, isInfinite: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomSlider by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = GlassSurfaceDark
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT DURATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTextSecondary,
                    letterSpacing = 1.2.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCustomSlider = !showCustomSlider }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Custom Duration",
                        tint = if (showCustomSlider) CyanLiquid else GlassTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showCustomSlider) "Custom Active" else "Custom Slider",
                        fontSize = 12.sp,
                        color = if (showCustomSlider) CyanLiquid else GlassTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Presets FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DurationPreset.entries.forEach { preset ->
                    val isSelected = if (preset.isInfinite) {
                        isInfinite
                    } else {
                        !isInfinite && selectedMinutes == preset.minutes && !showCustomSlider
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = if (isSelected) {
                                    Brush.horizontalGradient(listOf(CyanLiquid, IndigoLiquid))
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0x1AFFFFFF), Color(0x0DFFFFFF))
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyanLiquid else GlassSurfaceBorder.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                showCustomSlider = false
                                onSelectPreset(preset.minutes, preset.isInfinite)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (preset.isInfinite) {
                                Icon(
                                    imageVector = Icons.Rounded.AllInclusive,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else GlassTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = preset.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else GlassTextPrimary
                            )
                        }
                    }
                }
            }

            // Custom Slider Drawer
            AnimatedVisibility(visible = showCustomSlider) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22000000))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Timer:",
                            fontSize = 13.sp,
                            color = GlassTextSecondary
                        )
                        Text(
                            text = "$selectedMinutes Minutes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanLiquid
                        )
                    }

                    Slider(
                        value = selectedMinutes.coerceIn(1, 240).toFloat(),
                        onValueChange = { newValue ->
                            onSelectPreset(newValue.toInt(), false)
                        },
                        valueRange = 1f..240f,
                        steps = 239,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanLiquid,
                            activeTrackColor = CyanLiquid,
                            inactiveTrackColor = GlassSurfaceBorder
                        )
                    )
                }
            }
        }
    }
}
