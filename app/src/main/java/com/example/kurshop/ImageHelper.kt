package com.example.kurshop

object ImageHelper {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    fun getImageUrl(foto: String?): String {
        return if (foto.isNullOrEmpty()) {
            ""
        } else if (foto.startsWith("http")) {
            foto
        } else {
            BASE_URL + foto
        }
    }
}