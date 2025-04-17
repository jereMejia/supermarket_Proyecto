package com.example.supermarket

data class UpdatePasswordResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String
)