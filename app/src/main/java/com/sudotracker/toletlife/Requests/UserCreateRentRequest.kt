package com.sudotracker.toletlife.Requests


import com.google.gson.annotations.SerializedName

data class UserCreateRentRequest(
    @SerializedName("address")
    val address: String,
    @SerializedName("area")
    val area: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("monthly_rent")
    val monthlyRent: Int,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status_id")
    val statusId: String
)