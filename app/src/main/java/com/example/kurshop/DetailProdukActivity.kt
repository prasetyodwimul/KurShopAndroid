package com.example.kurshop

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kurshop.model.Produk
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class DetailProdukActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivFotoProduk: ImageView
    private lateinit var chipKategori: Chip
    private lateinit var tvNamaProduk: TextView
    private lateinit var tvHarga: TextView
    private lateinit var tvStok: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var btnTambahCart: MaterialButton

    private var idProduk: Int = 0
    private var nama: String = ""
    private var deskripsi: String = ""
    private var harga: Int = 0
    private var stok: Int = 0
    private var foto: String = ""
    private var idKategori: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_produk)

        toolbar = findViewById(R.id.toolbar)
        ivFotoProduk = findViewById(R.id.ivFotoProduk)
        chipKategori = findViewById(R.id.chipKategori)
        tvNamaProduk = findViewById(R.id.tvNamaProduk)
        tvHarga = findViewById(R.id.tvHarga)
        tvStok = findViewById(R.id.tvStok)
        tvDeskripsi = findViewById(R.id.tvDeskripsi)
        btnTambahCart = findViewById(R.id.btnTambahCart)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        ambilDataIntent()
        tampilkanData()
        setupButtonCart()
    }

    private fun ambilDataIntent() {
        idProduk = intent.getIntExtra("id_produk", 0)
        nama = intent.getStringExtra("nama") ?: ""
        deskripsi = intent.getStringExtra("deskripsi") ?: ""
        harga = intent.getIntExtra("harga", 0)
        stok = intent.getIntExtra("stok", 0)
        foto = intent.getStringExtra("foto") ?: ""
        idKategori = intent.getIntExtra("id_kategori", 0)
    }

    private fun tampilkanData() {
        tvNamaProduk.text = nama
        tvDeskripsi.text = deskripsi
        tvHarga.text = "Rp ${formatRupiah(harga)}"

        chipKategori.text = when (idKategori) {
            1 -> "Makanan"
            2 -> "Minuman"
            3 -> "Snack"
            4 -> "Dessert"
            else -> "Lainnya"
        }

        if (stok <= 0) {
            tvStok.text = "Stok: Habis"
            btnTambahCart.text = "HABIS"
            btnTambahCart.isEnabled = false
            btnTambahCart.alpha = 0.5f
        } else {
            tvStok.text = "Stok: $stok"
            btnTambahCart.text = "+ KERANJANG"
            btnTambahCart.isEnabled = true
            btnTambahCart.alpha = 1f
        }

        Glide.with(this)
            .load(ImageHelper.getImageUrl(foto))
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(ivFotoProduk)
    }

    private fun setupButtonCart() {
        btnTambahCart.setOnClickListener {
            val produk = Produk(
                id = idProduk,
                nama = nama,
                deskripsi = deskripsi,
                harga = harga,
                stok = stok,
                foto = foto,
                id_kategori = idKategori
            )

            val berhasil = CartSession.tambahProduk(produk)

            if (berhasil) {
                Toast.makeText(
                    this,
                    "$nama ditambahkan ke keranjang",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Stok $nama tidak mencukupi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}