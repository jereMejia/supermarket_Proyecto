package com.example.supermarket

data class UpdatePassword(
    val accessToken: String,
    val refreshToken: String,
    val newPassword: String
)