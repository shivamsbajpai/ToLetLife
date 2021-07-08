package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.OtpRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

const val BASE_URL = "https://toletlife.herokuapp.com/api/v1/"
interface IdentityInterface{
    @POST("identity/sendotp")
    fun sendOtp(@Body otpRequest: OtpRequest):Call<Any>
}

object IdentityService{
    val identityInstance: IdentityInterface
    init{
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        identityInstance = retrofit.create(IdentityInterface::class.java)
    }
}