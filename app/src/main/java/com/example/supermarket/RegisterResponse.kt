package com.example.supermarket

data class RegisterResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String
)

