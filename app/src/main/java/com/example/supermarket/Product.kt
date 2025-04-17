package com.example.supermarket
import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id_product") val idProduct: Int,
    val name: String,
    val description: String?,
    val stock: Int,
    val cost: Double,
    @SerializedName("sale_price") val salePrice: Double,
    val revenue: Double,
    val photo: String?,
    val date: String
)