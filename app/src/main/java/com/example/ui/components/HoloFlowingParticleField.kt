package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloGold
import com.example.ui.theme.HoloRed
import com.example.ui.theme.HoloWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private data class DriftingCyanParticle(
    val id: Int,
    val baseX: Float,
    val baseY: Float,
    val depthLayer: Float, // 0.25 (far bokeh) .. 1.0 (near crisp)
    val driftSpeedX: Float,
    val driftSpeedY: Float,
    val waveFreq: Float,
    val waveAmp: Float,
    val phase: Float,
    val baseRadius: Float,
    val hasTrail: Boolean,
    val isGoldenAccent: Boolean
)

/**
 * Custom Composable animation using [Canvas] and [rememberInfiniteTransition] to render
 * a continuous, flowing 3D parallax drift effect for background cyan nano-particles,
 * velocity comet trails, and dynamic plexus filaments.
 *
 * Drawing is scoped strictly to the Canvas draw phase so parent composables do not recompose.
 */
@Composable
fun HoloFlowingParticleField(
    modifier: Modifier = Modifier,
    particleCount: Int = 110,
    flowIntensity: Float = 1.0f,
    isAlertRed: Boolean = false
) {
    val particles = remember(particleCount) {
        val rng = Random(0x484F4C4F)
        List(particleCount) { idx ->
            val depth = 0.25f + rng.nextFloat() * 0.75f
            // Bias a stream along the diagonal holographic plume (bottom-right -> center -> top-left)
            val isPlumeStream = idx % 3 == 0
            val bx = if (isPlumeStream) {
                0.18f + rng.nextFloat() * 0.56f
            } else {
                rng.nextFloat()
            }
            val by = if (isPlumeStream) {
                0.08f + rng.nextFloat() * 0.72f
            } else {
                rng.nextFloat()
            }

            DriftingCyanParticle(
                id = idx,
                baseX = bx,
                baseY = by,
                depthLayer = depth,
                driftSpeedX = (-0.014f - rng.nextFloat() * 0.022f) * (0.5f + depth * 0.7f),
                driftSpeedY = (-0.010f - rng.nextFloat() * 0.018f) * (0.5f + depth * 0.7f),
                waveFreq = 1.5f + rng.nextFloat() * 2.5f,
                waveAmp = 0.012f + rng.nextFloat() * 0.026f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
                baseRadius = if (depth < 0.42f) {
                    2.6f + rng.nextFloat() * 2.2f // Soft background bokeh
                } else {
                    1.1f + depth * 1.6f // Crisp foreground nano-particle
                },
                hasTrail = depth > 0.68f && idx % 2 == 0,
                isGoldenAccent = idx % 11 == 0
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "holo_particle_drift")

    // Continuous normalized cycle [0f..100f] driving smooth curl-drift without resets
    val driftClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_drift_clock"
    )

    val primaryCyan = if (isAlertRed) HoloRed else HoloCyan
    val brightCyan = if (isAlertRed) Color(0xFFFF9E9E) else HoloCyanBright

    // Pre-allocate screen-space coordinate arrays to avoid allocations during onDraw
    val screenX = remember(particleCount) { FloatArray(particleCount) }
    val screenY = remember(particleCount) { FloatArray(particleCount) }

    Canvas(
        modifier = modifier.testTag("holo_flowing_particle_field")
    ) {
        val w = size.width
        val h = size.height
        val t = driftClock * flowIntensity.coerceIn(0.4f, 2.0f)

        // 1. Compute flowing positions in a curl-like harmonic vector field and draw particles
        for (i in particles.indices) {
            val p = particles[i]

            // Linear drift with seamless toroidal wrap in [0..1]
            val rawX = (p.baseX + p.driftSpeedX * t) % 1f
            val rawY = (p.baseY + p.driftSpeedY * t) % 1f
            val wrappedX = if (rawX < 0f) rawX + 1f else rawX
            val wrappedY = if (rawY < 0f) rawY + 1f else rawY

            // Curl-field harmonic turbulence (orthogonal sine/cosine eddies)
            val eddyAngle = t * p.waveFreq + p.phase + wrappedY * 4.2f
            val curlX = sin(eddyAngle) * p.waveAmp
            val curlY = cos(eddyAngle * 0.85f + wrappedX * 3.6f) * p.waveAmp

            val px = ((wrappedX + curlX).coerceIn(-0.05f, 1.05f)) * w
            val py = ((wrappedY + curlY).coerceIn(-0.05f, 1.05f)) * h
            screenX[i] = px
            screenY[i] = py

            // Shimmer / scintillation pulse
            val twinkle = 0.50f + 0.50f * sin(t * 2.8f + p.phase)
            val alpha = (p.depthLayer * 0.72f * twinkle).coerceIn(0.08f, 0.92f)

            val particleColor = when {
                p.isGoldenAccent -> HoloGold.copy(alpha = alpha)
                p.depthLayer > 0.82f -> brightCyan.copy(alpha = alpha)
                else -> primaryCyan.copy(alpha = alpha)
            }

            // Velocity motion trail for fast near-field particles
            if (p.hasTrail) {
                val vx = (p.driftSpeedX + cos(eddyAngle) * p.waveAmp * 0.6f) * w * 0.42f
                val vy = (p.driftSpeedY - sin(eddyAngle * 0.85f) * p.waveAmp * 0.6f) * h * 0.42f
                drawLine(
                    color = particleColor.copy(alpha = alpha * 0.38f),
                    start = Offset(px, py),
                    end = Offset(px - vx, py - vy),
                    strokeWidth = p.baseRadius * 0.9f,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )
            }

            // Soft outer halo for bokeh and bright foreground particles
            if (p.depthLayer < 0.42f || p.depthLayer > 0.78f) {
                drawCircle(
                    color = particleColor.copy(alpha = alpha * 0.24f),
                    radius = p.baseRadius * 2.4f,
                    center = Offset(px, py),
                    blendMode = BlendMode.Plus
                )
            }

            // Core glowing nano-particle
            drawCircle(
                color = if (p.depthLayer > 0.85f && twinkle > 0.82f) {
                    HoloWhite.copy(alpha = alpha)
                } else {
                    particleColor
                },
                radius = p.baseRadius,
                center = Offset(px, py),
                blendMode = BlendMode.Plus
            )
        }

        // 2. Dynamic Flowing Plexus Filaments between neighboring foreground cyan particles
        val maxLinkDist = minOf(w, h) * 0.105f
        val maxLinkDistSq = maxLinkDist * maxLinkDist
        for (i in 0 until particles.size step 2) {
            if (particles[i].depthLayer < 0.55f) continue
            val x1 = screenX[i]
            val y1 = screenY[i]
            var linksDrawn = 0
            for (j in i + 1 until particles.size) {
                if (particles[j].depthLayer < 0.55f) continue
                val dx = x1 - screenX[j]
                val dy = y1 - screenY[j]
                val distSq = dx * dx + dy * dy
                if (distSq < maxLinkDistSq) {
                    val distRatio = 1f - (sqrt(distSq) / maxLinkDist)
                    drawLine(
                        color = primaryCyan.copy(alpha = (distRatio * 0.26f).coerceIn(0.02f, 0.26f)),
                        start = Offset(x1, y1),
                        end = Offset(screenX[j], screenY[j]),
                        strokeWidth = 0.9f,
                        blendMode = BlendMode.Plus
                    )
                    linksDrawn++
                    if (linksDrawn >= 2) break
                }
            }
        }
    }
}
