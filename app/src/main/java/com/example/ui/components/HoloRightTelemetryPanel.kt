package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.render3d.drawPolyhedron3D
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloRed
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.RajdhaniFontFamily
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HoloRightTelemetryPanel(
    timeSec: Float,
    isCallActive: Boolean,
    isDisconnected: Boolean,
    isCompactHeight: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (isDisconnected) HoloRed else HoloCyan
    val brightColor = if (isDisconnected) Color(0xFFFF9999) else HoloCyanBright

    Box(
        modifier = modifier
            .testTag("right_telemetry_panel")
            .graphicsLayer {
                rotationY = -15f
                cameraDistance = 14f * density
            }
            .drawBehind {
                val w = size.width
                val h = size.height

                // Outer translucent glass backing
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.06f),
                            Color(0xFF031022).copy(alpha = 0.32f)
                        )
                    ),
                    topLeft = Offset(w * 0.06f, 8f),
                    size = Size(w * 0.94f, h - 16f)
                )

                // Top-left chamfered HUD corner line extending right
                val cornerPath = Path().apply {
                    moveTo(0f, h * 0.09f)
                    lineTo(w * 0.09f, 4f)
                    lineTo(w, 0f)
                }
                drawPath(
                    path = cornerPath,
                    color = primaryColor.copy(alpha = 0.85f),
                    style = Stroke(width = 1.8f),
                    blendMode = BlendMode.Plus
                )

                // Inner parallel top border line
                drawLine(
                    color = primaryColor.copy(alpha = 0.5f),
                    start = Offset(w * 0.06f, 16f),
                    end = Offset(w * 0.98f, 10f),
                    strokeWidth = 1.2f
                )

                // Left vertical framing border line
                drawLine(
                    color = primaryColor.copy(alpha = 0.72f),
                    start = Offset(w * 0.06f, 16f),
                    end = Offset(w * 0.06f, h * 0.97f),
                    strokeWidth = 1.6f
                )

                // Bottom horizontal border line with angled tick
                drawLine(
                    color = primaryColor.copy(alpha = 0.85f),
                    start = Offset(w * 0.04f, h * 0.97f),
                    end = Offset(w, h * 0.99f),
                    strokeWidth = 1.8f,
                    blendMode = BlendMode.Plus
                )
            }
            .padding(start = 18.dp, top = 16.dp, end = 6.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ================= TOP SECTION: Dual Audio Bar Waveform + 3D Wireframe Sphere =================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactHeight) 56.dp else 72.dp)
                ) {
                    // Left: Digital Audio Bar Waveform (2 distinct packets like the reference)
                    Canvas(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                    ) {
                        drawTopDualPacketWaveform(
                            timeSec = timeSec,
                            primaryColor = primaryColor,
                            brightColor = brightColor,
                            ampScale = if (isDisconnected) 0.2f else if (isCallActive) 1.25f else 1.0f
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: 3D Wireframe Geodesic Sphere
                    Canvas(
                        modifier = Modifier
                            .size(if (isCompactHeight) 54.dp else 66.dp)
                    ) {
                        drawPolyhedron3D(
                            cx = size.width * 0.5f,
                            cy = size.height * 0.5f,
                            radius = size.minDimension * 0.44f,
                            rotX = timeSec * 0.5f,
                            rotY = timeSec * 0.7f,
                            rotZ = timeSec * 0.3f,
                            edgeColor = primaryColor,
                            vertexColor = HoloWhite,
                            hasInnerCore = true
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.link_active),
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    letterSpacing = 1.5.sp,
                    color = HoloWhite.copy(alpha = 0.92f)
                )

                // Stepped horizontal cyan divider line underneath LINK ACTIVE
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    val w = size.width
                    val path = Path().apply {
                        moveTo(0f, 6f)
                        lineTo(w * 0.52f, 6f)
                        lineTo(w * 0.55f, 1f)
                        lineTo(w, 1f)
                    }
                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.8f),
                        style = Stroke(width = 1.4f),
                        blendMode = BlendMode.Plus
                    )
                }
            }

            // ================= MIDDLE SECTION: Wide High-Density Frequency Waveform =================
            Column(modifier = Modifier.fillMaxWidth()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactHeight) 68.dp else 88.dp)
                ) {
                    drawMiddleWideFrequencyWaveform(
                        timeSec = timeSec,
                        primaryColor = primaryColor,
                        brightColor = brightColor,
                        ampScale = if (isDisconnected) 0.18f else if (isCallActive) 1.25f else 1.0f
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.signal_detected),
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    letterSpacing = 1.5.sp,
                    color = HoloWhite.copy(alpha = 0.92f)
                )

                Spacer(modifier = Modifier.height(3.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.78f),
                        start = Offset(0f, 1f),
                        end = Offset(size.width, 1f),
                        strokeWidth = 1.4f,
                        blendMode = BlendMode.Plus
                    )
                }
            }

            // ================= BOTTOM SECTION: Progress Bars + Line Graph + Particle Torus =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompactHeight) 82.dp else 104.dp)
            ) {
                // Left side: Horizontal progress bars (top) + Line graph (bottom)
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    // 5 Horizontal Data Progress Bars inside an L-axis
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        drawHorizontalDataBars(
                            timeSec = timeSec,
                            primaryColor = primaryColor,
                            brightColor = brightColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Small Area/Line Telemetry Graph inside an L-axis
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        drawTelemetryLineGraph(
                            timeSec = timeSec,
                            primaryColor = primaryColor,
                            brightColor = brightColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right side: Glowing Particle Polyhedron / Torus
                Canvas(
                    modifier = Modifier
                        .size(if (isCompactHeight) 76.dp else 94.dp)
                ) {
                    drawParticlePolyhedronTorus(
                        timeSec = timeSec,
                        primaryColor = primaryColor,
                        brightColor = brightColor
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTopDualPacketWaveform(
    timeSec: Float,
    primaryColor: Color,
    brightColor: Color,
    ampScale: Float
) {
    val w = size.width
    val h = size.height
    val midY = h * 0.5f

    // Left optical lens flare on the horizontal waveform axis
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                HoloWhite.copy(alpha = 0.85f),
                primaryColor.copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(4f, midY),
            radius = h * 0.48f
        ),
        radius = h * 0.48f,
        center = Offset(4f, midY),
        blendMode = BlendMode.Plus
    )

    // Center horizontal baseline
    drawLine(
        color = brightColor.copy(alpha = 0.7f),
        start = Offset(0f, midY),
        end = Offset(w, midY),
        strokeWidth = 1.5f,
        blendMode = BlendMode.Plus
    )

    val barCount = 38
    val spacing = w / barCount
    for (i in 0 until barCount) {
        val normX = i.toFloat() / barCount
        // Create 2 distinct wave packets (left packet around 0.25, right packet around 0.72)
        val env1 = kotlin.math.exp(-((normX - 0.26f) / 0.14f).let { it * it })
        val env2 = kotlin.math.exp(-((normX - 0.72f) / 0.15f).let { it * it })
        val envelope = maxOf(env1, env2 * 1.1f)

        val mod = 0.55f + 0.45f * abs(sin(timeSec * 4.5f + i * 0.65f))
        val barHalfH = (h * 0.45f * envelope * mod * ampScale).coerceAtLeast(2f)
        val x = i * spacing + spacing * 0.5f

        // Glow bar
        drawLine(
            color = primaryColor.copy(alpha = 0.35f),
            start = Offset(x, midY - barHalfH * 1.15f),
            end = Offset(x, midY + barHalfH * 1.15f),
            strokeWidth = spacing * 0.75f,
            blendMode = BlendMode.Plus
        )
        // Sharp core bar
        drawLine(
            color = if (barHalfH > h * 0.26f) HoloWhite else brightColor,
            start = Offset(x, midY - barHalfH),
            end = Offset(x, midY + barHalfH),
            strokeWidth = spacing * 0.46f,
            blendMode = BlendMode.Plus
        )
    }
}

private fun DrawScope.drawMiddleWideFrequencyWaveform(
    timeSec: Float,
    primaryColor: Color,
    brightColor: Color,
    ampScale: Float
) {
    val w = size.width
    val h = size.height
    val midY = h * 0.5f

    // Subtle background oscilloscope grid
    val gridColor = primaryColor.copy(alpha = 0.14f)
    val cols = 12
    val rows = 4
    for (c in 0..cols) {
        val gx = (c.toFloat() / cols) * w
        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 0.8f)
    }
    for (r in 0..rows) {
        val gy = (r.toFloat() / rows) * h
        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 0.8f)
    }

    // Center horizontal glow axis
    drawLine(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, brightColor.copy(alpha = 0.85f), Color.Transparent)
        ),
        start = Offset(0f, midY),
        end = Offset(w, midY),
        strokeWidth = 1.8f,
        blendMode = BlendMode.Plus
    )

    val barCount = 68
    val step = w / barCount
    for (i in 0 until barCount) {
        val norm = i.toFloat() / barCount
        // Multi-peak envelope matching the middle SIGNAL DETECTED waveform in the reference image
        val p1 = kotlin.math.exp(-((norm - 0.19f) / 0.08f).let { it * it }) * 0.95f
        val p2 = kotlin.math.exp(-((norm - 0.36f) / 0.11f).let { it * it }) * 0.82f
        val p3 = kotlin.math.exp(-((norm - 0.53f) / 0.07f).let { it * it }) * 0.72f
        val p4 = kotlin.math.exp(-((norm - 0.68f) / 0.06f).let { it * it }) * 1.0f
        val p5 = kotlin.math.exp(-((norm - 0.83f) / 0.09f).let { it * it }) * 0.55f
        val env = maxOf(p1, p2, p3, p4, p5, 0.08f)

        val highFreq = 0.4f + 0.6f * abs(sin(i * 1.3f + timeSec * 5.2f) * cos(i * 0.45f - timeSec * 3.1f))
        val halfH = (h * 0.47f * env * highFreq * ampScale).coerceAtLeast(1.5f)
        val x = i * step + step * 0.5f

        // Soft bloom behind spike
        drawLine(
            color = primaryColor.copy(alpha = 0.32f),
            start = Offset(x, midY - halfH * 1.15f),
            end = Offset(x, midY + halfH * 1.15f),
            strokeWidth = step * 0.9f,
            blendMode = BlendMode.Plus
        )
        // Crisp vertical frequency bar
        drawLine(
            color = if (halfH > h * 0.3f) HoloWhite else brightColor,
            start = Offset(x, midY - halfH),
            end = Offset(x, midY + halfH),
            strokeWidth = step * 0.48f,
            blendMode = BlendMode.Plus
        )
    }
}

private fun DrawScope.drawHorizontalDataBars(
    timeSec: Float,
    primaryColor: Color,
    brightColor: Color
) {
    val w = size.width
    val h = size.height

    // L-shaped axis frame
    drawLine(
        color = primaryColor.copy(alpha = 0.75f),
        start = Offset(0f, 0f),
        end = Offset(0f, h),
        strokeWidth = 1.5f
    )
    drawLine(
        color = primaryColor.copy(alpha = 0.75f),
        start = Offset(0f, h),
        end = Offset(w, h),
        strokeWidth = 1.5f
    )

    val baseRatios = floatArrayOf(0.42f, 0.74f, 0.86f, 0.64f, 0.38f)
    val barCount = baseRatios.size
    val rowH = (h - 6f) / barCount

    for (i in 0 until barCount) {
        val osc = 0.05f * sin(timeSec * 2.4f + i * 1.2f)
        val ratio = (baseRatios[i] + osc).coerceIn(0.18f, 0.95f)
        val y = 3f + i * rowH + rowH * 0.25f
        val barH = (rowH * 0.52f).coerceAtLeast(3f)

        // Track background
        drawRect(
            color = primaryColor.copy(alpha = 0.16f),
            topLeft = Offset(5f, y),
            size = Size(w * 0.92f, barH)
        )
        // Active glowing bar
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(primaryColor, brightColor)
            ),
            topLeft = Offset(5f, y),
            size = Size(w * ratio, barH),
            blendMode = BlendMode.Plus
        )
    }
}

private fun DrawScope.drawTelemetryLineGraph(
    timeSec: Float,
    primaryColor: Color,
    brightColor: Color
) {
    val w = size.width
    val h = size.height

    // Subtle grid
    for (i in 1..4) {
        val gx = (i / 4f) * w
        drawLine(primaryColor.copy(alpha = 0.12f), Offset(gx, 0f), Offset(gx, h), 0.8f)
    }
    // L-shaped axis
    drawLine(primaryColor.copy(alpha = 0.75f), Offset(0f, 0f), Offset(0f, h), 1.5f)
    drawLine(primaryColor.copy(alpha = 0.75f), Offset(0f, h), Offset(w, h), 1.5f)

    // Rising telemetry curve matching the bottom-left graph in the reference
    val baseProfile = floatArrayOf(0.18f, 0.28f, 0.22f, 0.36f, 0.48f, 0.42f, 0.65f, 0.56f, 0.82f, 0.76f)
    val linePath = Path()
    val fillPath = Path()
    fillPath.moveTo(2f, h - 1f)

    for (idx in baseProfile.indices) {
        val x = 2f + (idx.toFloat() / (baseProfile.size - 1)) * (w - 4f)
        val wave = 0.05f * sin(timeSec * 3.0f + idx * 0.8f)
        val normY = (baseProfile[idx] + wave).coerceIn(0.08f, 0.92f)
        val y = h - normY * (h - 4f)
        if (idx == 0) {
            linePath.moveTo(x, y)
        } else {
            linePath.lineTo(x, y)
        }
        fillPath.lineTo(x, y)
    }
    fillPath.lineTo(w - 2f, h - 1f)
    fillPath.close()

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            listOf(primaryColor.copy(alpha = 0.42f), primaryColor.copy(alpha = 0.05f))
        ),
        blendMode = BlendMode.Plus
    )
    drawPath(
        path = linePath,
        color = HoloWhite,
        style = Stroke(width = 1.8f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )
}

/**
 * Draws the glowing cyan particle polyhedron/torus on the bottom-right of the right panel:
 * a multi-faceted geometric ring with an octagonal hollow center, triangulated struts, and particle dust.
 */
private fun DrawScope.drawParticlePolyhedronTorus(
    timeSec: Float,
    primaryColor: Color,
    brightColor: Color
) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val outerR = size.minDimension * 0.46f
    val midR = size.minDimension * 0.37f
    val innerR = size.minDimension * 0.25f

    // Soft radial aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                primaryColor.copy(alpha = 0.28f),
                primaryColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = outerR * 1.15f
        ),
        radius = outerR * 1.15f,
        center = Offset(cx, cy),
        blendMode = BlendMode.Plus
    )

    val sides = 8
    val rot = timeSec * 0.35f

    val outerPts = List(sides) { i ->
        val a = rot + (i.toFloat() / sides) * 2f * PI.toFloat()
        Offset(cx + cos(a) * outerR, cy + sin(a) * outerR)
    }
    val midPts = List(sides) { i ->
        val a = rot + ((i + 0.5f) / sides) * 2f * PI.toFloat()
        Offset(cx + cos(a) * midR, cy + sin(a) * midR)
    }
    val innerPts = List(sides) { i ->
        val a = -rot * 0.6f + (i.toFloat() / sides) * 2f * PI.toFloat()
        Offset(cx + cos(a) * innerR, cy + sin(a) * innerR)
    }

    // Draw translucent facet fills between inner, mid, and outer rings
    val facetPath = Path()
    for (i in 0 until sides) {
        val next = (i + 1) % sides
        facetPath.reset()
        facetPath.moveTo(outerPts[i].x, outerPts[i].y)
        facetPath.lineTo(midPts[i].x, midPts[i].y)
        facetPath.lineTo(innerPts[i].x, innerPts[i].y)
        facetPath.close()
        drawPath(
            path = facetPath,
            color = primaryColor.copy(alpha = if (i % 2 == 0) 0.22f else 0.12f),
            blendMode = BlendMode.Plus
        )

        // Wireframe struts
        drawLine(brightColor.copy(alpha = 0.85f), outerPts[i], outerPts[next], 1.5f, blendMode = BlendMode.Plus)
        drawLine(brightColor.copy(alpha = 0.75f), innerPts[i], innerPts[next], 1.5f, blendMode = BlendMode.Plus)
        drawLine(primaryColor.copy(alpha = 0.7f), outerPts[i], midPts[i], 1.2f, blendMode = BlendMode.Plus)
        drawLine(primaryColor.copy(alpha = 0.7f), midPts[i], outerPts[next], 1.2f, blendMode = BlendMode.Plus)
        drawLine(primaryColor.copy(alpha = 0.65f), midPts[i], innerPts[i], 1.1f, blendMode = BlendMode.Plus)
        drawLine(primaryColor.copy(alpha = 0.65f), midPts[i], innerPts[next], 1.1f, blendMode = BlendMode.Plus)
    }

    // Dense glowing particle dots along the torus shell
    for (pIdx in 0 until 44) {
        val a = (pIdx / 44f) * 2f * PI.toFloat() + timeSec * 0.25f
        val r = innerR + ((pIdx * 7) % 10) / 10f * (outerR - innerR)
        val px = cx + cos(a) * r
        val py = cy + sin(a) * r
        drawCircle(
            color = if (pIdx % 3 == 0) HoloWhite else brightColor.copy(alpha = 0.8f),
            radius = if (pIdx % 4 == 0) 1.8f else 1.2f,
            center = Offset(px, py),
            blendMode = BlendMode.Plus
        )
    }
}
