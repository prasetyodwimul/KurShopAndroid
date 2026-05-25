package com.example.kurshop.ui.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.TambahProdukActivity
import com.example.kurshop.adapter.ProdukAdminAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.Produk
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProdukAdminFragment : Fragment(R.layout.fragment_produk_admin) {

    private lateinit var etSearchProdukAdmin: TextInputEditText
    private lateinit var tvEmptyProdukAdmin: TextView
    private lateinit var rvProdukAdmin: RecyclerView

    private var fullListProduk: List<Produk> = emptyList()
    private var filteredListProduk: List<Produk> = emptyList()

    private var keywordSearch: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearchProdukAdmin = view.findViewById(R.id.etSearchProdukAdmin)
        tvEmptyProdukAdmin = view.findViewById(R.id.tvEmptyProdukAdmin)
        rvProdukAdmin = view.findViewById(R.id.rvProdukAdmin)

        rvProdukAdmin.layoutManager = GridLayoutManager(requireContext(), 2)

        setupSearch()
        loadProduk()
    }

    override fun onResume() {
        super.onResume()
        loadProduk()
    }

    private fun setupSearch() {
        etSearchProdukAdmin.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                keywordSearch = s.toString().trim()
                applyFilterProduk()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun loadProduk() {
        RetrofitClient.instance.getProduk()
            .enqueue(object : Callback<List<Produk>> {

                override fun onResponse(
                    call: Call<List<Produk>>,
                    response: Response<List<Produk>>
                ) {
                    if (response.isSuccessful) {

                        fullListProduk = response.body()
                            ?.sortedByDescending {
                                it.id
                            }
                            ?: emptyList()

                        applyFilterProduk()

                    } else {
                        Toast.makeText(
                            requireContext(),
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
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun applyFilterProduk() {
        filteredListProduk = if (keywordSearch.isEmpty()) {

            fullListProduk

        } else {

            fullListProduk.filter { produk ->

                val cocokNama = produk.nama.contains(
                    keywordSearch,
                    ignoreCase = true
                )

                val cocokDeskripsi = produk.deskripsi.contains(
                    keywordSearch,
                    ignoreCase = true
                )

                cocokNama || cocokDeskripsi
            }
        }

        tampilkanProduk()
    }

    private fun tampilkanProduk() {
        rvProdukAdmin.adapter = ProdukAdminAdapter(
            listProduk = filteredListProduk,

            onEditProduk = { produk ->
                bukaEditProduk(produk)
            },

            onHapusProduk = { produk ->
                konfirmasiHapus(produk)
            }
        )

        if (filteredListProduk.isEmpty()) {
            tvEmptyProdukAdmin.visibility = View.VISIBLE
            rvProdukAdmin.visibility = View.GONE
        } else {
            tvEmptyProdukAdmin.visibility = View.GONE
            rvProdukAdmin.visibility = View.VISIBLE
        }
    }

    private fun bukaEditProduk(produk: Produk) {
        val intent = Intent(
            requireContext(),
            TambahProdukActivity::class.java
        )

        intent.putExtra("mode", "edit")
        intent.putExtra("id_produk", produk.id)
        intent.putExtra("nama", produk.nama)
        intent.putExtra("deskripsi", produk.deskripsi)
        intent.putExtra("harga", produk.harga)
        intent.putExtra("stok", produk.stok)
        intent.putExtra("foto", produk.foto ?: "")
        intent.putExtra("id_kategori", produk.id_kategori)

        startActivity(intent)
    }

    private fun konfirmasiHapus(produk: Produk) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin mau hapus ${produk.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusProduk(produk.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusProduk(produkId: Int) {
        RetrofitClient.instance.deleteProduk(produkId)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Produk berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadProduk()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal menghapus produk",
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