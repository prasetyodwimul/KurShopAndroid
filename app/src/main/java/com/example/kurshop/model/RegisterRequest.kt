package com.example.kurshop.model

data class RegisterRequest(
    val nama: String,
    val email: String,
    val password: String,
    val role: String
)