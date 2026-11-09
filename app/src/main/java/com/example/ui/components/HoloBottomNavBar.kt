package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloRed
import com.example.ui.theme.HoloRedBright
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.RajdhaniFontFamily

private enum class HoloButtonIconType {
    CALL,
    VIDEO,
    SECURE,
    DISCONNECT
}

@Composable
fun HoloBottomNavBar(
    isCallActive: Boolean,
    isVideoActive: Boolean,
    isSecureActive: Boolean,
    isDisconnected: Boolean,
    isCompactHeight: Boolean,
    onCallClick: () -> Unit,
    onVideoClick: () -> Unit,
    onSecureClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boxSize = if (isCompactHeight) 56.dp else 66.dp
    val spacing = if (isCompactHeight) 22.dp else 30.dp

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = if (isCompactHeight) 4.dp else 8.dp)
    ) {
        HoloGlassSquareButton(
            label = stringResource(R.string.action_call),
            contentDesc = stringResource(R.string.cd_call_button),
            testTag = "btn_call",
            iconType = HoloButtonIconType.CALL,
            isActive = isCallActive,
            isDanger = false,
            boxSize = boxSize,
            onClick = onCallClick
        )

        HoloGlassSquareButton(
            label = stringResource(R.string.action_video),
            contentDesc = stringResource(R.string.cd_video_button),
            testTag = "btn_video",
            iconType = HoloButtonIconType.VIDEO,
            isActive = isVideoActive,
            isDanger = false,
            boxSize = boxSize,
            onClick = onVideoClick
        )

        HoloGlassSquareButton(
            label = stringResource(R.string.action_secure),
            contentDesc = stringResource(R.string.cd_secure_button),
            testTag = "btn_secure",
            iconType = HoloButtonIconType.SECURE,
            isActive = isSecureActive,
            isDanger = false,
            boxSize = boxSize,
            onClick = onSecureClick
        )

        HoloGlassSquareButton(
            label = stringResource(R.string.action_disconnect),
            contentDesc = stringResource(R.string.cd_disconnect_button),
            testTag = "btn_disconnect",
            iconType = HoloButtonIconType.DISCONNECT,
            isActive = isDisconnected,
            isDanger = true,
            boxSize = boxSize,
            onClick = onDisconnectClick
        )
    }
}

@Composable
private fun HoloGlassSquareButton(
    label: String,
    contentDesc: String,
    testTag: String,
    iconType: HoloButtonIconType,
    isActive: Boolean,
    isDanger: Boolean,
    boxSize: Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.07f else 1.0f,
        label = "button_scale"
    )

    val baseNeon = if (isDanger) HoloRed else HoloCyan
    val brightNeon = if (isDanger) HoloRedBright else HoloCyanBright
    val shape = RoundedCornerShape(9.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .semantics {
                role = Role.Button
                contentDescription = contentDesc
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(boxSize)
                .drawBehind {
                    // Outer neon drop-bloom
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                baseNeon.copy(alpha = if (isActive) 0.52f else 0.34f),
                                baseNeon.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.maxDimension * 0.78f
                        ),
                        topLeft = Offset(-size.width * 0.18f, -size.height * 0.18f),
                        size = Size(size.width * 1.36f, size.height * 1.36f),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        blendMode = BlendMode.Plus
                    )
                }
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            baseNeon.copy(alpha = if (isActive) 0.36f else 0.22f),
                            Color(0xFF06162A).copy(alpha = 0.65f),
                            baseNeon.copy(alpha = if (isActive) 0.28f else 0.14f)
                        ),
                        start = Offset.Zero,
                        end = Offset(200f, 200f)
                    )
                )
                .border(
                    width = if (isActive) 2.dp else 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            HoloWhite.copy(alpha = 0.85f),
                            brightNeon.copy(alpha = 0.85f),
                            baseNeon.copy(alpha = 0.45f)
                        )
                    ),
                    shape = shape
                )
                .padding(4.dp)
                .border(
                    width = 0.7.dp,
                    color = baseNeon.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Canvas(modifier = Modifier.size(boxSize * 0.56f)) {
                when (iconType) {
                    HoloButtonIconType.CALL -> drawCallHandsetIcon(brightNeon)
                    HoloButtonIconType.VIDEO -> drawVideoCameraIcon(brightNeon)
                    HoloButtonIconType.SECURE -> drawPadlockIcon(brightNeon)
                    HoloButtonIconType.DISCONNECT -> drawDisconnectXIcon(HoloRedBright)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            letterSpacing = 1.6.sp,
            color = if (isDanger) HoloRedBright else HoloCyanBright
        )
    }
}

private fun DrawScope.drawCallHandsetIcon(tint: Color) {
    val w = size.width
    val h = size.height

    // Classic sci-fi handset silhouette + 2 acoustic wave arcs in top-right
    val handsetPath = Path().apply {
        moveTo(w * 0.22f, h * 0.14f)
        cubicTo(w * 0.06f, h * 0.26f, w * 0.08f, h * 0.54f, w * 0.27f, h * 0.73f)
        cubicTo(w * 0.46f, h * 0.92f, w * 0.74f, h * 0.94f, w * 0.86f, h * 0.78f)
        lineTo(w * 0.72f, h * 0.62f)
        lineTo(w * 0.58f, h * 0.71f)
        cubicTo(w * 0.46f, h * 0.64f, w * 0.36f, h * 0.54f, w * 0.29f, h * 0.42f)
        lineTo(w * 0.38f, h * 0.28f)
        close()
    }

    drawPath(
        path = handsetPath,
        color = tint.copy(alpha = 0.4f),
        style = Stroke(width = 6f),
        blendMode = BlendMode.Plus
    )
    drawPath(
        path = handsetPath,
        color = HoloWhite.copy(alpha = 0.92f),
        blendMode = BlendMode.Plus
    )

    // Two concentric signal arcs in upper-right
    drawArc(
        color = HoloWhite.copy(alpha = 0.92f),
        startAngle = -65f,
        sweepAngle = 75f,
        useCenter = false,
        topLeft = Offset(w * 0.36f, h * 0.14f),
        size = Size(w * 0.36f, h * 0.36f),
        style = Stroke(width = 3.2f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )
    drawArc(
        color = tint,
        startAngle = -68f,
        sweepAngle = 80f,
        useCenter = false,
        topLeft = Offset(w * 0.26f, h * 0.04f),
        size = Size(w * 0.56f, h * 0.56f),
        style = Stroke(width = 3.2f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )
}

private fun DrawScope.drawVideoCameraIcon(tint: Color) {
    val w = size.width
    val h = size.height

    // Rounded rectangular camera body on left
    val bodyRect = RoundRect(
        rect = Rect(w * 0.06f, h * 0.22f, w * 0.66f, h * 0.78f),
        cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
    )
    val bodyPath = Path().apply { addRoundRect(bodyRect) }

    // Inner dark screen cutout on the left side of camera body (matching the reference icon)
    val cutoutRect = RoundRect(
        rect = Rect(w * 0.15f, h * 0.34f, w * 0.35f, h * 0.66f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
    val cutoutPath = Path().apply { addRoundRect(cutoutRect) }
    val finalBody = Path.combine(PathOperation.Difference, bodyPath, cutoutPath)

    // Trapezoidal lens cone on the right
    val lensPath = Path().apply {
        moveTo(w * 0.71f, h * 0.36f)
        lineTo(w * 0.95f, h * 0.23f)
        lineTo(w * 0.95f, h * 0.77f)
        lineTo(w * 0.71f, h * 0.64f)
        close()
    }

    drawPath(finalBody, tint.copy(alpha = 0.45f), style = Stroke(width = 6f), blendMode = BlendMode.Plus)
    drawPath(lensPath, tint.copy(alpha = 0.45f), style = Stroke(width = 6f), blendMode = BlendMode.Plus)
    drawPath(finalBody, HoloWhite.copy(alpha = 0.92f), blendMode = BlendMode.Plus)
    drawPath(lensPath, HoloWhite.copy(alpha = 0.92f), blendMode = BlendMode.Plus)
}

private fun DrawScope.drawPadlockIcon(tint: Color) {
    val w = size.width
    val h = size.height

    // Top shackle U-arch
    drawArc(
        color = tint.copy(alpha = 0.45f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.26f, h * 0.08f),
        size = Size(w * 0.48f, h * 0.48f),
        style = Stroke(width = 9f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )
    drawArc(
        color = HoloWhite.copy(alpha = 0.92f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.26f, h * 0.08f),
        size = Size(w * 0.48f, h * 0.48f),
        style = Stroke(width = 5.2f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )
    // Shackle vertical legs
    drawLine(
        color = HoloWhite.copy(alpha = 0.92f),
        start = Offset(w * 0.26f, h * 0.32f),
        end = Offset(w * 0.26f, h * 0.46f),
        strokeWidth = 5.2f,
        blendMode = BlendMode.Plus
    )
    drawLine(
        color = HoloWhite.copy(alpha = 0.92f),
        start = Offset(w * 0.74f, h * 0.32f),
        end = Offset(w * 0.74f, h * 0.46f),
        strokeWidth = 5.2f,
        blendMode = BlendMode.Plus
    )

    // Padlock body with central keyhole cutout
    val lockBody = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(w * 0.16f, h * 0.44f, w * 0.84f, h * 0.94f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
        )
    }
    val keyhole = Path().apply {
        addOval(Rect(w * 0.43f, h * 0.56f, w * 0.57f, h * 0.70f))
        addRect(Rect(w * 0.46f, h * 0.66f, w * 0.54f, h * 0.82f))
    }
    val finalLock = Path.combine(PathOperation.Difference, lockBody, keyhole)

    drawPath(finalLock, tint.copy(alpha = 0.45f), style = Stroke(width = 6f), blendMode = BlendMode.Plus)
    drawPath(finalLock, HoloWhite.copy(alpha = 0.92f), blendMode = BlendMode.Plus)
}

private fun DrawScope.drawDisconnectXIcon(tint: Color) {
    val w = size.width
    val h = size.height

    // Bold diagonal X cross with intense red glow matching the DISCONNECT icon
    rotate(degrees = 45f, pivot = center) {
        val bar1 = RoundRect(
            rect = Rect(w * 0.10f, h * 0.40f, w * 0.90f, h * 0.60f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        val bar2 = RoundRect(
            rect = Rect(w * 0.40f, h * 0.10f, w * 0.60f, h * 0.90f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        val xPath = Path().apply {
            addRoundRect(bar1)
            op(this, Path().apply { addRoundRect(bar2) }, PathOperation.Union)
        }
        drawPath(
            path = xPath,
            color = HoloRed.copy(alpha = 0.55f),
            style = Stroke(width = 8f),
            blendMode = BlendMode.Plus
        )
        drawPath(
            path = xPath,
            color = tint,
            blendMode = BlendMode.Plus
        )
    }
}
