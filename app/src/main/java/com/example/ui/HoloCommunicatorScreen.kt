package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.HoloViewMode
import com.example.ui.chat.HoloAiChatOverlay
import com.example.ui.chat.HoloAiChatViewModel
import com.example.ui.components.HoloBottomNavBar
import com.example.ui.components.HoloFlowingParticleField
import com.example.ui.components.HoloLeftContactsPanel
import com.example.ui.components.HoloRightTelemetryPanel
import com.example.ui.render3d.drawHolographicCenterpieceAndParticles
import com.example.ui.render3d.drawSciFiCorridorBackground
import com.example.ui.theme.HoloBlack
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloGold
import com.example.ui.theme.HoloGoldBright
import com.example.ui.theme.HoloRed
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.webgl.HoloMcpShaderLabModal
import com.example.ui.webgl.HoloThreeJsWebView
import com.example.util.CameraPermissionHelper
import com.example.util.rememberCameraPermissionRequester

@Composable
fun HoloCommunicatorScreen(
    modifier: Modifier = Modifier,
    hudViewModel: HudViewModel = viewModel(),
    aiChatViewModel: HoloAiChatViewModel = viewModel()
) {
    val uiState by hudViewModel.uiState.collectAsStateWithLifecycle()
    val chatState by aiChatViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraPermissionRequester = rememberCameraPermissionRequester { isGranted ->
        hudViewModel.onCameraPermissionResult(isGranted = isGranted, activateVideoOnGrant = true)
    }

    LaunchedEffect(context) {
        if (CameraPermissionHelper.hasCameraPermission(context)) {
            hudViewModel.onCameraPermissionResult(isGranted = true, activateVideoOnGrant = false)
        }
    }

    // Handle back navigation when inside AI Chat, 3D Mesh, or MCP Shader Lab sub-modes
    if (uiState.viewMode != HoloViewMode.NATIVE_3D_HUD) {
        BackHandler {
            hudViewModel.setViewMode(HoloViewMode.NATIVE_3D_HUD)
        }
    }

    // Pause heavy background 3D loops while full-screen modal overlays (AI_CHAT / MCP_SHADER_LAB) are active
    val isModalOverlayOpen = uiState.viewMode == HoloViewMode.AI_CHAT ||
        uiState.viewMode == HoloViewMode.MCP_SHADER_LAB

    var timeSec by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isModalOverlayOpen) {
        if (!isModalOverlayOpen) {
            val startMs = android.os.SystemClock.uptimeMillis() - (timeSec * 1000f).toLong()
            while (true) {
                kotlinx.coroutines.delay(33L)
                timeSec = (android.os.SystemClock.uptimeMillis() - startMs) / 1000f
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(HoloBlack)
            .testTag("holo_communicator_root")
    ) {
        val isCompactHeight = maxHeight < 430.dp
        val isPortraitLayout = maxWidth < maxHeight

        // Layer 1A: Full-Bleed 3D Sci-Fi Corridor, 3D Globe, Orbital Rings & Icosahedrons
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        hudViewModel.onGlobeDrag(dragAmount.x, dragAmount.y)
                    }
                }
        ) {
            drawSciFiCorridorBackground()
            drawHolographicCenterpieceAndParticles(
                timeSec = timeSec,
                yawOffset = uiState.globeYawOffset,
                pitchOffset = uiState.globePitchOffset,
                isDisconnected = uiState.isDisconnected,
                isSecure = uiState.isSecureActive,
                pulseEnergy = uiState.pulseEnergy
            )
        }

        // Layer 1B: Dedicated Flowing & Drifting Cyan Nano-Particle Atmosphere Composable
        if (!isModalOverlayOpen) {
            HoloFlowingParticleField(
                modifier = Modifier.fillMaxSize(),
                flowIntensity = uiState.pulseEnergy,
                isAlertRed = uiState.isDisconnected
            )
        }

        // Layer 2: Active Mode Content
        if (uiState.viewMode == HoloViewMode.THREEJS_WEBGL) {
            HoloThreeJsWebView(
                selectedContactId = uiState.selectedContactId,
                centerStatusText = uiState.centerStatusText,
                timeSec = timeSec,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Default: 1:1 3D Perspective HUD Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        horizontal = if (isPortraitLayout) 8.dp else 18.dp,
                        vertical = if (isCompactHeight) 6.dp else 12.dp
                    )
            ) {
                // Main 3-Column Perspective Stage (Left Panel + Centerpiece Text + Right Panel)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(if (isCompactHeight) 0.78f else 0.80f)
                        .align(Alignment.TopCenter)
                ) {
                    // Left Panel: CONTACTS / KONTAKTY + 3D Wireframe Bust Avatars + Gold Sparks
                    HoloLeftContactsPanel(
                        contacts = uiState.contacts,
                        selectedContactId = uiState.selectedContactId,
                        timeSec = timeSec,
                        isCompactHeight = isCompactHeight,
                        onSelectContact = { contactId ->
                            hudViewModel.selectContact(contactId)
                            aiChatViewModel.selectContact(contactId)
                        },
                        modifier = Modifier
                            .weight(if (isPortraitLayout) 0.33f else 0.27f)
                            .fillMaxHeight()
                    )

                    // Centerpiece: Glowing white sans-serif text "CONNECTING..." across the middle of the 3D globe
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(if (isPortraitLayout) 0.34f else 0.46f)
                            .fillMaxHeight()
                    ) {
                        val glowColor = if (uiState.isDisconnected) HoloRed else HoloCyan
                        Box(contentAlignment = Alignment.Center) {
                            // Outer soft neon bloom behind text
                            Text(
                                text = uiState.centerStatusText,
                                style = TextStyle(
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isPortraitLayout) 18.sp else 30.sp,
                                    letterSpacing = if (isPortraitLayout) 2.sp else 4.sp,
                                    color = glowColor.copy(alpha = 0.55f),
                                    shadow = Shadow(
                                        color = glowColor,
                                        offset = Offset.Zero,
                                        blurRadius = 32f
                                    )
                                ),
                                textAlign = TextAlign.Center
                            )
                            // Crisp white core text with bright cyan halo
                            Text(
                                text = uiState.centerStatusText,
                                style = TextStyle(
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isPortraitLayout) 18.sp else 30.sp,
                                    letterSpacing = if (isPortraitLayout) 2.sp else 4.sp,
                                    color = HoloWhite,
                                    shadow = Shadow(
                                        color = glowColor,
                                        offset = Offset.Zero,
                                        blurRadius = 16f
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("center_status_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick-launch Holographic Gemini AI Comms pill below "CONNECTING..."
                        Box(
                            modifier = Modifier
                                .testTag("open_ai_comms_button")
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xB303152B))
                                .border(1.dp, HoloCyanBright.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                                .clickable {
                                    aiChatViewModel.selectContact(uiState.selectedContactId)
                                    hudViewModel.setViewMode(HoloViewMode.AI_CHAT)
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${stringResource(R.string.open_ai_comms)} [${uiState.selectedContactId}]",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isPortraitLayout) 8.sp else 10.sp,
                                letterSpacing = 1.4.sp,
                                color = HoloCyanBright
                            )
                        }
                    }

                    // Right Panel: LINK ACTIVE + SIGNAL DETECTED + Bottom Telemetry & Particle Torus
                    HoloRightTelemetryPanel(
                        timeSec = timeSec,
                        isCallActive = uiState.isCallActive,
                        isDisconnected = uiState.isDisconnected,
                        isCompactHeight = isCompactHeight,
                        modifier = Modifier
                            .weight(if (isPortraitLayout) 0.33f else 0.27f)
                            .fillMaxHeight()
                    )
                }

                // Bottom Navigation Bar: 4 Floating Translucent 3D Square Glass Buttons
                HoloBottomNavBar(
                    isCallActive = uiState.isCallActive,
                    isVideoActive = uiState.isVideoActive,
                    isSecureActive = uiState.isSecureActive,
                    isDisconnected = uiState.isDisconnected,
                    isCompactHeight = isCompactHeight,
                    onCallClick = hudViewModel::toggleCall,
                    onVideoClick = {
                        if (uiState.isVideoActive) {
                            hudViewModel.toggleVideo()
                        } else {
                            cameraPermissionRequester.ensureCameraPermission()
                        }
                    },
                    onSecureClick = hudViewModel::toggleSecure,
                    onDisconnectClick = hudViewModel::disconnectOrReconnect,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // Top-Center Cyber-Trek Mode Switcher Pill (3D HUD | AI COMMS | 3D MESH | MCP/HLSL | RE-ALIGN)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x99031022))
                .border(0.8.dp, HoloCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            HudModeChip(
                label = stringResource(R.string.mode_native_3d),
                selected = uiState.viewMode == HoloViewMode.NATIVE_3D_HUD,
                tag = "mode_3d_hud",
                onClick = { hudViewModel.setViewMode(HoloViewMode.NATIVE_3D_HUD) }
            )
            HudModeChip(
                label = stringResource(R.string.mode_ai_chat),
                selected = uiState.viewMode == HoloViewMode.AI_CHAT,
                tag = "mode_ai_chat",
                onClick = {
                    aiChatViewModel.selectContact(uiState.selectedContactId)
                    hudViewModel.setViewMode(HoloViewMode.AI_CHAT)
                }
            )
            HudModeChip(
                label = stringResource(R.string.mode_webgl),
                selected = uiState.viewMode == HoloViewMode.THREEJS_WEBGL,
                tag = "mode_threejs",
                onClick = { hudViewModel.setViewMode(HoloViewMode.THREEJS_WEBGL) }
            )
            HudModeChip(
                label = stringResource(R.string.mode_specs),
                selected = uiState.viewMode == HoloViewMode.MCP_SHADER_LAB,
                tag = "mode_mcp_shader",
                onClick = { hudViewModel.setViewMode(HoloViewMode.MCP_SHADER_LAB) }
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(HoloGold.copy(alpha = 0.18f))
                    .border(0.7.dp, HoloGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .clickable { hudViewModel.resetGlobeOrbit() }
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "RE-ALIGN",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 8.sp,
                    color = HoloGoldBright
                )
            }
        }

        // Layer 3: Multi-Turn Gemini Holographic AI Comms Overlay
        if (uiState.viewMode == HoloViewMode.AI_CHAT) {
            HoloAiChatOverlay(
                chatState = chatState,
                onSelectContact = { contactId ->
                    hudViewModel.selectContact(contactId)
                    aiChatViewModel.selectContact(contactId)
                },
                onSelectTier = aiChatViewModel::selectModelTier,
                onSendMessage = aiChatViewModel::sendMessage,
                onDismissError = aiChatViewModel::clearError,
                onClose = { hudViewModel.setViewMode(HoloViewMode.NATIVE_3D_HUD) }
            )
        }

        // Layer 4: Cyber-Trek Liquid Glass HLSL Shader & MCP Pipeline Modal Overlay
        if (uiState.viewMode == HoloViewMode.MCP_SHADER_LAB) {
            HoloMcpShaderLabModal(
                onDismiss = { hudViewModel.setViewMode(HoloViewMode.NATIVE_3D_HUD) }
            )
        }
    }
}

@Composable
private fun HudModeChip(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) HoloCyan.copy(alpha = 0.28f) else Color.Transparent)
            .border(
                width = 0.7.dp,
                color = if (selected) HoloCyanBright else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontFamily = OrbitronFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 8.sp,
            color = if (selected) HoloWhite else HoloCyanBright.copy(alpha = 0.75f)
        )
    }
}
