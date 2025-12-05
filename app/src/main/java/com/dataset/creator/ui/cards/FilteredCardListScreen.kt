package com.dataset.creator.ui.cards

import android.content.Context
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dataset.creator.viewmodel.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredCardListScreen(navController: NavController, viewModel: CardsViewModel, onCardClick: (String) -> Unit) {
    val context = LocalContext.current
    val cards = viewModel.cards.filter { getCardImageCount(context, it.getFolderName()) < 20 }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Filtered Cards") })
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
                            Text(text = "Set: ${card.set} - #${card.card_number}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(text = "${getCardImageCount(context, card.getFolderName())} images")
                    }
                }
            }
        }
    }
}
