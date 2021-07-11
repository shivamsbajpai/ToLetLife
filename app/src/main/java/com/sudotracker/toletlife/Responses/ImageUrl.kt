package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class ImageUrl(
    @SerializedName("image_id")
    val imageId: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("rent_id")
    val rentId: String,
    @SerializedName("user_id")
    val userId: String
)