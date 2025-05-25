package com.intellimate.intellimate.data.remote

import com.intellimate.intellimate.data.remote.api.LlmApiService
import com.intellimate.intellimate.data.remote.api.VlmApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Conceptual Retrofit client setup.
 * This object provides functions to create instances of API services.
 * In a real application, this might also include OkHttpClient setup for headers, logging, etc.
 */
object ApiClient {

    // Placeholder base URLs - replace with actual API endpoints
    private const val LLM_BASE_URL = "https://api.examplellm.com/"
    private const val VLM_BASE_URL = "https://api.examplevlm.com/"

    /**
     * Creates an instance of the LlmApiService.
     * This is a conceptual setup; in a real app, you'd likely inject this
     * or have a more sophisticated DI setup.
     */
    fun createLlmService(): LlmApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(LLM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // .client(OkHttpClient.Builder().addInterceptor { chain -> /* Add API key header */ }.build()) // Example for headers
            .build()
        return retrofit.create(LlmApiService::class.java)
    }

    /**
     * Creates an instance of the VlmApiService.
     * Conceptual setup similar to LlmApiService.
     */
    fun createVlmService(): VlmApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(VLM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // .client(OkHttpClient.Builder().addInterceptor { chain -> /* Add API key header */ }.build()) // Example for headers
            .build()
        return retrofit.create(VlmApiService::class.java)
    }
}
