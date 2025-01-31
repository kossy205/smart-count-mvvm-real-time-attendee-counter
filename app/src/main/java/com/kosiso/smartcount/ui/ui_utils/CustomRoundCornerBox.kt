package com.kosiso.smartcount.ui.ui_utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class CustomRoundCornerBox(
    private val cornerRadius: Float = 45f,
    private val arrowHeight: Float = 20f,
    private val arrowWidth: Float = 40f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(path = createPath(size))
    }

    private fun createPath(size: Size): Path {
        val path = Path()
        val width = size.width
        val height = size.height

        // Top arrow
        path.moveTo(width / 2 - arrowWidth / 2, cornerRadius)
        path.lineTo(width / 2, 0f)
        path.lineTo(width / 2 + arrowWidth / 2, cornerRadius)

        // Top right corner
        path.arcTo(
            rect = Rect(
                left = width - cornerRadius * 2,
                top = cornerRadius,
                right = width,
                bottom = cornerRadius * 3
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Right side
        path.lineTo(width, height - cornerRadius - arrowHeight)

        // Bottom right arrow
        path.lineTo(width / 2 + arrowWidth / 2, height - arrowHeight)
        path.lineTo(width / 2, height)
        path.lineTo(width / 2 - arrowWidth / 2, height - arrowHeight)

        // Bottom left corner
        path.arcTo(
            rect = Rect(
                left = 0f,
                top = height - cornerRadius * 3,
                right = cornerRadius * 2,
                bottom = height - cornerRadius
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // Left side
        path.lineTo(0f, cornerRadius + arrowHeight)

        // Top left corner
        path.arcTo(
            rect = Rect(
                left = 0f,
                top = cornerRadius,
                right = cornerRadius * 2,
                bottom = cornerRadius * 3
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        path.close()
        return path
    }
}