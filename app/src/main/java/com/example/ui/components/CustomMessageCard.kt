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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeepAwakeSession
import com.example.ui.theme.CyanLiquid
import com.example.ui.theme.EmeraldLiquid
import com.example.ui.theme.GlassSurfaceBorder
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomMessageCard(
    currentMessage: String,
    onMessageChange: (newMessage: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(currentMessage) { mutableStateOf(currentMessage) }
    val focusManager = LocalFocusManager.current

    val messagePresets = listOf(
        "📝 Writing Notes — Keeping Screen Awake",
        "📖 Reading Documentation & E-Books",
        "💻 Coding & Debugging Session Active",
        "🍳 Recipe Mode — Screen Locked On",
        "App is running in background to help you don't turn off the screen by itself."
    )

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = CyanLiquid,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOTIFICATION MESSAGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTextSecondary,
                        letterSpacing = 1.2.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isEditing = !isEditing }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "Edit Custom Message",
                        tint = if (isEditing) CyanLiquid else GlassTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditing) "Done" else "Customize",
                        fontSize = 12.sp,
                        color = if (isEditing) CyanLiquid else GlassTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Preview Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x330F172A))
                    .border(1.dp, GlassSurfaceBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanLiquid.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = CyanLiquid,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Screen Awake • Ongoing",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassTextMuted
                            )
                            Text(
                                text = "now",
                                fontSize = 10.sp,
                                color = GlassTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = currentMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlassTextPrimary
                        )
                    }
                }
            }

            // Edit Drawer
            AnimatedVisibility(visible = isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                            onMessageChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter special notification message...", color = GlassTextMuted) },
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            isEditing = false
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLiquid,
                            unfocusedBorderColor = GlassSurfaceBorder,
                            focusedTextColor = GlassTextPrimary,
                            unfocusedTextColor = GlassTextPrimary,
                            focusedContainerColor = Color(0x22000000),
                            unfocusedContainerColor = Color(0x11000000)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quick Message Presets:",
                        fontSize = 11.sp,
                        color = GlassTextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        messagePresets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x22FFFFFF))
                                    .border(1.dp, GlassSurfaceBorder.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        textValue = preset
                                        onMessageChange(preset)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    color = GlassTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Default Reset Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldLiquid.copy(alpha = 0.15f))
                                .border(1.dp, EmeraldLiquid.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    textValue = KeepAwakeSession.DEFAULT_MESSAGE
                                    onMessageChange(KeepAwakeSession.DEFAULT_MESSAGE)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = EmeraldLiquid,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset Default",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLiquid
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
