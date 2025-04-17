package com.example.supermarket

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/login") // Ruta del endpoint de login
    suspend fun login(@Body user: Login): Response<LoginResponse>

    @POST("/register") // Ruta del endpoint de registro
    suspend fun register(@Body user: Register): Response<RegisterResponse>

    @POST("/verify-code") // Ruta del endpoint de verificar codigo
    suspend fun verify(@Body user: Verify): Response<VerifyResponse>

    @POST("/sending-code") // Ruta del endpoint de verificar codigo
    suspend fun sendingCode(@Body user: SendingCode): Response<SendingCodeResponse>

    @POST("/update-password") // Ruta del endpoint de verificar codigo
    suspend fun updatePassword(@Body user: UpdatePassword): Response<UpdatePasswordResponse>

    @POST("/products") // Ruta del endpoint de verificar codigo
    suspend fun products(@Body user: Products): Response<ProductsResponse>
}