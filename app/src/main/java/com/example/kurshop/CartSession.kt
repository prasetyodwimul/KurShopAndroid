package com.example.kurshop

import android.content.Context
import com.example.kurshop.model.CartItem
import com.example.kurshop.model.Produk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartSession {

    val cartItems = mutableListOf<CartItem>()

    private var contextApp: Context? = null
    private var idUser: Int = 0

    private const val PREF_NAME = "kurshop_cart"

    fun init(context: Context, userId: Int) {
        contextApp = context.applicationContext
        idUser = userId

        loadCart()
    }

    private fun getCartKey(): String {
        return "cart_user_$idUser"
    }

    private fun saveCart() {
        if (contextApp == null || idUser == 0) return

        val sharedPreferences =
            contextApp!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val json = Gson().toJson(cartItems)

        sharedPreferences.edit()
            .putString(getCartKey(), json)
            .apply()
    }

    private fun loadCart() {
        if (contextApp == null || idUser == 0) return

        val sharedPreferences =
            contextApp!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val json = sharedPreferences.getString(getCartKey(), null)

        cartItems.clear()

        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<CartItem>>() {}.type
            val savedCart: MutableList<CartItem> = Gson().fromJson(json, type)

            cartItems.addAll(savedCart)
        }
    }

    fun tambahProduk(produk: Produk): Boolean {
        val itemAda = cartItems.find {
            it.produk.id == produk.id
        }

        val berhasil = if (itemAda != null) {

            if (itemAda.jumlah < produk.stok) {
                itemAda.jumlah += 1
                true
            } else {
                false
            }

        } else {

            if (produk.stok > 0) {
                cartItems.add(
                    CartItem(
                        produk = produk,
                        jumlah = 1
                    )
                )
                true
            } else {
                false
            }
        }

        if (berhasil) {
            saveCart()
        }

        return berhasil
    }

    fun hapusProduk(produkId: Int) {
        cartItems.removeAll {
            it.produk.id == produkId
        }

        saveCart()
    }

    fun tambahJumlah(produkId: Int): Boolean {
        val item = cartItems.find {
            it.produk.id == produkId
        }

        val berhasil = if (item != null) {
            if (item.jumlah < item.produk.stok) {
                item.jumlah += 1
                true
            } else {
                false
            }
        } else {
            false
        }

        if (berhasil) {
            saveCart()
        }

        return berhasil
    }

    fun kurangJumlah(produkId: Int) {
        val item = cartItems.find {
            it.produk.id == produkId
        }

        if (item != null) {
            if (item.jumlah > 1) {
                item.jumlah -= 1
            } else {
                hapusProduk(produkId)
                return
            }
        }

        saveCart()
    }

    fun totalHarga(): Int {
        return cartItems.sumOf {
            it.subtotal()
        }
    }

    fun totalItem(): Int {
        return cartItems.sumOf {
            it.jumlah
        }
    }

    fun clearCart() {
        cartItems.clear()

        if (contextApp != null && idUser != 0) {
            val sharedPreferences =
                contextApp!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            sharedPreferences.edit()
                .remove(getCartKey())
                .apply()
        }
    }
}