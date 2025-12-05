package com.dataset.creator.ui.cards

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil.size.Size
import coil.transform.Transformation
import com.example.carddatasetcreator.core.model.Card
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

fun getCards(context: Context): List<Card> {
    return try {
        val jsonString = context.assets.open("cards.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    } catch (e: IOException) {
        emptyList()
    }
}

fun getCardImageCount(context: Context, cardFolderName: String): Int {
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

fun getFirstCardImageUri(context: Context, cardFolderName: String): Uri? {
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
