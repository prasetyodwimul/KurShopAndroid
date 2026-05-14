package com.example.kurshop.model

data class DetailTransaksiResponse(
    val id: Int,
    val id_transaksi: Int,
    val id_produk: Int,
    val jumlah: Int,
    val subtotal: Int
)