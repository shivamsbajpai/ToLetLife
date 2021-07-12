package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class UserCreateRentResponse(
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_description")
    val productDescription: String,
    @SerializedName("rent_id")
    val rentId: String,
    @SerializedName("monthly_rent")
    val monthlyRent: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status_id")
    val statusId: String,
    @SerializedName("product_category_id")
    val productCategoryId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("security_deposit")
    val securityDeposit: String,
    @SerializedName("pincode")
    val pincode: String,
    @SerializedName("area")
    val area: String,
    @SerializedName("last_updated")
    val lastUpdated: String,
)