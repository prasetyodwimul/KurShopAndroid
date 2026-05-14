package com.example.kurshop.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.adapter.KategoriAdminAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.Kategori
import com.example.kurshop.model.KategoriRequest
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KategoriFragment : Fragment(R.layout.fragment_kategori) {

    private lateinit var etNamaKategori: EditText
    private lateinit var btnTambahKategori: MaterialButton
    private lateinit var rvKategoriAdmin: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNamaKategori = view.findViewById(R.id.etNamaKategori)
        btnTambahKategori = view.findViewById(R.id.btnTambahKategori)
        rvKategoriAdmin = view.findViewById(R.id.rvKategoriAdmin)

        rvKategoriAdmin.layoutManager = LinearLayoutManager(requireContext())

        btnTambahKategori.setOnClickListener {
            tambahKategori()
        }

        loadKategori()
    }

    override fun onResume() {
        super.onResume()
        loadKategori()
    }

    private fun loadKategori() {
        RetrofitClient.instance.getKategori()
            .enqueue(object : Callback<List<Kategori>> {

                override fun onResponse(
                    call: Call<List<Kategori>>,
                    response: Response<List<Kategori>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: emptyList()

                        rvKategoriAdmin.adapter = KategoriAdminAdapter(data) { kategori ->
                            konfirmasiHapus(kategori)
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengambil kategori",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Kategori>>,
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

    private fun tambahKategori() {
        val namaKategori = etNamaKategori.text.toString().trim()

        if (namaKategori.isEmpty()) {
            etNamaKategori.error = "Nama kategori wajib diisi"
            etNamaKategori.requestFocus()
            return
        }

        val request = KategoriRequest(
            nama = namaKategori
        )

        RetrofitClient.instance.tambahKategori(request)
            .enqueue(object : Callback<Kategori> {

                override fun onResponse(
                    call: Call<Kategori>,
                    response: Response<Kategori>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Kategori berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()

                        etNamaKategori.text.clear()
                        loadKategori()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal menambahkan kategori",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<Kategori>,
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

    private fun konfirmasiHapus(kategori: Kategori) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Yakin mau hapus kategori ${kategori.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusKategori(kategori.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusKategori(kategoriId: Int) {
        RetrofitClient.instance.deleteKategori(kategoriId)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Kategori berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadKategori()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal menghapus kategori",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<Void>,
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