package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BustAvatarType
import com.example.model.HoloContact
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloGold
import com.example.ui.theme.HoloGoldBright
import com.example.ui.theme.HoloOrangeSpark
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HoloLeftContactsPanel(
    contacts: List<HoloContact>,
    selectedContactId: String,
    timeSec: Float,
    isCompactHeight: Boolean,
    onSelectContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarSize: Dp = if (isCompactHeight) 62.dp else 76.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = 15f
                cameraDistance = 14f * density
            }
            .drawBehind {
                val w = size.width
                val h = size.height

                // Top angled HUD header line with right drop tick
                drawLine(
                    color = HoloCyan.copy(alpha = 0.78f),
                    start = Offset(0f, 2f),
                    end = Offset(w * 0.96f, 10f),
                    strokeWidth = 1.8f,
                    blendMode = BlendMode.Plus
                )
                drawLine(
                    color = HoloCyan.copy(alpha = 0.78f),
                    start = Offset(w * 0.96f, 10f),
                    end = Offset(w * 0.96f, 22f),
                    strokeWidth = 1.8f,
                    blendMode = BlendMode.Plus
                )

                // Right vertical framing bracket line separating CONTACTS from the 3D globe
                val rightLineX = w * 0.96f
                val topBracketY = h * 0.20f
                val botBracketY = h * 0.92f
                drawLine(
                    color = HoloCyan.copy(alpha = 0.65f),
                    start = Offset(rightLineX - 8f, topBracketY),
                    end = Offset(rightLineX, topBracketY),
                    strokeWidth = 1.6f
                )
                drawLine(
                    color = HoloCyan.copy(alpha = 0.55f),
                    start = Offset(rightLineX, topBracketY),
                    end = Offset(rightLineX, botBracketY),
                    strokeWidth = 1.4f
                )
                drawLine(
                    color = HoloCyan.copy(alpha = 0.65f),
                    start = Offset(rightLineX - 8f, botBracketY),
                    end = Offset(rightLineX, botBracketY),
                    strokeWidth = 1.6f
                )

                // Bottom left horizontal bracket line
                drawLine(
                    color = HoloCyan.copy(alpha = 0.75f),
                    start = Offset(0f, h * 0.97f),
                    end = Offset(w * 0.36f, h * 0.97f),
                    strokeWidth = 2f,
                    blendMode = BlendMode.Plus
                )
            }
            .padding(top = 10.dp, bottom = 8.dp, end = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 12.dp)
        ) {
            // Header: Vertical Cyan Bar + CONTACTS + Underline + KONTAKTY
            Column(modifier = Modifier.fillMaxWidth(0.92f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Glowing vertical cyan accent bar
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .height(if (isCompactHeight) 22.dp else 28.dp)
                            .background(HoloCyan)
                            .drawBehind {
                                drawRect(
                                    color = HoloCyan.copy(alpha = 0.45f),
                                    size = size.copy(width = size.width + 6f, height = size.height + 6f),
                                    topLeft = Offset(-3f, -3f),
                                    blendMode = BlendMode.Plus
                                )
                            }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.contacts_title),
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompactHeight) 22.sp else 27.sp,
                        letterSpacing = 2.5.sp,
                        color = HoloWhite
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                // Horizontal cyan underline below CONTACTS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(HoloCyan, HoloCyan.copy(alpha = 0.75f), Color.Transparent)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.contacts_subtitle),
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    letterSpacing = 2.2.sp,
                    color = HoloCyanBright.copy(alpha = 0.9f),
                    modifier = Modifier.padding(start = 34.dp)
                )
            }

            // 3 Vertically Stacked Contact Rows (USER_01, USER_02, USER_03)
            Column(
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                contacts.forEachIndexed { index, contact ->
                    val isSelected = contact.id == selectedContactId
                    HoloContactRow(
                        contact = contact,
                        isSelected = isSelected,
                        avatarSize = avatarSize,
                        timeSec = timeSec + index * 0.9f,
                        onClick = { onSelectContact(contact.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HoloContactRow(
    contact: HoloContact,
    isSelected: Boolean,
    avatarSize: Dp,
    timeSec: Float,
    onClick: () -> Unit
) {
    val frameColor = if (isSelected) HoloCyanBright else HoloCyan.copy(alpha = 0.82f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_row_${contact.id.lowercase()}")
            .semantics {
                role = Role.Button
                contentDescription = "Select contact ${contact.label}"
            }
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        // Square cyan wireframe bust container with disintegrating gold/orange sparks on left edge
        Box(
            modifier = Modifier
                .size(avatarSize)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HoloCyan.copy(alpha = if (isSelected) 0.22f else 0.10f),
                            Color(0xFF031022).copy(alpha = 0.65f)
                        )
                    )
                )
                .border(
                    width = if (isSelected) 1.8.dp else 1.2.dp,
                    color = frameColor
                )
                .padding(3.dp)
                .border(
                    width = 0.7.dp,
                    color = HoloCyan.copy(alpha = 0.45f)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Disintegrating orange/gold particle sparks on the left edge of the frame
                drawAvatarLeftGoldSparks(timeSec = timeSec)

                // 2. 3D Wireframe Human Bust Avatar
                drawWireframeHumanBust3D(
                    avatarType = contact.avatarType,
                    timeSec = timeSec,
                    isSelected = isSelected
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side: USER_01 / USER_02 / USER_03 + Horizontal Cyan Line + [ • detail
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contact.label,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 1.6.sp,
                color = if (isSelected) HoloWhite else HoloWhite.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(3.dp))
            // Horizontal cyan underline extending across the label width
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(if (isSelected) 2.dp else 1.4.dp)
                    .background(
                        if (isSelected) HoloCyanBright else HoloCyan.copy(alpha = 0.85f)
                    )
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Small [ • detail below the underline
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(width = 24.dp, height = 14.dp)) {
                    val bracketColor = if (isSelected) HoloCyanBright else HoloCyan.copy(alpha = 0.85f)
                    val h = size.height
                    // Draw '[' bracket
                    drawLine(
                        color = bracketColor,
                        start = Offset(6f, 0f),
                        end = Offset(1f, 0f),
                        strokeWidth = 1.6f
                    )
                    drawLine(
                        color = bracketColor,
                        start = Offset(1f, 0f),
                        end = Offset(1f, h),
                        strokeWidth = 1.6f
                    )
                    drawLine(
                        color = bracketColor,
                        start = Offset(1f, h),
                        end = Offset(6f, h),
                        strokeWidth = 1.6f
                    )
                    // Draw glowing dot '•'
                    drawCircle(
                        color = if (isSelected) HoloWhite else HoloCyanBright,
                        radius = if (isSelected) 3.0f else 2.3f,
                        center = Offset(14f, h * 0.5f),
                        blendMode = BlendMode.Plus
                    )
                }
                if (isSelected) {
                    Text(
                        text = "LOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = HoloCyanBright.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAvatarLeftGoldSparks(timeSec: Float) {
    val w = size.width
    val h = size.height
    for (i in 0 until 18) {
        val seed = i * 1.37f
        val drift = ((timeSec * 0.7f + seed) % 1f)
        val sx = (0.18f - drift * 0.34f) * w
        val sy = ((i / 18f) * 0.9f + 0.05f + sin(timeSec * 2f + seed) * 0.04f) * h
        val alpha = (1f - drift).coerceIn(0.15f, 0.95f)
        val radius = 1.2f + (i % 3) * 0.8f

        drawCircle(
            color = HoloOrangeSpark.copy(alpha = alpha * 0.45f),
            radius = radius * 2.2f,
            center = Offset(sx, sy),
            blendMode = BlendMode.Plus
        )
        drawCircle(
            color = if (i % 2 == 0) HoloGoldBright.copy(alpha = alpha) else HoloGold.copy(alpha = alpha),
            radius = radius,
            center = Offset(sx, sy),
            blendMode = BlendMode.Plus
        )
    }
}

/**
 * Renders a 3D triangulated wireframe human bust avatar (head, facial bridge, neck, shoulders,
 * and optional cyber-hood for USER_02) matching the 3 cyan wireframe busts in the reference UI.
 */
private fun DrawScope.drawWireframeHumanBust3D(
    avatarType: BustAvatarType,
    timeSec: Float,
    isSelected: Boolean
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val yaw = sin(timeSec * 0.85f) * 0.22f
    val cosY = cos(yaw)
    val sinY = sin(yaw)

    fun proj(x3d: Float, y3d: Float, z3d: Float): Offset {
        val rx = x3d * cosY + z3d * sinY
        val rz = -x3d * sinY + z3d * cosY
        val p = 1f + rz * 0.18f
        return Offset(
            x = cx + rx * w * 0.44f * p,
            y = h * 0.52f + y3d * h * 0.46f
        )
    }

    val lineAlpha = if (isSelected) 0.85f else 0.65f
    val wireColor = HoloCyan.copy(alpha = lineAlpha)
    val brightWire = HoloCyanBright.copy(alpha = (lineAlpha + 0.15f).coerceAtMost(1f))

    // Construct symmetric 3D rings for Cranium, Brow, Cheekbones, Jaw, Chin, Neck, and Shoulders
    data class BustRing(val y: Float, val rx: Float, val rz: Float, val yDipFront: Float = 0f)

    val rings = when (avatarType) {
        BustAvatarType.USER_01_MALE -> listOf(
            BustRing(y = -0.82f, rx = 0.22f, rz = 0.24f), // Crown
            BustRing(y = -0.66f, rx = 0.36f, rz = 0.38f), // Upper cranium
            BustRing(y = -0.46f, rx = 0.38f, rz = 0.40f, yDipFront = 0.02f), // Brow ridge
            BustRing(y = -0.26f, rx = 0.35f, rz = 0.38f, yDipFront = 0.03f), // Cheekbones / Nose
            BustRing(y = -0.06f, rx = 0.28f, rz = 0.30f, yDipFront = 0.05f), // Jawline
            BustRing(y = 0.10f, rx = 0.16f, rz = 0.18f, yDipFront = 0.06f),  // Chin & upper neck
            BustRing(y = 0.34f, rx = 0.23f, rz = 0.22f),  // Neck base
            BustRing(y = 0.58f, rx = 0.72f, rz = 0.34f, yDipFront = 0.08f),  // Collarbone / Shoulders
            BustRing(y = 0.92f, rx = 0.92f, rz = 0.38f, yDipFront = 0.06f)   // Lower bust chest
        )
        BustAvatarType.USER_02_HOODED -> listOf(
            BustRing(y = -0.86f, rx = 0.26f, rz = 0.28f), // Hood peak
            BustRing(y = -0.66f, rx = 0.44f, rz = 0.42f), // Wide hood upper
            BustRing(y = -0.44f, rx = 0.48f, rz = 0.42f, yDipFront = 0.02f), // Hood temple
            BustRing(y = -0.22f, rx = 0.46f, rz = 0.38f, yDipFront = 0.04f), // Hood mid
            BustRing(y = -0.02f, rx = 0.38f, rz = 0.32f, yDipFront = 0.06f), // Jaw / cowl
            BustRing(y = 0.16f, rx = 0.26f, rz = 0.24f, yDipFront = 0.05f),  // Neck cowl
            BustRing(y = 0.40f, rx = 0.42f, rz = 0.28f),  // Collar flare
            BustRing(y = 0.64f, rx = 0.78f, rz = 0.36f, yDipFront = 0.07f),  // Shoulders
            BustRing(y = 0.92f, rx = 0.94f, rz = 0.38f, yDipFront = 0.05f)   // Lower bust
        )
        BustAvatarType.USER_03_COMMANDER -> listOf(
            BustRing(y = -0.80f, rx = 0.24f, rz = 0.26f),
            BustRing(y = -0.64f, rx = 0.38f, rz = 0.40f),
            BustRing(y = -0.44f, rx = 0.40f, rz = 0.42f, yDipFront = 0.02f),
            BustRing(y = -0.24f, rx = 0.37f, rz = 0.39f, yDipFront = 0.03f),
            BustRing(y = -0.04f, rx = 0.31f, rz = 0.32f, yDipFront = 0.05f),
            BustRing(y = 0.12f, rx = 0.19f, rz = 0.20f, yDipFront = 0.05f),
            BustRing(y = 0.36f, rx = 0.26f, rz = 0.24f),
            BustRing(y = 0.60f, rx = 0.78f, rz = 0.36f, yDipFront = 0.08f),
            BustRing(y = 0.92f, rx = 0.95f, rz = 0.40f, yDipFront = 0.06f)
        )
    }

    val radialCols = 10
    val gridPoints = Array(rings.size) { rIdx ->
        val ring = rings[rIdx]
        Array(radialCols) { cIdx ->
            // Angle from -PI/2 (left side) through 0 (front face) to +PI/2 (right side)
            val frac = cIdx.toFloat() / (radialCols - 1)
            val theta = (frac - 0.5f) * 3.14159f
            val x = sin(theta) * ring.rx
            val z = cos(theta) * ring.rz
            val y = ring.y + cos(theta) * ring.yDipFront
            proj(x, y, z)
        }
    }

    // Draw triangulated geodesic mesh (horizontal, vertical, and diagonal facial/shoulder struts)
    for (r in rings.indices) {
        for (c in 0 until radialCols) {
            val p = gridPoints[r][c]
            // Horizontal ring edge
            if (c + 1 < radialCols) {
                val pRight = gridPoints[r][c + 1]
                drawLine(
                    color = wireColor,
                    start = p,
                    end = pRight,
                    strokeWidth = 1.0f,
                    blendMode = BlendMode.Plus
                )
            }
            // Vertical meridian & diagonal triangulation
            if (r + 1 < rings.size) {
                val pDown = gridPoints[r + 1][c]
                drawLine(
                    color = wireColor,
                    start = p,
                    end = pDown,
                    strokeWidth = 1.0f,
                    blendMode = BlendMode.Plus
                )
                if (c + 1 < radialCols) {
                    val pDiag = if (c < radialCols / 2) gridPoints[r + 1][c + 1] else gridPoints[r][c + 1]
                    val pDiagStart = if (c < radialCols / 2) p else gridPoints[r + 1][c]
                    drawLine(
                        color = wireColor.copy(alpha = lineAlpha * 0.65f),
                        start = pDiagStart,
                        end = pDiag,
                        strokeWidth = 0.8f,
                        blendMode = BlendMode.Plus
                    )
                }
            }
            // Glowing vertex points at key facial landmarks
            if ((r + c) % 2 == 0) {
                drawCircle(
                    color = brightWire,
                    radius = 1.4f,
                    center = p,
                    blendMode = BlendMode.Plus
                )
            }
        }
    }

    // For USER_02 (Hooded avatar), draw inner facial contour inside the outer hood frame
    if (avatarType == BustAvatarType.USER_02_HOODED) {
        val facePath = Path().apply {
            val pTop = proj(0f, -0.58f, 0.35f)
            val pLeftBrow = proj(-0.24f, -0.42f, 0.32f)
            val pLeftJaw = proj(-0.18f, -0.08f, 0.28f)
            val pChin = proj(0f, 0.08f, 0.32f)
            val pRightJaw = proj(0.18f, -0.08f, 0.28f)
            val pRightBrow = proj(0.24f, -0.42f, 0.32f)
            moveTo(pTop.x, pTop.y)
            lineTo(pLeftBrow.x, pLeftBrow.y)
            lineTo(pLeftJaw.x, pLeftJaw.y)
            lineTo(pChin.x, pChin.y)
            lineTo(pRightJaw.x, pRightJaw.y)
            lineTo(pRightBrow.x, pRightBrow.y)
            close()
        }
        drawPath(
            path = facePath,
            color = HoloWhite.copy(alpha = 0.7f),
            style = Stroke(width = 1.3f),
            blendMode = BlendMode.Plus
        )
    }
}
