package com.example.kurshop.model

data class TransaksiResponse(
    val id: Int,
    val id_user: Int,
    val total_harga: Int,
    val tanggal_transaksi: String,
    val status: String
)