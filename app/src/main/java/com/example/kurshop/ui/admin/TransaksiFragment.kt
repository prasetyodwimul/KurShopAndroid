package com.example.kurshop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.DetailTransaksiActivity
import com.example.kurshop.R
import com.example.kurshop.adapter.TransaksiAdminAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.Transaksi
import com.example.kurshop.model.TransaksiRequest
import com.example.kurshop.model.TransaksiResponse
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class TransaksiFragment : Fragment(R.layout.fragment_transaksi) {

    private lateinit var rvTransaksiAdmin: RecyclerView
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var tvEmptyTransaksiAdmin: TextView
    private lateinit var tvTotalPendapatanTransaksi: TextView
    private lateinit var layoutLoadingTransaksiAdmin: LinearLayout

    private var semuaTransaksi: List<Transaksi> = emptyList()
    private var statusFilterAktif: String = "semua"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTransaksiAdmin = view.findViewById(R.id.rvTransaksiAdmin)
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus)
        tvEmptyTransaksiAdmin = view.findViewById(R.id.tvEmptyTransaksiAdmin)
        tvTotalPendapatanTransaksi = view.findViewById(R.id.tvTotalPendapatanTransaksi)
        layoutLoadingTransaksiAdmin = view.findViewById(R.id.layoutLoadingTransaksiAdmin)

        rvTransaksiAdmin.layoutManager = LinearLayoutManager(requireContext())

        setupFilterStatus()
        loadTransaksi()
    }

    override fun onResume() {
        super.onResume()
        loadTransaksi()
    }

    private fun setupFilterStatus() {
        chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->

            if (checkedIds.isEmpty()) {
                statusFilterAktif = "semua"
                tampilkanTransaksi()
                return@setOnCheckedStateChangeListener
            }

            statusFilterAktif = when (checkedIds.first()) {
                R.id.chipSemua -> "semua"
                R.id.chipPending -> "pending"
                R.id.chipDibayar -> "dibayar"
                R.id.chipSelesai -> "selesai"
                R.id.chipBatal -> "batal"
                else -> "semua"
            }

            tampilkanTransaksi()
        }
    }

    private fun loadTransaksi() {
        tampilkanLoading(true)

        RetrofitClient.instance.getTransaksi()
            .enqueue(object : Callback<List<Transaksi>> {

                override fun onResponse(
                    call: Call<List<Transaksi>>,
                    response: Response<List<Transaksi>>
                ) {
                    if (!isAdded) return

                    tampilkanLoading(false)

                    if (response.isSuccessful) {

                        semuaTransaksi = response.body()
                            ?.sortedByDescending {
                                it.id
                            }
                            ?: emptyList()

                        updateTotalPendapatan()
                        tampilkanTransaksi()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengambil transaksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Transaksi>>,
                    t: Throwable
                ) {
                    if (!isAdded) return

                    tampilkanLoading(false)

                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun tampilkanLoading(sedangLoading: Boolean) {
        layoutLoadingTransaksiAdmin.visibility = if (sedangLoading) View.VISIBLE else View.GONE
        rvTransaksiAdmin.visibility = if (sedangLoading) View.GONE else View.VISIBLE
        tvEmptyTransaksiAdmin.visibility = View.GONE
    }

    private fun tampilkanTransaksi() {
        val transaksiTampil = if (statusFilterAktif == "semua") {
            semuaTransaksi
        } else {
            semuaTransaksi.filter {
                it.status.lowercase().trim() == statusFilterAktif
            }
        }

        if (transaksiTampil.isEmpty()) {
            tvEmptyTransaksiAdmin.visibility = View.VISIBLE
            rvTransaksiAdmin.visibility = View.GONE
        } else {
            tvEmptyTransaksiAdmin.visibility = View.GONE
            rvTransaksiAdmin.visibility = View.VISIBLE
        }

        rvTransaksiAdmin.adapter = TransaksiAdminAdapter(
            listTransaksi = transaksiTampil,

            onUpdateStatus = { transaksi ->
                updateStatusTransaksi(transaksi)
            },

            onDetailTransaksi = { transaksi ->
                bukaDetailTransaksi(transaksi)
            }
        )
    }

    private fun updateTotalPendapatan() {
        val totalPendapatan = semuaTransaksi
            .filter {
                it.status.lowercase().trim() == "selesai"
            }
            .sumOf {
                it.total_harga.toLong()
            }

        tvTotalPendapatanTransaksi.text = formatRupiah(totalPendapatan)
    }

    private fun formatRupiah(jumlah: Long): String {
        val formatIndonesia = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${formatIndonesia.format(jumlah)}"
    }

    private fun bukaDetailTransaksi(transaksi: Transaksi) {
        val intent = Intent(
            requireContext(),
            DetailTransaksiActivity::class.java
        )

        intent.putExtra("id_transaksi", transaksi.id)
        intent.putExtra("status", transaksi.status)
        intent.putExtra("total_harga", transaksi.total_harga)

        startActivity(intent)
    }

    private fun updateStatusTransaksi(transaksi: Transaksi) {
        val statusSekarang = transaksi.status.lowercase().trim()

        if (statusSekarang == "batal") {
            Toast.makeText(
                requireContext(),
                "Transaksi sudah dibatalkan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (statusSekarang == "selesai") {
            Toast.makeText(
                requireContext(),
                "Transaksi sudah selesai",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val statusBaru = when (statusSekarang) {
            "pending" -> "dibayar"
            "dibayar" -> "selesai"
            else -> transaksi.status
        }

        if (statusBaru == transaksi.status) {
            Toast.makeText(
                requireContext(),
                "Status tidak bisa diubah",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request = TransaksiRequest(
            id_user = transaksi.id_user,
            total_harga = transaksi.total_harga,
            tanggal_transaksi = transaksi.tanggal_transaksi,
            status = statusBaru
        )

        RetrofitClient.instance.updateTransaksi(
            transaksi.id,
            request
        ).enqueue(object : Callback<TransaksiResponse> {

            override fun onResponse(
                call: Call<TransaksiResponse>,
                response: Response<TransaksiResponse>
            ) {
                if (!isAdded) return

                if (response.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        "Status diubah menjadi $statusBaru",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadTransaksi()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal update status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<TransaksiResponse>,
                t: Throwable
            ) {
                if (!isAdded) return

                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
