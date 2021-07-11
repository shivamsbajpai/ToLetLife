package com.sudotracker.toletlife.Requests

data class UploadImageRequest(
    var rent_id: String,
    var file_address_aws: String
)
