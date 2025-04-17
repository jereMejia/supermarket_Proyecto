package com.example.supermarket

data class Verify(
    val accessToken: String,
    val refreshToken: String,
    val code: String
)