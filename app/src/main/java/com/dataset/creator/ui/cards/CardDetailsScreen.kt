package com.dataset.creator.ui.cards

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dataset.creator.ComposableLifecycle
import com.dataset.creator.ui.common.StandardTopAppBar
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardDetailsScreen(cardName: String, navController: NavController, onImageClick: (Uri) -> Unit) {
    val context = LocalContext.current
    var imageUris by remember { mutableStateOf(getCardImageUris(context, cardName)) }
    var showDeleteDialog by remember { mutableStateOf<Uri?>(null) }

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            imageUris = getCardImageUris(context, cardName)
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { uri ->
                            deleteImage(context, uri)
                            imageUris = getCardImageUris(context, cardName) // Refresh list
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cardName) },
                navigationIcon = { StandardTopAppBar(title = "", navController = navController) },
                actions = {
                    Text(
                        text = "${imageUris.size} images",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(imageUris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(4.dp)
                            .combinedClickable(
                                onClick = { onImageClick(uri) },
                                onLongClick = { showDeleteDialog = uri }
                            )
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        navController.navigate("camera/$cardName")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Take Picture")
                }
            }
        }
    }
}

private fun getCardImageUris(context: Context, cardName: String): List<Uri> {
    val imageUris = mutableListOf<Uri>()
    val imageExtensions = setOf("jpg", "jpeg", "png", "webp")

    // Load from assets
    try {
        context.assets.list("cards/$cardName")?.forEach { fileName ->
            if (imageExtensions.any { fileName.endsWith(it, ignoreCase = true) }) {
                imageUris.add(Uri.parse("file:///android_asset/cards/$cardName/$fileName"))
            }
        }
    } catch (e: IOException) {
        // Folder might not exist in assets, which is fine
    }

    // Load from internal storage
    val internalStorageDir = File(context.getExternalFilesDir(null), cardName)
    if (internalStorageDir.exists()) {
        internalStorageDir.listFiles()?.forEach { file ->
            if (imageExtensions.any { file.extension.equals(it, ignoreCase = true) }) {
                imageUris.add(
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                )
            }
        }
    }
    return imageUris.sortedByDescending { it.toString() } // Show newest first
}

private fun deleteImage(context: Context, uri: Uri) {
    try {
        if (uri.scheme == "content") {
            context.contentResolver.delete(uri, null, null)
        } else if (uri.scheme == "file") {
            uri.path?.let { File(it).delete() }
        }
    } catch (e: Exception) {
        // Handle exceptions
    }
}
