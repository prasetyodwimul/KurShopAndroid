package com.example.kurshop.model

data class DetailTransaksi(
    val id: Int,
    val id_transaksi: Int,
    val id_produk: Int,
    val jumlah: Int,
    val subtotal: Int
)