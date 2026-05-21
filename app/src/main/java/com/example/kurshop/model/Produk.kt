package com.example.kurshop.model

data class Produk(
    val id: Int,
    val nama: String,
    val deskripsi: String,
    val harga: Int,
    val stok: Int,
    val foto: String?,
    val id_kategori: Int
)