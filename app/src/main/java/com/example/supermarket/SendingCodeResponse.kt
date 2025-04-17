package com.example.supermarket

data class SendingCodeResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String
)