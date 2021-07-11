package com.sudotracker.toletlife.Responses


import com.google.gson.annotations.SerializedName

data class Fields(
    @SerializedName("key")
    val file_address_aws: String,
    @SerializedName("policy")
    val policy: String,
    @SerializedName("x-amz-algorithm")
    val xAmzAlgorithm: String,
    @SerializedName("x-amz-credential")
    val xAmzCredential: String,
    @SerializedName("x-amz-date")
    val xAmzDate: String,
    @SerializedName("x-amz-signature")
    val xAmzSignature: String
)