package com.example.kurshop

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.adapter.CheckoutAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.DetailTransaksiRequest
import com.example.kurshop.model.DetailTransaksiResponse
import com.example.kurshop.model.Produk
import com.example.kurshop.model.ProdukRequest
import com.example.kurshop.model.TransaksiRequest
import com.example.kurshop.model.TransaksiResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var rvItemCheckout: RecyclerView
    private lateinit var tvTotalItem: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotalHarga: TextView
    private lateinit var chipStatus: Chip
    private lateinit var btnBayar: MaterialButton

    private var idUser: Int = 0
    private var isProcessing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

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
        rvItemCheckout = findViewById(R.id.rvItemCheckout)
        tvTotalItem = findViewById(R.id.tvTotalItem)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotalHarga = findViewById(R.id.tvTotalHarga)
        chipStatus = findViewById(R.id.chipStatus)
        btnBayar = findViewById(R.id.btnBayar)

        // =========================
        // RECYCLER VIEW
        // =========================
        rvItemCheckout.layoutManager = LinearLayoutManager(this)

        // =========================
        // TAMPILKAN CHECKOUT
        // =========================
        tampilkanCheckout()

        // =========================
        // BAYAR
        // =========================
        btnBayar.setOnClickListener {
            prosesCheckoutKeDatabase()
        }
    }

    override fun onResume() {
        super.onResume()

        if (idUser != 0) {
            CartSession.init(this, idUser)
        }

        tampilkanCheckout()
    }

    private fun tampilkanCheckout() {
        val totalItem = CartSession.totalItem()
        val totalHarga = CartSession.totalHarga()

        rvItemCheckout.adapter = CheckoutAdapter(CartSession.cartItems)

        tvTotalItem.text = "$totalItem item"
        tvSubtotal.text = "Rp ${formatRupiah(totalHarga)}"
        tvTotalHarga.text = "Rp ${formatRupiah(totalHarga)}"
        chipStatus.text = "Pending"
    }

    private fun prosesCheckoutKeDatabase() {
        if (isProcessing) return

        if (idUser == 0) {
            Toast.makeText(
                this,
                "ID user tidak ditemukan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (CartSession.cartItems.isEmpty()) {
            Toast.makeText(
                this,
                "Keranjang masih kosong",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val stokTidakCukup = CartSession.cartItems.find {
            it.jumlah > it.produk.stok
        }

        if (stokTidakCukup != null) {
            Toast.makeText(
                this,
                "Stok ${stokTidakCukup.produk.nama} tidak cukup",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        isProcessing = true
        btnBayar.isEnabled = false
        btnBayar.text = "MEMPROSES..."

        val tanggalSekarang = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val transaksiRequest = TransaksiRequest(
            id_user = idUser,
            total_harga = CartSession.totalHarga(),
            tanggal_transaksi = tanggalSekarang,
            status = "pending"
        )

        RetrofitClient.instance.createTransaksi(transaksiRequest)
            .enqueue(object : Callback<TransaksiResponse> {

                override fun onResponse(
                    call: Call<TransaksiResponse>,
                    response: Response<TransaksiResponse>
                ) {
                    if (response.isSuccessful) {

                        val transaksi = response.body()

                        if (transaksi != null) {
                            simpanDetailTransaksi(transaksi.id)
                        } else {
                            resetButton()

                            Toast.makeText(
                                this@CheckoutActivity,
                                "Data transaksi kosong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        resetButton()

                        Toast.makeText(
                            this@CheckoutActivity,
                            "Gagal membuat transaksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<TransaksiResponse>,
                    t: Throwable
                ) {
                    resetButton()

                    Toast.makeText(
                        this@CheckoutActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun simpanDetailTransaksi(idTransaksi: Int) {
        val cartItems = CartSession.cartItems.toList()

        if (cartItems.isEmpty()) {
            resetButton()
            return
        }

        var suksesCount = 0
        var gagal = false

        for (item in cartItems) {
            val detailRequest = DetailTransaksiRequest(
                id_transaksi = idTransaksi,
                id_produk = item.produk.id,
                jumlah = item.jumlah,
                harga = item.produk.harga
            )

            RetrofitClient.instance.createDetailTransaksi(detailRequest)
                .enqueue(object : Callback<DetailTransaksiResponse> {

                    override fun onResponse(
                        call: Call<DetailTransaksiResponse>,
                        response: Response<DetailTransaksiResponse>
                    ) {
                        if (gagal) return

                        if (response.isSuccessful) {
                            suksesCount++

                            if (suksesCount == cartItems.size) {
                                kurangiStokProduk(cartItems)
                            }

                        } else {
                            gagal = true
                            resetButton()

                            Toast.makeText(
                                this@CheckoutActivity,
                                "Gagal menyimpan detail transaksi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<DetailTransaksiResponse>,
                        t: Throwable
                    ) {
                        if (gagal) return

                        gagal = true
                        resetButton()

                        Toast.makeText(
                            this@CheckoutActivity,
                            "Error detail: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun kurangiStokProduk(cartItems: List<com.example.kurshop.model.CartItem>) {
        if (cartItems.isEmpty()) {
            resetButton()
            return
        }

        var suksesUpdateStok = 0
        var gagal = false

        for (item in cartItems) {
            val produk = item.produk
            val stokBaru = produk.stok - item.jumlah

            val request = ProdukRequest(
                nama = produk.nama,
                deskripsi = produk.deskripsi,
                harga = produk.harga,
                stok = stokBaru,
                foto = produk.foto ?: "",
                id_kategori = produk.id_kategori
            )

            RetrofitClient.instance.updateProduk(produk.id, request)
                .enqueue(object : Callback<Produk> {

                    override fun onResponse(
                        call: Call<Produk>,
                        response: Response<Produk>
                    ) {
                        if (gagal) return

                        if (response.isSuccessful) {
                            suksesUpdateStok++

                            if (suksesUpdateStok == cartItems.size) {
                                checkoutSelesai()
                            }

                        } else {
                            gagal = true
                            resetButton()

                            Toast.makeText(
                                this@CheckoutActivity,
                                "Transaksi masuk, tapi gagal update stok",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<Produk>,
                        t: Throwable
                    ) {
                        if (gagal) return

                        gagal = true
                        resetButton()

                        Toast.makeText(
                            this@CheckoutActivity,
                            "Error update stok: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun checkoutSelesai() {
        Toast.makeText(
            this,
            "Checkout berhasil",
            Toast.LENGTH_SHORT
        ).show()

        // Ini tetap ada.
        // Cart hanya dihapus setelah checkout berhasil.
        CartSession.clearCart()

        finish()
    }

    private fun resetButton() {
        isProcessing = false
        btnBayar.isEnabled = true
        btnBayar.text = "BAYAR SEKARANG"
    }
}