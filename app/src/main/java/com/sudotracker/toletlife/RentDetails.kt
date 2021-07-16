package com.sudotracker.toletlife

data class RentDetails(
    var user_id: String,
    var pincode: String,
    var address: String,
    var monthly_rent: Int,
    var status_id: String,
    var titleImage: Int
)
