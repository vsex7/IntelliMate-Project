package com.intellimate.intellimate.data.remote.api

import com.intellimate.intellimate.data.remote.dto.VlmRequest
import com.intellimate.intellimate.data.remote.dto.VlmResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VlmApiService {
    @POST("v1/vision/analyze") // Example endpoint
    suspend fun analyzeImage(@Body request: VlmRequest, @Header("Authorization") apiKey: String): VlmResponse
}
