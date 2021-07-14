package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.UserCreateRentRequest
import com.sudotracker.toletlife.Requests.UserProfileUpdateRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface UserProfileInterface {

    @GET("userprofiledetails/get")
    fun getUserProfileDetails(@Header("Authorization") token: String
    ): Call<Any>

    @PUT("userprofiledetails/update")
    fun updateUserProfileDetails(
        @Body userProfileUpdateRequest: UserProfileUpdateRequest,
        @Header("Authorization") token: String
    ): Call<Any>

    @DELETE("userprofiledetails/delete")
    fun deleteUserProfileDetails(
        @Query("delete_string")deleteString: String,@Header("Authorization") token: String
    ): Call<Any>

}

object UserProfileService {
    val userProfileInstance: UserProfileInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        userProfileInstance = retrofit.create(UserProfileInterface::class.java)
    }
}