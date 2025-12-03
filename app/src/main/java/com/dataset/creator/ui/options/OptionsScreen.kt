package com.dataset.creator.ui.options

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var lastExportUri by remember { mutableStateOf<Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isExporting = true
            coroutineScope.launch {
                lastExportUri = exportImages(context)
                isExporting = false
            }
        } else {
            Toast.makeText(context, "Permission denied. Cannot export images.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Options") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Images will be exported to the public 'Pictures/RFBound Cards' folder on your device.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (isExporting) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        isExporting = true
                        coroutineScope.launch {
                            lastExportUri = exportImages(context)
                            isExporting = false
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }) {
                    Text("Export All Images")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    lastExportUri?.let {
                        val intent = Intent(Intent.ACTION_VIEW, it)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(Intent.createChooser(intent, "Open with..."))
                    }
                },
                enabled = lastExportUri != null
            ) {
                Text("Open Export Folder")
            }
        }
    }
}

private suspend fun exportImages(context: Context): Uri? {
    var lastSavedUri: Uri? = null
    withContext(Dispatchers.IO) {
        val imageExtensions = setOf("jpg", "jpeg", "png", "webp")
        val sourceRoot = context.getExternalFilesDir(null)
        if (sourceRoot == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "External storage not available.", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        val contentResolver = context.contentResolver
        var imagesCopied = 0

        sourceRoot.listFiles()?.forEach { cardDir ->
            if (cardDir.isDirectory) {
                cardDir.listFiles()?.forEach { imageFile ->
                    if (imageExtensions.any { imageFile.extension.equals(it, ignoreCase = true) }) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFile.name)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/RFBound Cards/${cardDir.name}")
                            }
                        }

                        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            try {
                                contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    imageFile.inputStream().use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                                imagesCopied++
                                lastSavedUri = uri
                            } catch (e: Exception) {
                                // Handle exceptions
                            }
                        }
                    }
                }
            }
        }

        withContext(Dispatchers.Main) {
            val message = if (imagesCopied > 0) "$imagesCopied images exported successfully." else "No new images to export."
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    return lastSavedUri
}
