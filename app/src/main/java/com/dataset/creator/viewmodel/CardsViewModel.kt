package com.dataset.creator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.carddatasetcreator.core.model.Card
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

class CardsViewModel : ViewModel() {
    var cards: List<Card> = emptyList()
        private set

    fun loadCards(context: Context) {
        if (cards.isEmpty()) {
            cards = try {
                val jsonString = context.assets.open("cards.json").bufferedReader().use { it.readText() }
                Json.decodeFromString<List<Card>>(jsonString)
            } catch (e: IOException) {
                emptyList()
            }
        }
    }
}
