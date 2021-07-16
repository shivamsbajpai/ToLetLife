package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class ProductCategoryResponseItem(
    @SerializedName("product_category")
    val productCategory: String,
    @SerializedName("product_category_id")
    val productCategoryId: String
)