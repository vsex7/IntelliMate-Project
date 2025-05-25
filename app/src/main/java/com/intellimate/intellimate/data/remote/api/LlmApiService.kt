package com.intellimate.intellimate.data.remote.api

import com.intellimate.intellimate.data.remote.dto.LlmRequest
import com.intellimate.intellimate.data.remote.dto.LlmResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LlmApiService {
    @POST("v1/completions") // Example endpoint
    suspend fun generateText(@Body request: LlmRequest, @Header("Authorization") apiKey: String): LlmResponse
}
