package com.dataset.creator.ui.camera

import android.content.Context
import android.media.MediaActionSound
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.dataset.creator.viewmodel.CardsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen(cardName: String, navController: NavController, viewModel: CardsViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isAutoCaptureOn by remember { mutableStateOf(false) }
    var showPictureAddedCue by remember { mutableStateOf(false) }

    val sound = remember { MediaActionSound() }

    // Auto-capture logic
    LaunchedEffect(isAutoCaptureOn) {
        if (isAutoCaptureOn) {
            while (isActive) {
                takePhoto(context, imageCapture, cardName) {
                    coroutineScope.launch {
                        showPictureAddedCue = true
                        sound.play(MediaActionSound.SHUTTER_CLICK)
                        delay(500) // Show cue for 0.5s
                        showPictureAddedCue = false
                    }
                }
                delay(600) // Wait 0.6s before next photo
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    } catch (exc: Exception) {
                        // Handle exceptions
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close camera", tint = Color.White)
                }
                Text(
                    text = cardName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val cards = viewModel.cards
                    val currentIndex = cards.indexOfFirst { it.getFolderName() == cardName }
                    if (currentIndex != -1 && currentIndex < cards.size - 1) {
                        val nextCard = cards[currentIndex + 1]
                        navController.navigate("cardDetails/${nextCard.getFolderName()}") {
                            popUpTo("cardDetails/$cardName") { inclusive = true }
                        }
                        navController.navigate("camera/${nextCard.getFolderName()}")
                    }
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Card", tint = Color.White)
                }
            }

            AnimatedVisibility(
                visible = showPictureAddedCue,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Picture added", color = Color.White, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(16.dp))
            }
        }

        // Bottom UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(60.dp)) // Placeholder

            IconButton(
                onClick = {
                    if (!isAutoCaptureOn) {
                        takePhoto(context, imageCapture, cardName) {
                            coroutineScope.launch {
                                showPictureAddedCue = true
                                sound.play(MediaActionSound.SHUTTER_CLICK)
                                delay(500)
                                showPictureAddedCue = false
                            }
                        }
                    }
                },
                enabled = !isAutoCaptureOn
            ) {
                Box(modifier = Modifier.size(80.dp).padding(1.dp).border(2.dp, if (isAutoCaptureOn) Color.Gray else Color.White, CircleShape))
            }

            IconButton(
                onClick = { isAutoCaptureOn = !isAutoCaptureOn },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isAutoCaptureOn) Color.Yellow else Color.White
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "Auto Capture")
            }
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture?, cardName: String, onPhotoTaken: () -> Unit) {
    if (imageCapture == null) return

    val photoFile = createImageFile(context, cardName)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                onPhotoTaken()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onPhotoTaken()
            }
        }
    )
}

private fun createImageFile(context: Context, cardName: String): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
    val storageDir = File(context.getExternalFilesDir(null), cardName)
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File(storageDir, "JPEG_${timeStamp}.jpg")
}
