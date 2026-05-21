package com.example.kurshop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransaksiFragment : Fragment(R.layout.fragment_transaksi) {

    private lateinit var rvTransaksiAdmin: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTransaksiAdmin = view.findViewById(R.id.rvTransaksiAdmin)
        rvTransaksiAdmin.layoutManager = LinearLayoutManager(requireContext())

        loadTransaksi()
    }

    override fun onResume() {
        super.onResume()
        loadTransaksi()
    }

    private fun loadTransaksi() {
        RetrofitClient.instance.getTransaksi()
            .enqueue(object : Callback<List<Transaksi>> {

                override fun onResponse(
                    call: Call<List<Transaksi>>,
                    response: Response<List<Transaksi>>
                ) {
                    if (response.isSuccessful) {

                        val data = response.body() ?: emptyList()

                        val sortedData = data.sortedByDescending {
                            it.id
                        }

                        rvTransaksiAdmin.adapter = TransaksiAdminAdapter(
                            listTransaksi = sortedData,

                            onUpdateStatus = { transaksi ->
                                updateStatusTransaksi(transaksi)
                            },

                            onDetailTransaksi = { transaksi ->
                                bukaDetailTransaksi(transaksi)
                            }
                        )

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
                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}