package com.sudotracker.toletlife.Requests


import com.google.gson.annotations.SerializedName

data class UserCreateRentRequest(
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_category_id")
    val productCategoryId: String,
    @SerializedName("product_description")
    val productDescription: String,
    @SerializedName("security_deposit")
    val securityDeposit: String,
    @SerializedName("monthly_rent")
    val monthlyRent: String,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("area")
    val area: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status_id")
    val statusId: String
)