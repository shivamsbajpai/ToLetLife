package com.sudotracker.toletlife.Services

import com.sudotracker.toletlife.Requests.FeedbackRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackInterface{
    @POST("feedback/")
    fun sendFeedback(@Body feedbackRequest: FeedbackRequest): Call<Any>
}

object FeedbackService{
    val feedbackInstance: FeedbackInterface
    init{
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        feedbackInstance = retrofit.create(FeedbackInterface::class.java)
    }
}