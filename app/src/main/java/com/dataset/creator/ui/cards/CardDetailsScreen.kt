package com.dataset.creator.ui.cards

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dataset.creator.ComposableLifecycle
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardDetailsScreen(cardName: String, navController: NavController, onImageClick: (Uri) -> Unit) {
    val context = LocalContext.current
    var imageUris by remember { mutableStateOf(getCardImageUris(context, cardName)) }
    var selectedUris by remember { mutableStateOf(emptySet<Uri>()) }
    var inSelectionMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun toggleSelection(uri: Uri) {
        selectedUris = if (selectedUris.contains(uri)) {
            selectedUris - uri
        } else {
            selectedUris + uri
        }
        if (selectedUris.isEmpty()) {
            inSelectionMode = false
        }
    }

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            imageUris = getCardImageUris(context, cardName)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Images") },
            text = { Text("Are you sure you want to delete the selected images?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedUris.forEach { deleteImage(context, it) }
                        imageUris = getCardImageUris(context, cardName) // Refresh list
                        selectedUris = emptySet()
                        inSelectionMode = false
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { if (!inSelectionMode) Text(cardName) else Text("${selectedUris.size} selected") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (inSelectionMode) {
                            inSelectionMode = false
                            selectedUris = emptySet()
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (inSelectionMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    } else {
                        Text("${imageUris.size} images", modifier = Modifier.padding(end = 16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.weight(1f).padding(16.dp)
            ) {
                items(imageUris) { uri ->
                    Box(modifier = Modifier.size(128.dp).padding(4.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    if (inSelectionMode) {
                                        toggleSelection(uri)
                                    } else {
                                        onImageClick(uri)
                                    }
                                },
                                onLongClick = {
                                    if (!inSelectionMode) {
                                        inSelectionMode = true
                                        toggleSelection(uri)
                                    }
                                }
                            )
                        )
                        if (selectedUris.contains(uri)) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { navController.navigate("camera/$cardName") },
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

    try {
        context.assets.list("cards/$cardName")?.forEach { fileName ->
            if (imageExtensions.any { fileName.endsWith(it, ignoreCase = true) }) {
                imageUris.add(Uri.parse("file:///android_asset/cards/$cardName/$fileName"))
            }
        }
    } catch (e: IOException) { /* Folder might not exist in assets */ }

    val internalStorageDir = File(context.getExternalFilesDir(null), cardName)
    if (internalStorageDir.exists()) {
        internalStorageDir.listFiles()?.forEach { file ->
            if (imageExtensions.any { file.extension.equals(it, ignoreCase = true) }) {
                imageUris.add(
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                )
            }
        }
    }
    return imageUris.sortedByDescending { it.toString() }
}

private fun deleteImage(context: Context, uri: Uri) {
    try {
        if (uri.scheme == "content") {
            context.contentResolver.delete(uri, null, null)
        } else if (uri.scheme == "file") {
            uri.path?.let { File(it).delete() }
        }
    } catch (e: Exception) { /* Handle exceptions */ }
}
