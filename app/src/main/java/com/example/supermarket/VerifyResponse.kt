package com.example.supermarket

data class VerifyResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String
)