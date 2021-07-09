package com.sudotracker.toletlife.Requests

data class RegisterRequest (
    var name: String,
    var phone_number: String,
    var email: String,
    var otp_code: String,
    var password: String
        )