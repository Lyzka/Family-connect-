package com.example.ui.webgl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.render3d.drawHolographicCenterpieceAndParticles
import com.example.ui.render3d.drawPolyhedron3D
import com.example.ui.render3d.drawSciFiCorridorBackground
import com.example.ui.theme.HoloBlack
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloGold
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily

/**
 * Pure native Compose 3D WebGL-style interactive viewport (replaces Android WebView
 * to completely eliminate Chromium renderer crashes in emulator environments).
 */
@Composable
fun HoloThreeJsWebView(
    selectedContactId: String,
    centerStatusText: String,
    timeSec: Float,
    modifier: Modifier = Modifier
) {
    var yaw by remember { mutableFloatStateOf(0.4f) }
    var pitch by remember { mutableFloatStateOf(-0.15f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HoloBlack)
            .testTag("threejs_webview")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        yaw += dragAmount.x * 0.008f
                        pitch = (pitch + dragAmount.y * 0.006f).coerceIn(-0.8f, 0.8f)
                    }
                }
        ) {
            drawSciFiCorridorBackground()
            drawHolographicCenterpieceAndParticles(
                timeSec = timeSec,
                yawOffset = yaw,
                pitchOffset = pitch,
                isDisconnected = false,
                isSecure = true,
                pulseEnergy = 1.35f
            )
            // Extra nested wireframe Icosahedron overlay in 3D Mesh Inspection mode
            drawPolyhedron3D(
                cx = size.width * 0.5f,
                cy = size.height * 0.46f,
                radius = size.minDimension * 0.30f,
                rotX = timeSec * 0.3f + pitch,
                rotY = -timeSec * 0.45f + yaw,
                rotZ = timeSec * 0.2f,
                edgeColor = HoloCyanBright.copy(alpha = 0.65f),
                vertexColor = HoloWhite,
                hasInnerCore = false
            )
            // Coordinate axes
            drawLine(
                color = HoloCyan.copy(alpha = 0.35f),
                start = Offset(0f, size.height * 0.46f),
                end = Offset(size.width, size.height * 0.46f),
                strokeWidth = 1f,
                blendMode = BlendMode.Plus
            )
            drawLine(
                color = HoloCyan.copy(alpha = 0.35f),
                start = Offset(size.width * 0.5f, 0f),
                end = Offset(size.width * 0.5f, size.height),
                strokeWidth = 1f,
                blendMode = BlendMode.Plus
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 18.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xCC031022))
                .border(1.dp, HoloCyan.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "3D WIREFRAME & ICOSAHEDRON ORBITAL INSPECTOR // $selectedContactId",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = HoloCyanBright
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$centerStatusText  •  DRAG TO ORBIT 3D SCENE (YAW: ${"%.2f".format(yaw)} | PITCH: ${"%.2f".format(pitch)})",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = HoloGold
            )
        }
    }
}

@Composable
fun HoloMcpShaderLabModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("UE6 LIQUID GLASS HLSL", "MCP UI PIPELINE JSON", "THREE.JS WEBGL SPEC")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD902060D))
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xF0051326))
                .border(1.5.dp, HoloCyan, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CYBER-TREK LIQUID GLASS // UE6 & MCP PIPELINE",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = HoloCyanBright
                    )
                    Text(
                        text = "LCARS Analytical Grid + Cyberpunk Neon Matrix + Refractive Liquid Glass",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = HoloGold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HoloCyan.copy(alpha = 0.2f))
                        .border(1.dp, HoloCyan, RoundedCornerShape(6.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = HoloWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, title ->
                    val active = selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) HoloCyan.copy(alpha = 0.28f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (active) HoloCyanBright else HoloCyan.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = title,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (active) HoloWhite else HoloCyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloBlack)
                    .border(1.dp, HoloCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val codeText = when (selectedTab) {
                    0 -> UE6_LIQUID_GLASS_HLSL
                    1 -> MCP_PIPELINE_JSON
                    else -> THREEJS_SHADER_SUMMARY
                }
                Text(
                    text = codeText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = HoloCyanBright
                )
            }
        }
    }
}

private const val UE6_LIQUID_GLASS_HLSL = """// Unreal Engine 6 - Lumen / Path-Traced Liquid Glass Holographic Shader (HLSL)
// Material Domain: Surface | Blend Mode: Translucent (Additive Dual-Lobe) | Shading: Thin Translucent
struct LiquidGlassInput {
    float2 UV;
    float3 WorldNormal;
    float3 CameraVector;
    float2 TouchRippleUV;
    float  Time;
    float3 EmissiveNeonCyan; // #00F3FF
    float3 EmissiveGoldSpark; // #FFAA00
};

float4 ComputeCyberTrekLiquidGlass(LiquidGlassInput IN, Texture2D SceneColorTex, SamplerState SceneSampler) {
    // 1. Micro-Capillary Touch Ripple Wave
    float dist = distance(IN.UV, IN.TouchRippleUV);
    float ripple = sin(dist * 64.0 - IN.Time * 8.5) * exp(-dist * 9.0) * 0.018;

    // 2. Dynamic Index of Refraction (IOR = 1.52 with RGB Chromatic Aberration)
    float fresnel = pow(1.0 - saturate(dot(IN.WorldNormal, IN.CameraVector)), 3.2);
    float2 refractOffset = (IN.WorldNormal.xy + ripple) * (0.025 + fresnel * 0.04);

    float r = SceneColorTex.Sample(SceneSampler, IN.UV + refractOffset * 1.12).r;
    float g = SceneColorTex.Sample(SceneSampler, IN.UV + refractOffset * 1.00).g;
    float b = SceneColorTex.Sample(SceneSampler, IN.UV + refractOffset * 0.88).b;
    float3 refractedScene = float3(r, g, b);

    // 3. LCARS Analytical Rim & Cyber-Matrix Neon Bloom
    float3 emissiveRim = IN.EmissiveNeonCyan * (fresnel * 2.4) + IN.EmissiveGoldSpark * max(0.0, ripple * 18.0);
    return float4(refractedScene * 0.82 + emissiveRim, saturate(0.28 + fresnel * 0.65));
}"""

private const val MCP_PIPELINE_JSON = """{
  "mcp_protocol_version": "2026.2",
  "pipeline_id": "holo_comm_3d_hud_sync",
  "target_engine": "UnrealEngine6_UMG_Nanite",
  "aesthetic_pillars": {
    "base_environment": "Cyber-Matrix Dark Corridor (#02060D)",
    "interface_grid": "Star Trek LCARS 3D Perspective (rotateY +/-15deg)",
    "material_binder": "Refractive Liquid Glass (IOR 1.52, Additive Bloom)"
  },
  "nodes": [
    {
      "id": "left_panel_contacts",
      "transform_3d": { "rotateY_deg": 15.0, "perspective_px": 1100 },
      "header": { "title": "CONTACTS", "subtitle": "KONTAKTY", "accent_bar": "#00F3FF" },
      "avatars": ["USER_01_WIREFRAME_BUST", "USER_02_WIREFRAME_HOOD", "USER_03_WIREFRAME_BUST"],
      "edge_fx": "GOLD_SPARK_DISINTEGRATION_EMITTER"
    },
    {
      "id": "centerpiece_globe",
      "geometry": ["PointCloudEarth_4096", "OrbitalRings_x3", "IcosahedronGeometry_x7"],
      "status_overlay": "CONNECTING..."
    },
    {
      "id": "right_panel_telemetry",
      "transform_3d": { "rotateY_deg": -15.0, "perspective_px": 1100 },
      "modules": ["LINK_ACTIVE_BAR_WAVEFORM", "WIREFRAME_GEODESIC_SPHERE", "SIGNAL_DETECTED_OSC", "PARTICLE_TORUS"]
    },
    {
      "id": "bottom_nav_glass_bar",
      "buttons": ["CALL", "VIDEO", "SECURE", "DISCONNECT_RED"]
    }
  ]
}"""

private const val THREEJS_SHADER_SUMMARY = """// Three.js / WebGL 1:1 Scene Architecture
// - Point-Cloud Globe: THREE.Points(SphereGeometry, PointsMaterial({ color: 0x00f3ff, blending: THREE.AdditiveBlending }))
// - 3 Intersecting Orbital Rings: THREE.TorusGeometry(radius, tube, 16, 120) with custom Euler tilts
// - Floating Polyhedrons: THREE.IcosahedronGeometry(r, 0) + THREE.WireframeGeometry + inner glowing core
// - Left Panel (transform: perspective(1000px) rotateY(15deg)): CONTACTS / KONTAKTY + 3D Bust Canvases + Gold Sparks
// - Right Panel (transform: perspective(1000px) rotateY(-15deg)): LINK ACTIVE + SIGNAL DETECTED + Particle Torus
// - Bottom Bar: 4 Translucent Glassmorphic 3D Buttons (CALL, VIDEO, SECURE, DISCONNECT)"""
