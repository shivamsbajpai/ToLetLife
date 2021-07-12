package com.sudotracker.toletlife.Services

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ProductCategoryInterface {

    @GET("productcategory/")
    fun getAllproductCategories(@Header("Authorization") token: String
    ): Call<Any>

}

object ProductCategoryService {
    val productCategoryInstance: ProductCategoryInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        productCategoryInstance = retrofit.create(ProductCategoryInterface::class.java)
    }
}