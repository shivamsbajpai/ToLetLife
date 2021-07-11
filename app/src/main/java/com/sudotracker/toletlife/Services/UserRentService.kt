package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.UploadImageRequest
import com.sudotracker.toletlife.Requests.UserCreateRentRequest
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface UserRentInterface {

    @GET("user/rentdetails/")
    fun userGetAllRentDetails(@Header("Authorization") token: String
    ): Call<Any>


    @POST("user/rentdetails/")
    fun userCreateRent(
        @Body userCreateRentRequest: UserCreateRentRequest,
        @Header("Authorization") token: String
    ): Call<Any>

}

object UserRentService {
    val userRentInstance: UserRentInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        userRentInstance = retrofit.create(UserRentInterface::class.java)
    }
}