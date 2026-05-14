package com.example.kurshop.model

data class ProdukRequest(
    val nama: String,
    val deskripsi: String,
    val harga: Int,
    val stok: Int,
    val foto: String,
    val id_kategori: Int
)