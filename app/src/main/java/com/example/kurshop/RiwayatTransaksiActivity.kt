package com.example.kurshop

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.adapter.RiwayatTransaksiAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.DetailTransaksi
import com.example.kurshop.model.Produk
import com.example.kurshop.model.ProdukRequest
import com.example.kurshop.model.Transaksi
import com.example.kurshop.model.TransaksiRequest
import com.example.kurshop.model.TransaksiResponse
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatTransaksiActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvEmptyRiwayat: TextView
    private lateinit var rvRiwayatTransaksi: RecyclerView

    private var idUser: Int = 0
    private var isProcessingCancel: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_transaksi)

        idUser = intent.getIntExtra("id_user", 0)

        toolbar = findViewById(R.id.toolbar)
        tvEmptyRiwayat = findViewById(R.id.tvEmptyRiwayat)
        rvRiwayatTransaksi = findViewById(R.id.rvRiwayatTransaksi)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        rvRiwayatTransaksi.layoutManager = LinearLayoutManager(this)

        loadRiwayat()
    }

    override fun onResume() {
        super.onResume()
        loadRiwayat()
    }

    private fun loadRiwayat() {
        RetrofitClient.instance.getTransaksi()
            .enqueue(object : Callback<List<Transaksi>> {

                override fun onResponse(
                    call: Call<List<Transaksi>>,
                    response: Response<List<Transaksi>>
                ) {
                    if (response.isSuccessful) {
                        val semuaTransaksi = response.body() ?: emptyList()

                        val transaksiUser = semuaTransaksi
                            .filter {
                                it.id_user == idUser
                            }
                            .sortedByDescending {
                                it.id
                            }

                        if (transaksiUser.isEmpty()) {
                            tvEmptyRiwayat.visibility = View.VISIBLE
                            rvRiwayatTransaksi.visibility = View.GONE
                        } else {
                            tvEmptyRiwayat.visibility = View.GONE
                            rvRiwayatTransaksi.visibility = View.VISIBLE
                        }

                        rvRiwayatTransaksi.adapter =
                            RiwayatTransaksiAdapter(
                                listTransaksi = transaksiUser,
                                onClick = { transaksi ->
                                    bukaDetailTransaksi(transaksi)
                                },
                                onBatalPesanan = { transaksi ->
                                    konfirmasiBatalPesanan(transaksi)
                                }
                            )

                    } else {
                        Toast.makeText(
                            this@RiwayatTransaksiActivity,
                            "Gagal mengambil riwayat transaksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Transaksi>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@RiwayatTransaksiActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun bukaDetailTransaksi(transaksi: Transaksi) {
        val intent = Intent(
            this,
            DetailTransaksiActivity::class.java
        )

        intent.putExtra("id_transaksi", transaksi.id)
        intent.putExtra("status", transaksi.status)
        intent.putExtra("total_harga", transaksi.total_harga)

        startActivity(intent)
    }

    private fun konfirmasiBatalPesanan(transaksi: Transaksi) {
        if (transaksi.status.lowercase() != "pending") {
            Toast.makeText(
                this,
                "Pesanan tidak bisa dibatalkan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Batalkan Pesanan")
            .setMessage("Yakin mau membatalkan pesanan #TRX-${transaksi.id}?")
            .setPositiveButton("Batalkan") { _, _ ->
                batalkanPesanan(transaksi)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun batalkanPesanan(transaksi: Transaksi) {
        if (isProcessingCancel) return

        isProcessingCancel = true

        RetrofitClient.instance.getDetailTransaksi()
            .enqueue(object : Callback<List<DetailTransaksi>> {

                override fun onResponse(
                    call: Call<List<DetailTransaksi>>,
                    response: Response<List<DetailTransaksi>>
                ) {
                    if (response.isSuccessful) {
                        val semuaDetail = response.body() ?: emptyList()

                        val detailPesanan = semuaDetail.filter {
                            it.id_transaksi == transaksi.id
                        }

                        if (detailPesanan.isEmpty()) {
                            updateStatusJadiBatal(transaksi)
                        } else {
                            kembalikanStokProduk(transaksi, detailPesanan)
                        }

                    } else {
                        isProcessingCancel = false

                        Toast.makeText(
                            this@RiwayatTransaksiActivity,
                            "Gagal mengambil detail transaksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<DetailTransaksi>>,
                    t: Throwable
                ) {
                    isProcessingCancel = false

                    Toast.makeText(
                        this@RiwayatTransaksiActivity,
                        "Error detail: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun kembalikanStokProduk(
        transaksi: Transaksi,
        detailPesanan: List<DetailTransaksi>
    ) {
        RetrofitClient.instance.getProduk()
            .enqueue(object : Callback<List<Produk>> {

                override fun onResponse(
                    call: Call<List<Produk>>,
                    response: Response<List<Produk>>
                ) {
                    if (response.isSuccessful) {
                        val listProduk = response.body() ?: emptyList()

                        val detailDenganProduk = detailPesanan.mapNotNull { detail ->
                            val produk = listProduk.find {
                                it.id == detail.id_produk
                            }

                            if (produk != null) {
                                Pair(detail, produk)
                            } else {
                                null
                            }
                        }

                        if (detailDenganProduk.isEmpty()) {
                            updateStatusJadiBatal(transaksi)
                            return
                        }

                        var suksesUpdateStok = 0
                        var gagalUpdateStok = false

                        for ((detail, produk) in detailDenganProduk) {
                            val stokBaru = produk.stok + detail.jumlah

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
                                        if (response.isSuccessful) {
                                            suksesUpdateStok++

                                            if (
                                                suksesUpdateStok == detailDenganProduk.size &&
                                                !gagalUpdateStok
                                            ) {
                                                updateStatusJadiBatal(transaksi)
                                            }

                                        } else {
                                            gagalUpdateStok = true
                                            isProcessingCancel = false

                                            Toast.makeText(
                                                this@RiwayatTransaksiActivity,
                                                "Gagal mengembalikan stok produk",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<Produk>,
                                        t: Throwable
                                    ) {
                                        gagalUpdateStok = true
                                        isProcessingCancel = false

                                        Toast.makeText(
                                            this@RiwayatTransaksiActivity,
                                            "Error stok: ${t.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        }

                    } else {
                        isProcessingCancel = false

                        Toast.makeText(
                            this@RiwayatTransaksiActivity,
                            "Gagal mengambil data produk",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Produk>>,
                    t: Throwable
                ) {
                    isProcessingCancel = false

                    Toast.makeText(
                        this@RiwayatTransaksiActivity,
                        "Error produk: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateStatusJadiBatal(transaksi: Transaksi) {
        val request = TransaksiRequest(
            id_user = transaksi.id_user,
            total_harga = transaksi.total_harga,
            tanggal_transaksi = transaksi.tanggal_transaksi,
            status = "batal"
        )

        RetrofitClient.instance.updateTransaksi(transaksi.id, request)
            .enqueue(object : Callback<TransaksiResponse> {

                override fun onResponse(
                    call: Call<TransaksiResponse>,
                    response: Response<TransaksiResponse>
                ) {
                    isProcessingCancel = false

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RiwayatTransaksiActivity,
                            "Pesanan berhasil dibatalkan",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadRiwayat()

                    } else {
                        Toast.makeText(
                            this@RiwayatTransaksiActivity,
                            "Gagal membatalkan pesanan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<TransaksiResponse>,
                    t: Throwable
                ) {
                    isProcessingCancel = false

                    Toast.makeText(
                        this@RiwayatTransaksiActivity,
                        "Error batal: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}