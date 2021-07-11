package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.UploadImageRequest
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ImageInterface {
    @GET("image/sign_s3")
    fun presignedUrl(
        @Query("file_name") file_name: String,
        @Query("file_type") file_type: String,
        @Query("rent_id") rent_id: String,
        @Header("Authorization") token: String
    ): Call<Any>

    @Multipart
    @POST
    fun sendToAws(
        @Url url: String,
        @Part("key") file_name: RequestBody,
        @Part("x-amz-algorithm") algorithm: RequestBody,
        @Part("x-amz-credential") credential: RequestBody,
        @Part("x-amz-date") date: RequestBody,
        @Part("policy") policy: RequestBody,
        @Part("x-amz-signature") signature: RequestBody,
        @Part("file") file: RequestBody,
    ): Call<Any>

    @POST("image/uploadimage")
    fun saveImageDetails(
        @Body uploadImageRequest: UploadImageRequest,
        @Header("Authorization") token: String
    ): Call<Any>


}


object ImageService {
    val imageInstance: ImageInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        imageInstance = retrofit.create(ImageInterface::class.java)
    }
}