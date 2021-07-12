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


    @GET("rentdetails/searchbyaddress")
    fun search_by_address(
        @Query("search_term") address_search_term: String,
        @Header("Authorization") token: String
    ): Call<Any>

    @GET("rentdetails/searchbyproduct")
    fun search_by_product(
        @Query("search_term") product_search_term: String,
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