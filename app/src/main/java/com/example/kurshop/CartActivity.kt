package com.example.kurshop

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.adapter.CartAdapter
import com.google.android.material.button.MaterialButton

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: MaterialButton

    private var idUser: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // =========================
        // AMBIL ID USER
        // =========================
        idUser = intent.getIntExtra("id_user", 0)

        // =========================
        // INIT CART SESSION PER USER
        // =========================
        if (idUser != 0) {
            CartSession.init(this, idUser)
        }

        // =========================
        // INIT VIEW
        // =========================
        rvCart = findViewById(R.id.rvCart)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)

        // =========================
        // RECYCLER VIEW
        // =========================
        rvCart.layoutManager = LinearLayoutManager(this)

        // =========================
        // TAMPILKAN CART
        // =========================
        tampilkanCart()

        // =========================
        // CHECKOUT
        // =========================
        btnCheckout.setOnClickListener {
            if (CartSession.cartItems.isEmpty()) {

                Toast.makeText(
                    this,
                    "Keranjang masih kosong",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                val intent = Intent(
                    this,
                    CheckoutActivity::class.java
                )

                intent.putExtra("id_user", idUser)

                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (idUser != 0) {
            CartSession.init(this, idUser)
        }

        tampilkanCart()
    }

    private fun tampilkanCart() {
        val adapter = CartAdapter(
            listCart = CartSession.cartItems,

            onTambah = { item ->
                val berhasil = CartSession.tambahJumlah(item.produk.id)

                if (berhasil) {
                    tampilkanCart()
                } else {
                    Toast.makeText(
                        this,
                        "Jumlah sudah mencapai batas stok",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },

            onKurang = { item ->
                CartSession.kurangJumlah(item.produk.id)
                tampilkanCart()
            },

            onHapus = { item ->
                CartSession.hapusProduk(item.produk.id)
                tampilkanCart()
            }
        )

        rvCart.adapter = adapter

        val total = CartSession.totalHarga()

        tvSubtotal.text = "Rp ${formatRupiah(total)}"
        tvTotal.text = "Rp ${formatRupiah(total)}"
    }
}