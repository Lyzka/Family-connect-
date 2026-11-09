package com.example.ui.render3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.HoloBlack
import com.example.ui.theme.HoloCorridorBlue
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloDeepBlue
import com.example.ui.theme.HoloGold
import com.example.ui.theme.HoloGoldBright
import com.example.ui.theme.HoloOrangeSpark
import com.example.ui.theme.HoloRed
import com.example.ui.theme.HoloWhite
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Vec3(val x: Float, val y: Float, val z: Float)

data class GlobePoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val isLand: Boolean,
    val isBorder: Boolean,
    val sizeFactor: Float
)

data class NanoParticle(
    val xNorm: Float,
    val yNorm: Float,
    val zDepth: Float,
    val radius: Float,
    val isGold: Boolean,
    val isLeftSpark: Boolean,
    val speedX: Float,
    val speedY: Float,
    val phase: Float
)

data class PolyhedronInstance(
    val xOffsetNorm: Float,
    val yOffsetNorm: Float,
    val zOffset: Float,
    val radiusNorm: Float,
    val rotSpeedX: Float,
    val rotSpeedY: Float,
    val rotSpeedZ: Float,
    val hasInnerCore: Boolean,
    val goldTint: Float
)

object HoloGeometryCache {
    // Golden ratio for exact IcosahedronGeometry
    private val PHI = ((1.0 + sqrt(5.0)) / 2.0).toFloat()

    val icosahedronVertices: List<Vec3> = listOf(
        Vec3(-1f, PHI, 0f), Vec3(1f, PHI, 0f), Vec3(-1f, -PHI, 0f), Vec3(1f, -PHI, 0f),
        Vec3(0f, -1f, PHI), Vec3(0f, 1f, PHI), Vec3(0f, -1f, -PHI), Vec3(0f, 1f, -PHI),
        Vec3(PHI, 0f, -1f), Vec3(PHI, 0f, 1f), Vec3(-PHI, 0f, -1f), Vec3(-PHI, 0f, 1f)
    ).map { v ->
        val len = sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
        Vec3(v.x / len, v.y / len, v.z / len)
    }

    val icosahedronFaces: List<Triple<Int, Int, Int>> = listOf(
        Triple(0, 11, 5), Triple(0, 5, 1), Triple(0, 1, 7), Triple(0, 7, 10), Triple(0, 10, 11),
        Triple(1, 5, 9), Triple(5, 11, 4), Triple(11, 10, 2), Triple(10, 7, 6), Triple(7, 1, 8),
        Triple(3, 9, 4), Triple(3, 4, 2), Triple(3, 2, 6), Triple(3, 6, 8), Triple(3, 8, 9),
        Triple(4, 9, 5), Triple(2, 4, 11), Triple(6, 2, 10), Triple(8, 6, 7), Triple(9, 8, 1)
    )

    val icosahedronEdges: List<Pair<Int, Int>> = buildList {
        val set = mutableSetOf<Pair<Int, Int>>()
        for ((a, b, c) in icosahedronFaces) {
            listOf(minOf(a, b) to maxOf(a, b), minOf(b, c) to maxOf(b, c), minOf(a, c) to maxOf(a, c))
                .forEach { if (set.add(it)) add(it) }
        }
    }

    // Precompute continental point-cloud globe matching the reference Earth point-cloud
    val globePoints: List<GlobePoint> = buildList {
        val latSteps = 30
        val lonSteps = 60
        for (i in 0..latSteps) {
            val lat = -PI.toFloat() * 0.48f + (i.toFloat() / latSteps) * PI.toFloat() * 0.96f
            val cosLat = cos(lat)
            val sinLat = sin(lat)
            val currentLonSteps = (lonSteps * cosLat).toInt().coerceAtLeast(8)
            for (j in 0 until currentLonSteps) {
                val lon = -PI.toFloat() + (j.toFloat() / currentLonSteps) * (2f * PI.toFloat())
                val landScore = evaluateContinentMask(lat, lon)
                if (landScore > 0.16f) {
                    val isBorder = landScore in 0.16f..0.42f
                    val x = cosLat * sin(lon)
                    val y = -sinLat
                    val z = cosLat * cos(lon)
                    add(
                        GlobePoint(
                            x = x,
                            y = y,
                            z = z,
                            isLand = true,
                            isBorder = isBorder,
                            sizeFactor = if (isBorder) 1.35f else (0.85f + (landScore * 0.4f))
                        )
                    )
                } else if (i % 3 == 0 && j % 3 == 0) {
                    // Sparse ocean graticule grid dots
                    val x = cosLat * sin(lon)
                    val y = -sinLat
                    val z = cosLat * cos(lon)
                    add(
                        GlobePoint(
                            x = x,
                            y = y,
                            z = z,
                            isLand = false,
                            isBorder = false,
                            sizeFactor = 0.55f
                        )
                    )
                }
            }
        }
    }

    // Realistic procedural continental silhouettes (Europe, Africa, Americas, Asia)
    private fun evaluateContinentMask(latRad: Float, lonRad: Float): Float {
        val lat = latRad * (180f / PI.toFloat())
        val lon = lonRad * (180f / PI.toFloat())

        // Europe & Mediterranean
        val europe = gaussianBlob(lat, lon, 48f, 15f, 14f, 22f) +
            gaussianBlob(lat, lon, 58f, 25f, 10f, 18f) +
            gaussianBlob(lat, lon, 41f, -2f, 6f, 10f)

        // Africa
        val africa = gaussianBlob(lat, lon, 18f, 16f, 16f, 22f) +
            gaussianBlob(lat, lon, -8f, 24f, 18f, 13f) +
            gaussianBlob(lat, lon, 8f, 38f, 10f, 10f)

        // Asia & Middle East
        val asia = gaussianBlob(lat, lon, 32f, 52f, 14f, 18f) +
            gaussianBlob(lat, lon, 46f, 85f, 18f, 34f) +
            gaussianBlob(lat, lon, 26f, 105f, 15f, 20f) +
            gaussianBlob(lat, lon, 60f, 115f, 12f, 32f)

        // North America
        val northAmerica = gaussianBlob(lat, lon, 45f, -100f, 16f, 26f) +
            gaussianBlob(lat, lon, 58f, -115f, 12f, 24f) +
            gaussianBlob(lat, lon, 24f, -98f, 10f, 10f) +
            gaussianBlob(lat, lon, 72f, -42f, 9f, 16f) // Greenland

        // South America
        val southAmerica = gaussianBlob(lat, lon, -10f, -60f, 16f, 16f) +
            gaussianBlob(lat, lon, -32f, -64f, 16f, 9f)

        // Australia & SE Asia Islands
        val australia = gaussianBlob(lat, lon, -25f, 135f, 11f, 16f) +
            gaussianBlob(lat, lon, -2f, 115f, 7f, 14f)

        val raw = maxOf(europe, africa, asia, northAmerica, southAmerica, australia)
        val detailNoise = 0.12f * sin(latRad * 14f) * cos(lonRad * 16f)
        return (raw + detailNoise).coerceIn(0f, 1f)
    }

    private fun gaussianBlob(
        lat: Float,
        lon: Float,
        cLat: Float,
        cLon: Float,
        sLat: Float,
        sLon: Float
    ): Float {
        val dLat = (lat - cLat) / sLat
        val dLon = (lon - cLon) / sLon
        val d2 = dLat * dLat + dLon * dLon
        return if (d2 > 2.6f) 0f else kotlin.math.exp(-d2 * 1.35f)
    }

    // Floating wireframe icosahedrons positioned 1:1 with the reference image
    val scenePolyhedrons: List<PolyhedronInstance> = listOf(
        // 1. Top-Right / Upper-Center Hero Nested Dual-Icosahedron
        PolyhedronInstance(
            xOffsetNorm = 0.055f,
            yOffsetNorm = -0.32f,
            zOffset = 0.2f,
            radiusNorm = 0.095f,
            rotSpeedX = 0.25f,
            rotSpeedY = 0.38f,
            rotSpeedZ = 0.15f,
            hasInnerCore = true,
            goldTint = 0.05f
        ),
        // 2. Top-Left Floating Icosahedron in the cyan particle plume
        PolyhedronInstance(
            xOffsetNorm = -0.075f,
            yOffsetNorm = -0.32f,
            zOffset = -0.1f,
            radiusNorm = 0.052f,
            rotSpeedX = -0.32f,
            rotSpeedY = 0.24f,
            rotSpeedZ = 0.28f,
            hasInnerCore = true,
            goldTint = 0.0f
        ),
        // 3. Mid-Left Icosahedron on the upper-left orbital curve
        PolyhedronInstance(
            xOffsetNorm = -0.125f,
            yOffsetNorm = -0.11f,
            zOffset = 0.1f,
            radiusNorm = 0.036f,
            rotSpeedX = 0.42f,
            rotSpeedY = -0.31f,
            rotSpeedZ = 0.19f,
            hasInnerCore = false,
            goldTint = 0.0f
        ),
        // 4. Right Orbital Tip Golden/Cyan Polyhedron
        PolyhedronInstance(
            xOffsetNorm = 0.135f,
            yOffsetNorm = -0.11f,
            zOffset = 0.15f,
            radiusNorm = 0.054f,
            rotSpeedX = 0.29f,
            rotSpeedY = 0.45f,
            rotSpeedZ = -0.22f,
            hasInnerCore = false,
            goldTint = 0.72f
        ),
        // 5. Lower-Right Wireframe Icosahedron
        PolyhedronInstance(
            xOffsetNorm = 0.132f,
            yOffsetNorm = 0.12f,
            zOffset = 0.2f,
            radiusNorm = 0.045f,
            rotSpeedX = -0.36f,
            rotSpeedY = -0.28f,
            rotSpeedZ = 0.34f,
            hasInnerCore = true,
            goldTint = 0.2f
        ),
        // 6. Bottom-Center Golden-Lit Icosahedron directly below the globe
        PolyhedronInstance(
            xOffsetNorm = 0.042f,
            yOffsetNorm = 0.225f,
            zOffset = 0.25f,
            radiusNorm = 0.052f,
            rotSpeedX = 0.35f,
            rotSpeedY = -0.38f,
            rotSpeedZ = 0.25f,
            hasInnerCore = true,
            goldTint = 0.65f
        ),
        // 7. Bottom-Left Large Wireframe Polyhedron Mesh behind the CALL button
        PolyhedronInstance(
            xOffsetNorm = -0.21f,
            yOffsetNorm = 0.365f,
            zOffset = 0.3f,
            radiusNorm = 0.115f,
            rotSpeedX = 0.18f,
            rotSpeedY = 0.26f,
            rotSpeedZ = -0.14f,
            hasInnerCore = true,
            goldTint = 0.0f
        )
    )

    val nanoParticles: List<NanoParticle> = buildList {
        val rng = Random(20261108)
        // 1. Far-left golden/orange disintegrating sparks off the CONTACTS panel
        for (i in 0 until 60) {
            val x = 0.02f + rng.nextFloat() * 0.12f
            val y = 0.22f + rng.nextFloat() * 0.56f
            add(
                NanoParticle(
                    xNorm = x,
                    yNorm = y,
                    zDepth = 0.6f + rng.nextFloat() * 0.6f,
                    radius = 1.2f + rng.nextFloat() * 2.5f,
                    isGold = true,
                    isLeftSpark = true,
                    speedX = -(0.008f + rng.nextFloat() * 0.018f),
                    speedY = (rng.nextFloat() - 0.5f) * 0.012f,
                    phase = rng.nextFloat() * 6.28f
                )
            )
        }
        // 2. Top-left & center-top dense cyan particle nebula plume
        for (i in 0 until 85) {
            val t = rng.nextFloat()
            val x = 0.25f + t * 0.28f + (rng.nextFloat() - 0.5f) * 0.12f
            val y = 0.02f + t * 0.42f + (rng.nextFloat() - 0.5f) * 0.14f
            add(
                NanoParticle(
                    xNorm = x,
                    yNorm = y,
                    zDepth = 0.4f + rng.nextFloat() * 0.8f,
                    radius = 1.1f + rng.nextFloat() * 2.4f,
                    isGold = rng.nextFloat() < 0.08f,
                    isLeftSpark = false,
                    speedX = (rng.nextFloat() - 0.5f) * 0.005f,
                    speedY = -(0.003f + rng.nextFloat() * 0.007f),
                    phase = rng.nextFloat() * 6.28f
                )
            )
        }
        // 3. Orbital golden & cyan dust around the central globe and bottom-left polyhedron
        for (i in 0 until 95) {
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val dist = 0.10f + rng.nextFloat() * 0.24f
            val cx = 0.50f + cos(angle) * dist * 0.85f
            val cy = 0.48f + sin(angle) * dist
            val isGoldZone = (cos(angle - 0.5f) > 0.15f) || (sin(angle) > 0.45f)
            add(
                NanoParticle(
                    xNorm = cx,
                    yNorm = cy,
                    zDepth = 0.5f + rng.nextFloat() * 0.7f,
                    radius = 1.1f + rng.nextFloat() * 2.2f,
                    isGold = isGoldZone && rng.nextFloat() < 0.58f,
                    isLeftSpark = false,
                    speedX = (rng.nextFloat() - 0.5f) * 0.004f,
                    speedY = (rng.nextFloat() - 0.5f) * 0.004f,
                    phase = rng.nextFloat() * 6.28f
                )
            )
        }
    }
}

fun DrawScope.drawSciFiCorridorBackground() {
    val w = size.width
    val h = size.height

    // 1. Deep space-corridor base gradient
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF0D2847),
                Color(0xFF061325),
                Color(0xFF02060D)
            ),
            center = Offset(w * 0.48f, h * 0.46f),
            radius = maxOf(w, h) * 0.72f
        ),
        size = size
    )

    // 2. Out-of-focus architectural sci-fi corridor perspective lines & ceiling bays
    val vanish = Offset(w * 0.50f, h * 0.48f)
    val corridorColor = Color(0xFF1A3A5C).copy(alpha = 0.22f)
    val ribAngles = listOf(-0.85f, -0.58f, -0.35f, 0.35f, 0.58f, 0.85f, 2.25f, 2.55f, -2.25f, -2.55f)
    for (angle in ribAngles) {
        val endX = vanish.x + cos(angle) * w * 0.9f
        val endY = vanish.y + sin(angle) * h * 0.9f
        drawLine(
            color = corridorColor,
            start = vanish,
            end = Offset(endX, endY),
            strokeWidth = 6f
        )
    }

    // Ceiling perspective trapezoidal bays and blurred overhead lights
    for (depth in 1..4) {
        val d = depth / 4.5f
        val bayHalfW = w * (0.12f + d * 0.36f)
        val bayY = h * (0.32f - d * 0.28f)
        drawLine(
            color = Color(0xFF285580).copy(alpha = 0.16f * (1f - d * 0.5f)),
            start = Offset(vanish.x - bayHalfW, bayY),
            end = Offset(vanish.x + bayHalfW, bayY),
            strokeWidth = 4f + d * 6f
        )
        // Twin ceiling light strips (left & right of corridor roof)
        val lightOffset = bayHalfW * 0.68f
        for (sign in listOf(-1f, 1f)) {
            val lx = vanish.x + sign * lightOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB8E6FF).copy(alpha = 0.22f),
                        Color(0xFF00F3FF).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(lx, bayY),
                    radius = 28f + d * 24f
                ),
                radius = 28f + d * 24f,
                center = Offset(lx, bayY)
            )
        }
    }

    // 3. Left-edge bright cyan optical lens flare (matching the left edge of the reference image)
    val leftFlareCenter = Offset(w * 0.015f, h * 0.21f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                HoloWhite.copy(alpha = 0.85f),
                HoloCyan.copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = leftFlareCenter,
            radius = w * 0.11f
        ),
        radius = w * 0.11f,
        center = leftFlareCenter,
        blendMode = BlendMode.Plus
    )
    // Horizontal anamorphic streak from left flare
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                HoloWhite.copy(alpha = 0.9f),
                HoloCyan.copy(alpha = 0.45f),
                Color.Transparent
            ),
            startX = 0f,
            endX = w * 0.22f
        ),
        start = Offset(0f, leftFlareCenter.y),
        end = Offset(w * 0.22f, leftFlareCenter.y),
        strokeWidth = 3.5f,
        blendMode = BlendMode.Plus
    )
}

fun DrawScope.drawHolographicCenterpieceAndParticles(
    timeSec: Float,
    yawOffset: Float,
    pitchOffset: Float,
    isDisconnected: Boolean,
    isSecure: Boolean,
    pulseEnergy: Float
) {
    val w = size.width
    val h = size.height
    val centerX = w * 0.50f
    val centerY = h * 0.46f
    val globeRadius = minOf(w, h) * 0.215f

    val primaryColor = if (isDisconnected) HoloRed else HoloCyan
    val brightColor = if (isDisconnected) Color(0xFFFF9999) else HoloCyanBright
    val accentGold = if (isDisconnected) HoloOrangeSpark else HoloGold

    // 1. Central atmospheric volumetric bloom behind the globe
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.36f * pulseEnergy.coerceAtMost(1.4f)),
                primaryColor.copy(alpha = 0.14f),
                Color(0xFF004466).copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = globeRadius * 2.15f
        ),
        radius = globeRadius * 2.15f,
        center = Offset(centerX, centerY),
        blendMode = BlendMode.Plus
    )

    // 2. Background Nano-Particles & Far-Left Golden Disintegration Sparks
    for (p in HoloGeometryCache.nanoParticles) {
        val px: Float
        val py: Float
        if (p.isLeftSpark) {
            val cycle = (timeSec * 0.45f + p.phase) % 1f
            px = (p.xNorm + p.speedX * cycle * 4.5f) * w
            py = (p.yNorm + sin(timeSec * 1.8f + p.phase) * 0.015f) * h
        } else {
            px = (p.xNorm + sin(timeSec * 0.6f + p.phase) * 0.008f) * w
            py = (p.yNorm + cos(timeSec * 0.5f + p.phase) * 0.008f) * h
        }

        val flicker = 0.55f + 0.45f * sin(timeSec * 3.2f + p.phase)
        val color = if (p.isGold) {
            if (p.isLeftSpark) HoloOrangeSpark.copy(alpha = (0.75f * flicker).coerceIn(0.1f, 1f))
            else accentGold.copy(alpha = (0.7f * flicker).coerceIn(0.1f, 1f))
        } else {
            primaryColor.copy(alpha = (0.65f * flicker).coerceIn(0.1f, 1f))
        }

        // Soft outer glow for bright particles
        if (p.radius > 2.0f) {
            drawCircle(
                color = color.copy(alpha = color.alpha * 0.32f),
                radius = p.radius * 2.6f,
                center = Offset(px, py),
                blendMode = BlendMode.Plus
            )
        }
        drawCircle(
            color = color,
            radius = p.radius,
            center = Offset(px, py),
            blendMode = BlendMode.Plus
        )
    }

    // 3. Back halves of the 3 intersecting orbital rings (z < 0)
    drawOrbitalRingsPass(
        centerX = centerX,
        centerY = centerY,
        globeRadius = globeRadius,
        timeSec = timeSec,
        yawOffset = yawOffset,
        pitchOffset = pitchOffset,
        frontPass = false,
        primaryColor = primaryColor,
        brightColor = brightColor,
        accentGold = accentGold
    )

    // 4. Translucent inner globe core & rim glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF04223A).copy(alpha = 0.65f),
                primaryColor.copy(alpha = 0.24f),
                primaryColor.copy(alpha = 0.52f)
            ),
            center = Offset(centerX - globeRadius * 0.15f, centerY - globeRadius * 0.15f),
            radius = globeRadius * 1.02f
        ),
        radius = globeRadius,
        center = Offset(centerX, centerY)
    )
    // Bright outer atmospheric horizon ring
    drawCircle(
        color = brightColor.copy(alpha = 0.55f),
        radius = globeRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 2.2f),
        blendMode = BlendMode.Plus
    )

    // 5. 3D Point-Cloud Globe (Earth Continents + Graticule)
    val rotY = timeSec * 0.28f + yawOffset
    val rotX = -0.18f + pitchOffset
    val cosY = cos(rotY)
    val sinY = sin(rotY)
    val cosX = cos(rotX)
    val sinX = sin(rotX)

    for (pt in HoloGeometryCache.globePoints) {
        // Rotate around Y
        val rx = pt.x * cosY + pt.z * sinY
        val rz1 = -pt.x * sinY + pt.z * cosY
        // Rotate around X
        val ry = pt.y * cosX - rz1 * sinX
        val rz = pt.y * sinX + rz1 * cosX

        // Draw back-hemisphere faintly and front-hemisphere brightly
        if (rz > -0.35f) {
            val depthAlpha = ((rz + 0.35f) / 1.35f).coerceIn(0.08f, 1f)
            val screenX = centerX + rx * globeRadius
            val screenY = centerY + ry * globeRadius
            val dotRadius = if (pt.isLand) {
                (1.4f + depthAlpha * 1.3f) * pt.sizeFactor
            } else {
                1.0f
            }
            val dotColor = when {
                pt.isBorder && rz > 0.1f -> HoloWhite.copy(alpha = 0.85f * depthAlpha)
                pt.isLand -> primaryColor.copy(alpha = 0.78f * depthAlpha)
                else -> primaryColor.copy(alpha = 0.22f * depthAlpha)
            }
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(screenX, screenY),
                blendMode = BlendMode.Plus
            )
        }
    }

    // If SECURE mode is active, draw a subtle geodesic shield lattice over the globe
    if (isSecure) {
        drawPolyhedron3D(
            cx = centerX,
            cy = centerY,
            radius = globeRadius * 1.07f,
            rotX = -timeSec * 0.2f,
            rotY = timeSec * 0.35f,
            rotZ = 0.1f,
            edgeColor = HoloCyanBright.copy(alpha = 0.42f),
            vertexColor = HoloWhite.copy(alpha = 0.8f),
            hasInnerCore = false
        )
    }

    // 6. Front halves of the 3 intersecting orbital rings (z >= 0)
    drawOrbitalRingsPass(
        centerX = centerX,
        centerY = centerY,
        globeRadius = globeRadius,
        timeSec = timeSec,
        yawOffset = yawOffset,
        pitchOffset = pitchOffset,
        frontPass = true,
        primaryColor = primaryColor,
        brightColor = brightColor,
        accentGold = accentGold
    )

    // 7. Floating 3D Wireframe Icosahedrons & Plexus Tethers
    for ((index, poly) in HoloGeometryCache.scenePolyhedrons.withIndex()) {
        val px = centerX + poly.xOffsetNorm * w + sin(timeSec * 0.7f + index) * 4f
        val py = centerY + poly.yOffsetNorm * h + cos(timeSec * 0.6f + index) * 4f
        val pr = poly.radiusNorm * minOf(w, h)

        val edgeCol = if (poly.goldTint > 0.4f) {
            Color(
                red = primaryColor.red * (1f - poly.goldTint) + accentGold.red * poly.goldTint,
                green = primaryColor.green * (1f - poly.goldTint) + accentGold.green * poly.goldTint,
                blue = primaryColor.blue * (1f - poly.goldTint) + accentGold.blue * poly.goldTint,
                alpha = 0.78f
            )
        } else {
            primaryColor.copy(alpha = 0.75f)
        }

        drawPolyhedron3D(
            cx = px,
            cy = py,
            radius = pr,
            rotX = timeSec * poly.rotSpeedX + index * 0.7f + pitchOffset * 0.5f,
            rotY = timeSec * poly.rotSpeedY + index * 1.1f + yawOffset * 0.5f,
            rotZ = timeSec * poly.rotSpeedZ,
            edgeColor = edgeCol,
            vertexColor = if (poly.goldTint > 0.4f) HoloGoldBright else HoloWhite,
            hasInnerCore = poly.hasInnerCore
        )

        // Connect plexus lines from the top-right hero icosahedron to top of viewport
        if (index == 0) {
            drawLine(
                color = primaryColor.copy(alpha = 0.35f),
                start = Offset(px, py - pr),
                end = Offset(px - pr * 0.9f, 0f),
                strokeWidth = 1.2f,
                blendMode = BlendMode.Plus
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.28f),
                start = Offset(px - pr * 0.5f, py - pr * 0.6f),
                end = Offset(centerX - globeRadius * 0.35f, 0f),
                strokeWidth = 1.0f,
                blendMode = BlendMode.Plus
            )
        }
    }

    // 8. Bottom Plexus Constellation Mesh connecting behind the 4 navigation buttons
    drawBottomPlexusNetwork(
        w = w,
        h = h,
        timeSec = timeSec,
        primaryColor = primaryColor
    )
}

private fun DrawScope.drawOrbitalRingsPass(
    centerX: Float,
    centerY: Float,
    globeRadius: Float,
    timeSec: Float,
    yawOffset: Float,
    pitchOffset: Float,
    frontPass: Boolean,
    primaryColor: Color,
    brightColor: Color,
    accentGold: Color
) {
    // Ring 1: Main Wide Cyan Tilted Plasma Ring (matching the iconic diagonal cyan ring)
    draw3DOrbitalRing(
        cx = centerX,
        cy = centerY,
        rx = globeRadius * 1.56f,
        ry = globeRadius * 1.56f,
        tiltX = -1.16f + pitchOffset * 0.3f,
        tiltZ = -0.46f + yawOffset * 0.15f,
        spinOffset = timeSec * 0.65f,
        frontPass = frontPass,
        ringColor = primaryColor,
        highlightColor = HoloWhite,
        strokeWidth = 3.2f,
        hasGoldenSparks = false
    )
    // Ring 1B: Concentric outer track of the main cyan ring
    draw3DOrbitalRing(
        cx = centerX,
        cy = centerY,
        rx = globeRadius * 1.66f,
        ry = globeRadius * 1.66f,
        tiltX = -1.14f + pitchOffset * 0.3f,
        tiltZ = -0.44f + yawOffset * 0.15f,
        spinOffset = -timeSec * 0.45f,
        frontPass = frontPass,
        ringColor = brightColor.copy(alpha = 0.75f),
        highlightColor = HoloWhite,
        strokeWidth = 1.8f,
        hasGoldenSparks = false
    )
    // Ring 2: Secondary intersecting cyan ring with steeper tilt
    draw3DOrbitalRing(
        cx = centerX,
        cy = centerY,
        rx = globeRadius * 1.44f,
        ry = globeRadius * 1.44f,
        tiltX = -1.05f + pitchOffset * 0.25f,
        tiltZ = -0.22f + yawOffset * 0.15f,
        spinOffset = timeSec * 0.85f + 1.5f,
        frontPass = frontPass,
        ringColor = primaryColor.copy(alpha = 0.8f),
        highlightColor = accentGold,
        strokeWidth = 2.2f,
        hasGoldenSparks = true
    )
    // Ring 3: Counter-tilted Golden/Orange & Cyan high-inclination ring
    draw3DOrbitalRing(
        cx = centerX,
        cy = centerY,
        rx = globeRadius * 1.38f,
        ry = globeRadius * 1.38f,
        tiltX = 1.12f - pitchOffset * 0.25f,
        tiltZ = 0.62f - yawOffset * 0.15f,
        spinOffset = -timeSec * 0.55f + 0.8f,
        frontPass = frontPass,
        ringColor = accentGold.copy(alpha = 0.72f),
        highlightColor = HoloGoldBright,
        strokeWidth = 1.9f,
        hasGoldenSparks = true
    )
}

private fun DrawScope.draw3DOrbitalRing(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    tiltX: Float,
    tiltZ: Float,
    spinOffset: Float,
    frontPass: Boolean,
    ringColor: Color,
    highlightColor: Color,
    strokeWidth: Float,
    hasGoldenSparks: Boolean
) {
    val segments = 96
    val cosX = cos(tiltX)
    val sinX = sin(tiltX)
    val cosZ = cos(tiltZ)
    val sinZ = sin(tiltZ)

    var prevPoint: Offset? = null
    var prevZ = 0f

    for (i in 0..segments) {
        val theta = (i.toFloat() / segments) * 2f * PI.toFloat()
        val lx = rx * cos(theta)
        val ly = ry * sin(theta)

        // Rotate in X (pitch)
        val y1 = ly * cosX
        val z1 = ly * sinX

        // Rotate in Z (roll/diagonal tilt)
        val x2 = lx * cosZ - y1 * sinZ
        val y2 = lx * sinZ + y1 * cosZ

        val currentPoint = Offset(cx + x2, cy + y2)
        val isFront = z1 >= 0f

        if (i > 0 && isFront == frontPass) {
            val prev = prevPoint
            if (prev != null) {
                val pulseWave = sin(theta * 2f - spinOffset * 2.5f)
                val alphaBoost = if (frontPass) 0.78f else 0.34f
                val segColor = if (pulseWave > 0.72f) {
                    highlightColor.copy(alpha = (alphaBoost + 0.2f).coerceAtMost(1f))
                } else {
                    ringColor.copy(alpha = alphaBoost * (0.65f + 0.35f * pulseWave))
                }

                // Outer glow line
                drawLine(
                    color = ringColor.copy(alpha = alphaBoost * 0.28f),
                    start = prev,
                    end = currentPoint,
                    strokeWidth = strokeWidth * 3.2f,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )
                // Core plasma line
                drawLine(
                    color = segColor,
                    start = prev,
                    end = currentPoint,
                    strokeWidth = if (pulseWave > 0.72f) strokeWidth * 1.35f else strokeWidth,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )

                // Orbiting golden/cyan spark nodes
                if (hasGoldenSparks && i % 12 == 0 && pulseWave > 0.2f) {
                    drawCircle(
                        color = highlightColor.copy(alpha = 0.9f),
                        radius = strokeWidth * 1.4f,
                        center = currentPoint,
                        blendMode = BlendMode.Plus
                    )
                }
            }
        }
        prevPoint = currentPoint
        prevZ = z1
    }
}

fun DrawScope.drawPolyhedron3D(
    cx: Float,
    cy: Float,
    radius: Float,
    rotX: Float,
    rotY: Float,
    rotZ: Float,
    edgeColor: Color,
    vertexColor: Color,
    hasInnerCore: Boolean
) {
    val cosX = cos(rotX)
    val sinX = sin(rotX)
    val cosY = cos(rotY)
    val sinY = sin(rotY)
    val cosZ = cos(rotZ)
    val sinZ = sin(rotZ)

    fun project(v: Vec3, scale: Float): Pair<Offset, Float> {
        // Rotate X
        val y1 = v.y * cosX - v.z * sinX
        val z1 = v.y * sinX + v.z * cosX
        // Rotate Y
        val x2 = v.x * cosY + z1 * sinY
        val z2 = -v.x * sinY + z1 * cosY
        // Rotate Z
        val x3 = x2 * cosZ - y1 * sinZ
        val y3 = x2 * sinZ + y1 * cosZ

        val perspective = 1f + z2 * 0.18f
        return Offset(
            cx + x3 * radius * scale * perspective,
            cy + y3 * radius * scale * perspective
        ) to z2
    }

    val projectedOuter = HoloGeometryCache.icosahedronVertices.map { project(it, 1f) }

    // Optional glowing inner core polyhedron (like the nested top-right & bottom-left polyhedrons)
    if (hasInnerCore) {
        val projectedInner = HoloGeometryCache.icosahedronVertices.map { project(it, 0.54f) }
        val facetPath = Path()
        for ((i0, i1, i2) in HoloGeometryCache.icosahedronFaces) {
            val (p0, z0) = projectedInner[i0]
            val (p1, z1) = projectedInner[i1]
            val (p2, z2) = projectedInner[i2]
            val avgZ = (z0 + z1 + z2) / 3f
            if (avgZ > -0.2f) {
                facetPath.reset()
                facetPath.moveTo(p0.x, p0.y)
                facetPath.lineTo(p1.x, p1.y)
                facetPath.lineTo(p2.x, p2.y)
                facetPath.close()
                drawPath(
                    path = facetPath,
                    color = edgeColor.copy(alpha = (0.14f + avgZ * 0.16f).coerceIn(0.05f, 0.34f)),
                    blendMode = BlendMode.Plus
                )
            }
        }
        // Connect inner vertices to outer vertices with fine struts
        for (idx in projectedOuter.indices) {
            drawLine(
                color = edgeColor.copy(alpha = 0.25f),
                start = projectedInner[idx].first,
                end = projectedOuter[idx].first,
                strokeWidth = 0.9f,
                blendMode = BlendMode.Plus
            )
        }
    }

    // Draw outer wireframe edges
    for ((a, b) in HoloGeometryCache.icosahedronEdges) {
        val (pA, zA) = projectedOuter[a]
        val (pB, zB) = projectedOuter[b]
        val depthFactor = (((zA + zB) * 0.5f) + 1.2f) / 2.2f
        val alpha = (0.28f + 0.65f * depthFactor).coerceIn(0.18f, 0.95f)

        drawLine(
            color = edgeColor.copy(alpha = alpha * 0.35f),
            start = pA,
            end = pB,
            strokeWidth = 3.2f,
            blendMode = BlendMode.Plus
        )
        drawLine(
            color = edgeColor.copy(alpha = alpha),
            start = pA,
            end = pB,
            strokeWidth = 1.3f,
            blendMode = BlendMode.Plus
        )
    }

    // Draw glowing vertex dots
    for ((pt, z) in projectedOuter) {
        if (z > -0.4f) {
            drawCircle(
                color = vertexColor.copy(alpha = 0.85f),
                radius = 2.2f,
                center = pt,
                blendMode = BlendMode.Plus
            )
        }
    }
}

private fun DrawScope.drawBottomPlexusNetwork(
    w: Float,
    h: Float,
    timeSec: Float,
    primaryColor: Color
) {
    val nodes = listOf(
        Offset(w * 0.22f, h * 0.78f),
        Offset(w * 0.28f, h * 0.72f),
        Offset(w * 0.34f, h * 0.85f),
        Offset(w * 0.41f, h * 0.79f),
        Offset(w * 0.47f, h * 0.86f),
        Offset(w * 0.54f, h * 0.78f),
        Offset(w * 0.61f, h * 0.84f),
        Offset(w * 0.69f, h * 0.77f),
        Offset(w * 0.75f, h * 0.86f),
        Offset(w * 0.78f, h * 0.74f)
    ).mapIndexed { idx, base ->
        Offset(
            base.x + sin(timeSec * 0.8f + idx * 0.9f) * 5f,
            base.y + cos(timeSec * 0.7f + idx * 1.1f) * 4f
        )
    }

    for (i in nodes.indices) {
        for (j in i + 1 until nodes.size) {
            val d = (nodes[i] - nodes[j]).getDistance()
            if (d < w * 0.14f) {
                val alpha = (1f - d / (w * 0.14f)) * 0.36f
                drawLine(
                    color = primaryColor.copy(alpha = alpha),
                    start = nodes[i],
                    end = nodes[j],
                    strokeWidth = 1.1f,
                    blendMode = BlendMode.Plus
                )
            }
        }
        drawCircle(
            color = HoloWhite.copy(alpha = 0.7f),
            radius = 2.0f,
            center = nodes[i],
            blendMode = BlendMode.Plus
        )
    }
}
