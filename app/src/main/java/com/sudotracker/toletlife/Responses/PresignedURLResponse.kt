package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class PresignedURLResponse(
    @SerializedName("fields")
    val fields: Fields,
    @SerializedName("url")
    val url: String
)