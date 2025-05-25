package com.intellimate.intellimate.data.remote.dto

data class LlmRequest(
    val prompt: String,
    val model: String = "text-davinci-003" // Example default model
)
