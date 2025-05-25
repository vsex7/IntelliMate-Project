package com.intellimate.intellimate.data.remote.dto

data class VlmRequest(
    val image_url: String, // Simplified: In reality, this might be a base64 string or a more complex object
    val prompt: String
)
