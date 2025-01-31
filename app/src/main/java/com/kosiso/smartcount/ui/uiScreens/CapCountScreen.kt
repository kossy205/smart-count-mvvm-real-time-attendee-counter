package com.kosiso.smartcount.ui.uiScreens



import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.kosiso.smartcount.ui.ui_utils.Custom4SidedShape
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.ui_utils.Common
import kotlin.math.abs
import com.kosiso.smartcount.R
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import androidx.compose.ui.tooling.preview.Preview as ComposePreview



private var currentFaceCount = mutableStateOf(0)


@ComposePreview(showBackground = true)
@Composable
private fun Preview(){
    CountContentSection()
}


@Composable
fun CapCountScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 65.dp)
    ){
        Column {
            TopIconSection()

            CropCameraPreviewSection()

            CountContentSection()
        }
    }
}


@Composable
private fun TopIconSection(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .height(74.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Common.IconButtonDesign(
                iconId = R.drawable.ic_arrange1,
                iconColor = White,
                backgroundColor = Pink,
                onIconClick = {

                }
            )

            Row {
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_profile0,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {

                    }
                )
                Spacer(modifier = Modifier.width(5.dp))
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_capture1,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {

                    }
                )
            }

        }
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier
){
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    // Initialize the camera provider
    val cameraProvider = rememberUpdatedState(ProcessCameraProvider.getInstance(context).get())
    val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
    )
    // Executor for handling analysis in a background thread
    val executor: Executor = remember { Executors.newSingleThreadExecutor() }
    // Image analysis setup
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setTargetResolution(Size(400, 400)) // Resolution of the frames
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Drop old frames
            .build()
    }
    val preview = remember {
        Preview.Builder()
            .build()
    }

    AndroidView(
        factory = {
            PreviewView(it).apply {
                preview.setSurfaceProvider(surfaceProvider)

                // Analyzer for processing camera frames
                imageAnalysis.setAnalyzer(executor) { imageProxy: ImageProxy ->
                    // Convert the camera frame to InputImage for ML Kit processing
                    val mediaImage = imageProxy.image
                    Log.i("face image proxy", "$mediaImage and $imageProxy")
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                        // Detect faces
                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces ->
                                val faceCount = faces.size
                                // Pass face count back to the composable's callback
                                currentFaceCount.value = faceCount
                                Log.i("face count", "$faceCount")
                            }
                            .addOnFailureListener { exception ->
                                // Handle any failure in face detection
                            }
                            .addOnCompleteListener {
                                imageProxy.close() // Always close the imageProxy
                            }
                    } else {
                        imageProxy.close()
                    }
                }

//              Bind the ImageAnalysis use case to the lifecycle
                try {
                    // Unbind all use cases before rebinding
                    cameraProvider.value.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.value.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.i("CameraPreview", "Use case binding failed $exc")
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun CropCameraPreviewSection() {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var vertices by remember {
        mutableStateOf(listOf(
            Offset(0.02f, 0.01f),
            Offset(0.98f, 0.01f),
            Offset(0.98f, 0.99f),
            Offset(0.02f, 0.99f)
        ))
    }
    var draggedVertexIndex by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }

    // initialize and bind the camera
    LaunchedEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
    }

    val customShape = Custom4SidedShape(vertices)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(customShape)
            .height(600.dp)
            .fillMaxWidth()
            .background(Color.Green)
    ){

        CameraPreview(
            modifier = Modifier
                .fillMaxSize()
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { screenSize = it }
                .pointerInput(screenSize) {
                    detectDragGestures(
                        // area around vertex that would respond to drag
                        onDragStart = { offset ->
                            draggedVertexIndex = vertices.indexOfFirst { vertex ->
                                val absX = abs(vertex.x - offset.x / screenSize.width)
                                val absY = abs(vertex.y - offset.y / screenSize.height)
                                absX < 0.17f && absY < 0.17f
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
                                            ) / screenSize.width) * 0.98f) + 0.02f,
                                            y = ((change.position.y.coerceIn(
                                                0f,
                                                screenSize.height.toFloat()
                                            ) / screenSize.height) * 0.98f) + 0.02f
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

            drawPath(path, Color.Black, style = Stroke(width = 8f))

            vertices.forEach { vertex ->
                drawCircle(
                    color = Pink,
                    radius = 30f,
                    center = Offset(
                        vertex.x * size.width,
                        vertex.y * size.height
                    )
                )
            }
        }
    }
}


@Composable
private fun CountContentSection(){

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(top = 12.dp)
    ){

        RealtimeCountBox(
            modifier = Modifier
                .weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        ClickToCountBox(
            modifier = Modifier
                .weight(1f)
        )

    }
}

@Composable
private fun RealtimeCountBox(modifier: Modifier = Modifier){
    val faceCount by remember { currentFaceCount }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Black)
            .padding(16.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Realtime Count",
//                    modifier = Modifier.align(Alignment.TopStart),
                    style = TextStyle(
                        color = White,
                        fontFamily = onest,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = White
                            )
                        ) {
                            append(faceCount.toString())
                        }
                        withStyle(
                            style = SpanStyle(
                                color = White.copy(alpha = 0.2f)
                            )
                        ) {
                            append(" ppl")
                        }
                    },
                    style = TextStyle(
                        color = White,
                        fontFamily = onest,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(18.dp),
                        painter = painterResource(R.drawable.baseline_access_time_24),
                        contentDescription = "",
                        tint = White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "1.2(-1.68%)",
                        style = TextStyle(
                            color = White,
                            fontFamily = onest,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }

                val sampleData = listOf(
                    DataPoint(0f, 20f),
                    DataPoint(0.5f, 45f),
                    DataPoint(1f, 30f),
                    DataPoint(1.5f, 60f),
                    DataPoint(2f, 50f),
                    DataPoint(2.5f, 30f),
                    DataPoint(3f, 60f),
                    DataPoint(3.5f, 50f)
                )

                Chart(
                    dataPoints = sampleData,
                    modifier = Modifier.padding(top = 10.dp)
                )

            }
        }
    }
}

data class DataPoint(
    val x: Float,
    val y: Float
)

@Composable
private fun Chart(dataPoints: List<DataPoint>,
          modifier: Modifier = Modifier,
          lineColor: Color = White.copy(alpha = 0.5f)){
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(1.dp)
    ) {
        if (dataPoints.isEmpty()) return@Canvas

        val xMin = dataPoints.minOf { it.x }
        val xMax = dataPoints.maxOf { it.x }
        val yMin = dataPoints.minOf { it.y }
        val yMax = dataPoints.maxOf { it.y }

        val xRange = xMax - xMin
        val yRange = yMax - yMin


        // Draw line chart
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = ((point.x - xMin) / xRange) * size.width
            val y = size.height - ((point.y - yMin) / yRange) * size.height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw points
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun ClickToCountBox(modifier: Modifier = Modifier){
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Pink)
            .padding(16.dp)
    ){
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            Column(
                modifier = Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "Click to \nCount",
//                    modifier = Modifier.align(Alignment.TopStart),
                        style = TextStyle(
                            color = White,
                            fontFamily = onest,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.ic_step),
                            contentDescription = "",
                            tint = White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = White
                            )
                        ) {
                            append("313")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = White.copy(alpha = 0.4f)
                            )
                        ) {
                            append(" ppl")
                        }
                    },
                    style = TextStyle(
                        color = White,
                        fontFamily = onest,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        modifier = Modifier
                            .size(18.dp),
                        painter = painterResource(R.drawable.baseline_access_time_24),
                        contentDescription = "",
                        tint = White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "1.2(-1.68%)",
                        style = TextStyle(
                            color = White,
                            fontFamily = onest,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

