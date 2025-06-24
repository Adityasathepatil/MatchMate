package com.tech.matchmate.data.remote.api

import com.tech.matchmate.data.remote.model.RandomUserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/")
    suspend fun getRandomUsers(@Query("results") results: Int = 10): RandomUserResponse
}