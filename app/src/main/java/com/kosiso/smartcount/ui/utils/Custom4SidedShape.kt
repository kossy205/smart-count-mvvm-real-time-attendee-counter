package com.kosiso.smartcount.ui.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class Custom4SidedShape(val vertices: List<Offset>) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Define a custom quadrilateral shape
            moveTo(size.width * vertices[0].x, size.height * vertices[0].y)  // Top left point
            lineTo(size.width * vertices[1].x, size.height * vertices[1].y)  // Top right point
            lineTo(size.width * vertices[2].x, size.height * vertices[2].y)  // Bottom right point
            lineTo(size.width * vertices[3].x, size.height * vertices[3].y)  // Bottom left point
            close()
        }
        return Outline.Generic(path)
    }
}