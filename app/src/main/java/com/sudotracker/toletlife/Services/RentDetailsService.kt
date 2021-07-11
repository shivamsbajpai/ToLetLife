package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.UserCreateRentRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface RentDetailsInterface {

    @GET("rentdetails/getallrentdetails/")
    fun getAllRentDetails(@Header("Authorization") token: String
    ): Call<Any>


    @GET("rentdetails/search")
    fun search(
        @Query("search_term") search_term: String,
        @Header("Authorization") token: String
    ): Call<Any>

}

object RentDetailsService {
    val rentDetailsInstance: RentDetailsInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        rentDetailsInstance = retrofit.create(RentDetailsInterface::class.java)
    }
}