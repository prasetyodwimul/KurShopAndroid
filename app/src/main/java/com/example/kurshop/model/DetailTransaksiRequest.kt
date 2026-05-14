package com.example.kurshop.model

data class DetailTransaksiRequest(
    val id_transaksi: Int,
    val id_produk: Int,
    val jumlah: Int,
    val harga: Int
)