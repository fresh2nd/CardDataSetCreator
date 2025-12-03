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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
                lastExportUri = exportImagesAsZip(context)
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
                text = "Export all card images as a single 'CardDataSet.zip' file to your device's 'Documents' folder.",
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
                            lastExportUri = exportImagesAsZip(context)
                            isExporting = false
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }) {
                    Text("Export as .zip file")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    lastExportUri?.let {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(it, "application/zip")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Open with..."))
                    }
                },
                enabled = lastExportUri != null
            ) {
                Text("Open Zip File")
            }
        }
    }
}

private suspend fun exportImagesAsZip(context: Context): Uri? {
    var zipUri: Uri? = null
    withContext(Dispatchers.IO) {
        val sourceRoot = context.getExternalFilesDir(null)
        if (sourceRoot == null || !sourceRoot.exists()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "No images to export.", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "CardDataSet.zip")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/RFBound Cards")
            }
        }

        zipUri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (zipUri == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to create zip file.", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        try {
            contentResolver.openOutputStream(zipUri!!)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOutputStream ->
                    sourceRoot.listFiles()?.forEach { cardDir ->
                        if (cardDir.isDirectory) {
                            cardDir.listFiles()?.forEach { imageFile ->
                                val entry = ZipEntry("${cardDir.name}/${imageFile.name}")
                                zipOutputStream.putNextEntry(entry)
                                imageFile.inputStream().use { inputStream ->
                                    inputStream.copyTo(zipOutputStream)
                                }
                                zipOutputStream.closeEntry()
                            }
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Successfully exported to Documents/RFBound Cards", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    return zipUri
}
