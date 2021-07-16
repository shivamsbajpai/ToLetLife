package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class UserProfileDetailsResponse(
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("user_id")
    val userId: String
)