package com.sudotracker.toletlife.Requests


import com.google.gson.annotations.SerializedName

data class UserProfileUpdateRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("phone_number")
    val phoneNumber: String
)