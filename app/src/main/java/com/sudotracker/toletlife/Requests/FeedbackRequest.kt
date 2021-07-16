package com.sudotracker.toletlife.Requests


import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("feedback")
    val feedback: String,
    @SerializedName("name")
    val name: String
)