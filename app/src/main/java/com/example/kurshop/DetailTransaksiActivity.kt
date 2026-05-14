package com.example.kurshop

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.adapter.DetailTransaksiAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.DetailTransaksi
import com.example.kurshop.model.Produk
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailTransaksiActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTotalHarga: TextView
    private lateinit var rvDetailTransaksi: RecyclerView

    private var idTransaksi: Int = 0
    private var status: String = ""
    private var totalHarga: Int = 0

    private var listProduk: List<Produk> = emptyList()
    private var listDetail: List<DetailTransaksi> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaksi)

        idTransaksi = intent.getIntExtra("id_transaksi", 0)
        status = intent.getStringExtra("status") ?: "-"
        totalHarga = intent.getIntExtra("total_harga", 0)

        toolbar = findViewById(R.id.toolbar)
        tvIdTransaksi = findViewById(R.id.tvIdTransaksi)
        tvStatus = findViewById(R.id.tvStatus)
        tvTotalHarga = findViewById(R.id.tvTotalHarga)
        rvDetailTransaksi = findViewById(R.id.rvDetailTransaksi)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tvIdTransaksi.text = "#TRX-$idTransaksi"
        tvStatus.text = "Status: $status"
        tvTotalHarga.text = "Rp ${formatRupiah(totalHarga)}"

        rvDetailTransaksi.layoutManager = LinearLayoutManager(this)

        loadProduk()
    }

    private fun loadProduk() {
        RetrofitClient.instance.getProduk()
            .enqueue(object : Callback<List<Produk>> {

                override fun onResponse(
                    call: Call<List<Produk>>,
                    response: Response<List<Produk>>
                ) {
                    if (response.isSuccessful) {
                        listProduk = response.body() ?: emptyList()
                        loadDetailTransaksi()
                    } else {
                        Toast.makeText(
                            this@DetailTransaksiActivity,
                            "Gagal mengambil produk",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Produk>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@DetailTransaksiActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadDetailTransaksi() {
        RetrofitClient.instance.getDetailTransaksi()
            .enqueue(object : Callback<List<DetailTransaksi>> {

                override fun onResponse(
                    call: Call<List<DetailTransaksi>>,
                    response: Response<List<DetailTransaksi>>
                ) {
                    if (response.isSuccessful) {

                        val semuaDetail = response.body() ?: emptyList()

                        listDetail = semuaDetail.filter {
                            it.id_transaksi == idTransaksi
                        }

                        if (listDetail.isEmpty()) {
                            Toast.makeText(
                                this@DetailTransaksiActivity,
                                "Detail transaksi kosong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        rvDetailTransaksi.adapter =
                            DetailTransaksiAdapter(listDetail, listProduk)

                    } else {

                        Toast.makeText(
                            this@DetailTransaksiActivity,
                            "Gagal detail transaksi. Code: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<DetailTransaksi>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@DetailTransaksiActivity,
                        "Error detail: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}