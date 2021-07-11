package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class UserAllRentDetailsItem(
    @SerializedName("address")
    val address: String,
    @SerializedName("area")
    val area: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("image_urls")
    val imageUrls: List<ImageUrl>,
    @SerializedName("monthly_rent")
    val monthlyRent: Int,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("rent_id")
    val rentId: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status_id")
    val statusId: String,
    @SerializedName("user_id")
    val userId: String
)