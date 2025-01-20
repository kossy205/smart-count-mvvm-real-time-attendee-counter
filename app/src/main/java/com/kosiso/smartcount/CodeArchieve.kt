package com.kosiso.smartcount

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kosiso.smartcount.ui.utils.Custom4SidedShape
import kotlin.math.abs

class CodeArchieve {


    fun Modifier.clipQuadrilateral(vertices: List<Offset>): Modifier =
        this.clip(object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val path = Path().apply {
                    moveTo(vertices[0].x, vertices[0].y)
                    lineTo(vertices[1].x, vertices[1].y)
                    lineTo(vertices[2].x, vertices[2].y)
                    lineTo(vertices[3].x, vertices[3].y)
                    close()
                }
                return Outline.Generic(path)
            }
        })


    @Composable
    fun MovableQuadrilateral() {
        var screenSize by remember { mutableStateOf(IntSize.Zero) }
        var vertices by remember {
            mutableStateOf(listOf(
                Offset(0.2f, 0.2f),
                Offset(0.8f, 0.2f),
                Offset(0.8f, 0.8f),
                Offset(0.2f, 0.8f)
            ))
        }
        var draggedVertexIndex by remember { mutableStateOf<Int?>(null) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .onSizeChanged { screenSize = it }
                .pointerInput(screenSize) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            draggedVertexIndex = vertices.indexOfFirst { vertex ->
                                val absX = abs(vertex.x - offset.x / screenSize.width)
                                val absY = abs(vertex.y - offset.y / screenSize.height)
                                absX < 0.07f && absY < 0.07f
                            }
                        },
                        onDrag = { change, _ ->
                            draggedVertexIndex?.let { index ->
                                vertices = vertices
                                    .toMutableList()
                                    .apply {

                                        this[index] = Offset(
                                            x = ((change.position.x.coerceIn(
                                                0f,
                                                screenSize.width.toFloat()
                                            ) / screenSize.width) * 0.9f) + 0.05f,
                                            y = ((change.position.y.coerceIn(
                                                0f,
                                                screenSize.height.toFloat()
                                            ) / screenSize.height) * 0.9f) + 0.05f
                                        )

                                    }
                            }
                        },
                        onDragEnd = {
                            draggedVertexIndex = null
                        }
                    )
                }
        ) {
            val path = Path().apply {
                moveTo(vertices[0].x * size.width, vertices[0].y * size.height)
                lineTo(vertices[1].x * size.width, vertices[1].y * size.height)
                lineTo(vertices[2].x * size.width, vertices[2].y * size.height)
                lineTo(vertices[3].x * size.width, vertices[3].y * size.height)
                close()
            }

            drawPath(path, Color.Red, style = Fill)
            drawPath(path, Color.Black, style = Stroke(width = 2f))

            vertices.forEach { vertex ->
                drawCircle(
                    color = Color.Black,
                    radius = 10f,
                    center = Offset(
                        vertex.x * size.width,
                        vertex.y * size.height
                    )
                )
            }
        }
    }


    @Composable
    fun CustomShapeExample() {
        var screenSize by remember { mutableStateOf(IntSize.Zero) }
        var vertices by remember {
            mutableStateOf(listOf(
                Offset(0.2f, 0.2f),
                Offset(0.8f, 0.2f),
                Offset(0.8f, 0.8f),
                Offset(0.2f, 0.8f)
            ))
        }
        var draggedVertexIndex by remember { mutableStateOf<Int?>(null) }
        // Create the custom shape
        val customShape = Custom4SidedShape(vertices)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Box with custom shape and clipped content
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(700.dp)
                    .clip(customShape) // Clip the entire box to the custom shape
//                .background(Color.Blue.copy(alpha = 0.5f))
                    .border(2.dp, Color.Black)
                    .pointerInput(screenSize) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                draggedVertexIndex = vertices.indexOfFirst { vertex ->
                                    val absX = abs(vertex.x - offset.x / screenSize.width)
                                    val absY = abs(vertex.y - offset.y / screenSize.height)
                                    absX < 0.07f && absY < 0.07f
                                }
                            },
                            onDrag = { change, _ ->
                                draggedVertexIndex?.let { index ->
                                    vertices = vertices
                                        .toMutableList()
                                        .apply {

                                            this[index] = Offset(
                                                x = ((change.position.x.coerceIn(
                                                    0f,
                                                    screenSize.width.toFloat()
                                                ) / screenSize.width) * 0.9f) + 0.05f,
                                                y = ((change.position.y.coerceIn(
                                                    0f,
                                                    screenSize.height.toFloat()
                                                ) / screenSize.height) * 0.9f) + 0.05f
                                            )

                                        }
                                }
                            },
                            onDragEnd = {
                                draggedVertexIndex = null
                            }
                        )
                    }
            ) {
                // Content inside the custom-shaped box
//            Box(
//                modifier = Modifier
//                    .size(200.dp)
//                    .background(Color.Red.copy(alpha = 0.7f))
//            )
                CameraBox()
            }
        }
    }


    @Composable
    fun CameraBox(){
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val controller = remember {
            LifecycleCameraController(context).apply {
                // this is enabling what we want to use the camera preview to do.
//            it can be image capture, video recording or image analysis (which is what we want to do)
                setEnabledUseCases(
                    CameraController.IMAGE_ANALYSIS
                )
            }
        }

        // initialize and bind the camera
        LaunchedEffect(lifecycleOwner) {
            controller.bindToLifecycle(lifecycleOwner)
        }

        Box(modifier = Modifier.fillMaxSize()){

//            CameraPreview(
//                controller = controller,
//                modifier = Modifier
//                    .fillMaxSize()
//            )
        }
    }
}