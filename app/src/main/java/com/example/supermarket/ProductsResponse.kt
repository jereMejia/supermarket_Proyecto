package com.example.supermarket

data class ProductsResponse(
    val message: String,
    val products: List<Product>,
    val accessToken: String,
    val refreshToken: String
)

