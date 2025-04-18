package com.example.fetchassessment

import retrofit2.Call
import retrofit2.http.GET

interface MyApi {
    @GET("hiring.json")
    fun getItems(): Call<List<Item>>
}