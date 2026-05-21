package com.example.kurshop.model

data class CartItem(
    val produk: Produk,
    var jumlah: Int = 1
) {
    fun subtotal(): Int {
        return produk.harga * jumlah
    }
}