package com.example.carddatasetcreator.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val set: String,
    val card_number: String,
    val name: String
) {
    fun getFolderName(): String {
        val formattedName = name.replace(" ", "_")
        return "$set-$card_number-$formattedName"
    }
}