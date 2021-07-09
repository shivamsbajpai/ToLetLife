package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.LoginRequest
import com.sudotracker.toletlife.Requests.OtpRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

const val BASE_URL = "https://toletlife.herokuapp.com/"
const val VERSION_PREFIX = "api/v1/"
const val SERVICE_PREFIX = "identity"
interface IdentityInterface{
    @POST("${VERSION_PREFIX}${SERVICE_PREFIX}/sendotp")
    fun sendOtp(@Body otpRequest: OtpRequest):Call<Any>

    @POST("${VERSION_PREFIX}${SERVICE_PREFIX}/loginuser")
    fun sendLogin(@Body loginRequest: LoginRequest):Call<Any>
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