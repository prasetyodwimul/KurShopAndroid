package com.example.kurshop

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.adapter.InvoiceProdukAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.DetailTransaksi
import com.example.kurshop.model.Produk
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvoiceActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvTanggal: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTotalHarga: TextView
    private lateinit var rvInvoiceProduk: RecyclerView
    private lateinit var btnKembaliDashboard: MaterialButton

    private var idTransaksi: Int = 0
    private var idUser: Int = 0
    private var tanggalTransaksi: String = "-"
    private var statusTransaksi: String = "pending"
    private var totalHarga: Int = 0

    private var listProduk: List<Produk> = emptyList()
    private var listDetail: List<DetailTransaksi> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        idTransaksi = intent.getIntExtra("id_transaksi", 0)
        idUser = intent.getIntExtra("id_user", 0)
        tanggalTransaksi = intent.getStringExtra("tanggal_transaksi") ?: "-"
        statusTransaksi = intent.getStringExtra("status") ?: "pending"
        totalHarga = intent.getIntExtra("total_harga", 0)

        toolbar = findViewById(R.id.toolbar)
        tvIdTransaksi = findViewById(R.id.tvIdTransaksi)
        tvTanggal = findViewById(R.id.tvTanggal)
        tvStatus = findViewById(R.id.tvStatus)
        tvTotalHarga = findViewById(R.id.tvTotalHarga)
        rvInvoiceProduk = findViewById(R.id.rvInvoiceProduk)
        btnKembaliDashboard = findViewById(R.id.btnKembaliDashboard)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Struk Transaksi"

        rvInvoiceProduk.layoutManager = LinearLayoutManager(this)

        tampilkanHeaderInvoice()
        loadProduk()

        btnKembaliDashboard.setOnClickListener {
            kembaliKeDashboard()
        }
    }

    override fun onBackPressed() {
        kembaliKeDashboard()
    }

    private fun tampilkanHeaderInvoice() {
        tvIdTransaksi.text = "ID Transaksi: #TRX-$idTransaksi"
        tvTanggal.text = "Tanggal: $tanggalTransaksi"
        tvStatus.text = "Status: $statusTransaksi"
        tvTotalHarga.text = "Rp ${formatRupiah(totalHarga)}"
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
                            this@InvoiceActivity,
                            "Gagal mengambil data produk",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Produk>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@InvoiceActivity,
                        "Error produk: ${t.message}",
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

                        rvInvoiceProduk.adapter =
                            InvoiceProdukAdapter(
                                listDetail = listDetail,
                                listProduk = listProduk
                            )

                    } else {
                        Toast.makeText(
                            this@InvoiceActivity,
                            "Gagal mengambil detail transaksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<DetailTransaksi>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@InvoiceActivity,
                        "Error detail: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun kembaliKeDashboard() {
        val sessionManager = SessionManager(this)

        val intent = Intent(
            this,
            UserDashboardActivity::class.java
        )

        intent.putExtra("id_user", idUser)
        intent.putExtra("nama_user", sessionManager.getNamaUser())

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}