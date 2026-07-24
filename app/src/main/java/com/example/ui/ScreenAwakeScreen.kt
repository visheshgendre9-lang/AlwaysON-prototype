package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.CustomMessageCard
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidTimerRing
import com.example.ui.components.PresetSelector
import com.example.ui.theme.CyanLiquid
import com.example.ui.theme.EmeraldLiquid
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.GlassSurfaceBorder
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.IndigoLiquid
import com.example.ui.theme.RoseLiquid
import com.example.viewmodel.ScreenAwakeViewModel

@Composable
fun ScreenAwakeScreen(
    viewModel: ScreenAwakeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by viewModel.sessionState.collectAsState()
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val isInfinite by viewModel.isInfinite.collectAsState()
    val customMessage by viewModel.customMessage.collectAsState()

    // Notification Permission Handling for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Liquid background gradient animations
    val infiniteTransition = rememberInfiniteTransition(label = "bgLiquid")
    val liquidOrbOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquidOrbOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBackgroundDark)
    ) {
        // Floating Glass Liquid Ambient Orbs in Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanLiquid.copy(alpha = 0.25f), Color.Transparent)
                ),
                radius = 450f,
                center = Offset(size.width * 0.2f + liquidOrbOffset, size.height * 0.25f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(IndigoLiquid.copy(alpha = 0.3f), Color.Transparent)
                ),
                radius = 500f,
                center = Offset(size.width * 0.8f - liquidOrbOffset, size.height * 0.7f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(EmeraldLiquid.copy(alpha = 0.15f), Color.Transparent)
                ),
                radius = 350f,
                center = Offset(size.width * 0.5f, size.height * 0.5f + liquidOrbOffset)
            )
        }

        // Main Scrollable Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyanLiquid.copy(alpha = 0.3f), IndigoLiquid.copy(alpha = 0.3f))
                                )
                            )
                            .border(1.dp, GlassSurfaceBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Screen Awake Logo",
                            tint = CyanLiquid,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Screen Awake",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GlassTextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Liquid Glass Display Guard",
                            fontSize = 11.sp,
                            color = GlassTextMuted
                        )
                    }
                }

                // Stats Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, GlassSurfaceBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = CyanLiquid,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${viewModel.totalActiveMinutes}m Preserved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val canWriteSettings = remember(context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.System.canWrite(context)
                } else true
            }

            // System Timeout Permission Banner if required
            if (!canWriteSettings && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0x2206B6D4)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = CyanLiquid,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Enable System Timeout control so screen stays awake in background note apps.",
                                fontSize = 12.sp,
                                color = GlassTextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanLiquid)
                                .clickable {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Grant",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Notification Permission Banner if required
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0x33F59E0B)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Allow notifications so status message stays visible while awake.",
                                fontSize = 12.sp,
                                color = GlassTextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFBBF24))
                                .clickable {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Enable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Liquid Timer Ring Display
            LiquidTimerRing(
                session = session,
                onAdjustTime = { delta ->
                    viewModel.adjustMinutes(delta)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Big Glowing Power Button (Start / Stop)
            val buttonGlowColor = if (session.isActive) RoseLiquid else CyanLiquid
            val buttonScale by animateFloatAsState(
                targetValue = if (session.isActive) 1.02f else 1f,
                animationSpec = tween(300),
                label = "buttonScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(buttonScale)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = buttonGlowColor,
                        spotColor = buttonGlowColor
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = if (session.isActive) {
                            Brush.horizontalGradient(listOf(RoseLiquid, Color(0xFFE11D48)))
                        } else {
                            Brush.horizontalGradient(listOf(CyanLiquid, IndigoLiquid))
                        }
                    )
                    .clickable {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.toggleKeepAwake()
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Toggle Screen Keep Awake",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (session.isActive) "TURN OFF KEEP AWAKE" else "KEEP SCREEN AWAKE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preset Selection Card
            PresetSelector(
                selectedMinutes = selectedMinutes,
                isInfinite = isInfinite,
                onSelectPreset = { mins, infinite ->
                    viewModel.selectPreset(mins, infinite)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Notification Message Card
            CustomMessageCard(
                currentMessage = customMessage,
                onMessageChange = { newMessage ->
                    viewModel.updateCustomMessage(newMessage)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note-Taking & Reading Mode Tip Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x1A10B981)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldLiquid.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📝", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Perfect for Note-Taking & Reading",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTextPrimary
                        )
                        Text(
                            text = "Screen Awake holds the backlight bright while you copy notes or study without touching the glass.",
                            fontSize = 11.sp,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
