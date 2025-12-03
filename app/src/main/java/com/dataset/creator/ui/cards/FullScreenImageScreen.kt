package com.dataset.creator.ui.cards

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dataset.creator.ui.common.StandardTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageScreen(imageUri: Uri, navController: NavController) {
    Scaffold(
        topBar = { StandardTopAppBar(title = "Image Viewer", navController = navController) }
    ) { paddingValues ->
        Image(
            painter = rememberAsyncImagePainter(model = imageUri),
            contentDescription = "Full-screen image",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentScale = ContentScale.Fit
        )
    }
}
