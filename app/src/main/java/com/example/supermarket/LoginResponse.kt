package com.example.supermarket

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String
)

