package com.example.kurshop.model

data class LoginResponse(
    val message: String,
    val user: User
)

data class User(
    val id: Int,
    val nama: String,
    val email: String,
    val role: String
)