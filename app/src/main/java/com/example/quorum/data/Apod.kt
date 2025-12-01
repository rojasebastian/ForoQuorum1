package com.example.quorum.data
import com.google.gson.annotations.SerializedName

data class Apod(
    val title: String,
    val explanation: String,
    val url: String, // URL de la imagen
    @SerializedName("media_type") val mediaType: String
)
