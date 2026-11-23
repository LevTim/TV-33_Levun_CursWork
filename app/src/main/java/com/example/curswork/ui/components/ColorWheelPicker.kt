package com.example.curswork.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun ColorWheelPicker(
    selectedColorInt: Int,
    isPowered: Boolean,
    modifier: Modifier = Modifier,
    wheelSize: Dp = 220.dp,
    onColorSelected: (Int) -> Unit
) {
    val activeColor = Color(selectedColorInt)
    var selectorOffset by remember { mutableStateOf<Offset?>(null) }

    val hueColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    }

    fun calculateColorAtOffset(touch: Offset, center: Offset, radius: Float): Pair<Int, Offset> {
        val dx = touch.x - center.x
        val dy = touch.y - center.y
        val distance = hypot(dx, dy).coerceAtMost(radius)
        val angleRad = atan2(dy, dx)
        var degrees = Math.toDegrees(angleRad.toDouble()).toFloat()
        if (degrees < 0f) degrees += 360f

        val saturation = (distance / radius).coerceIn(0f, 1f)
        val color = Color.hsv(hue = degrees, saturation = saturation, value = 1f)
        val clampedPos = Offset(
            x = center.x + distance * cos(angleRad),
            y = center.y + distance * sin(angleRad)
        )
        return color.toArgb() to clampedPos
    }

    Box(
        modifier = modifier.size(wheelSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(wheelSize)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = minOf(size.width, size.height) / 2f
                        val (colorInt, pos) = calculateColorAtOffset(offset, center, radius)
                        selectorOffset = pos
                        onColorSelected(colorInt)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = minOf(size.width, size.height) / 2f
                        val (colorInt, pos) = calculateColorAtOffset(change.position, center, radius)
                        selectorOffset = pos
                        onColorSelected(colorInt)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Зовнішній спектр відтінків (HSV SweepGradient)
            drawCircle(
                brush = Brush.sweepGradient(colors = hueColors, center = center),
                radius = radius,
                center = center
            )

            // Радіальний білий градієнт від центру для насиченості
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Центральне коло індикатора обраного кольору (відповідно до рис. 5)
            val innerRadius = radius * 0.34f
            drawCircle(
                color = if (isPowered) activeColor else Color.DarkGray,
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Маркер вибраної точки на палітрі
            selectorOffset?.let { pos ->
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = pos,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}