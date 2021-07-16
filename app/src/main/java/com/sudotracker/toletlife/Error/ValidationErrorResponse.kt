package com.sudotracker.toletlife.Error

data class ValidationErrorResponse (
    val detail: ArrayList<ValidationType>
)