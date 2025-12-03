package com.dataset.creator.ui.cards

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import com.example.carddatasetcreator.core.model.Card
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(navController: NavController, onCardClick: (String) -> Unit) {
    val context = LocalContext.current
    val cards = remember { getCards(context) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cards") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                val firstImageUri = remember(card) { getFirstCardImageUri(context, card.getFolderName()) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCardClick(card.getFolderName()) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (firstImageUri != null) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(firstImageUri)
                                    .transformations(TopCropTransformation())
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Card thumbnail",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(MaterialTheme.shapes.small),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Spacer(modifier = Modifier.size(56.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = card.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Set: ${card.set}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(text = "${getCardImageCount(context, card.getFolderName())} images")
                    }
                }
            }
        }
    }
}

private fun getCards(context: Context): List<Card> {
    return try {
        val jsonString = context.assets.open("cards.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    } catch (e: IOException) {
        emptyList()
    }
}

private fun getCardImageCount(context: Context, cardFolderName: String): Int {
    val imageExtensions = setOf("jpg", "jpeg", "png", "webp")
    try {
        val assetImages = context.assets.list("cards/$cardFolderName")
            ?.count { f -> imageExtensions.any { f.endsWith(it, ignoreCase = true) } } ?: 0

        val internalStorageDir = File(context.getExternalFilesDir(null), cardFolderName)
        val internalImages = if (internalStorageDir.exists()) {
            internalStorageDir.listFiles()
                ?.count { f -> imageExtensions.any { f.extension.equals(it, ignoreCase = true) } } ?: 0
        } else 0

        return assetImages + internalImages
    } catch (e: IOException) {
        return 0
    }
}

private fun getFirstCardImageUri(context: Context, cardFolderName: String): Uri? {
    val imageExtensions = setOf("jpg", "jpeg", "png", "webp")
    try {
        // Check assets first
        context.assets.list("cards/$cardFolderName")?.firstOrNull { f ->
            imageExtensions.any { f.endsWith(it, ignoreCase = true) }
        }?.let {
            return Uri.parse("file:///android_asset/cards/$cardFolderName/$it")
        }

        // Then check internal storage
        val internalStorageDir = File(context.getExternalFilesDir(null), cardFolderName)
        if (internalStorageDir.exists()) {
            internalStorageDir.listFiles()?.firstOrNull { f ->
                imageExtensions.any { f.extension.equals(it, ignoreCase = true) }
            }?.let {
                return Uri.fromFile(it)
            }
        }
    } catch (e: IOException) {
        // Ignore
    }
    return null
}

class TopCropTransformation : Transformation {
    override val cacheKey: String = "topCrop"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val smallerEdge = minOf(input.width, input.height)
        return Bitmap.createBitmap(input, 0, 0, smallerEdge, smallerEdge)
    }
}