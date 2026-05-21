package com.example.kurshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.model.Kategori
import com.google.android.material.button.MaterialButton

class KategoriAdminAdapter(
    private val listKategori: List<Kategori>,
    private val onHapusKategori: (Kategori) -> Unit
) : RecyclerView.Adapter<KategoriAdminAdapter.KategoriViewHolder>() {

    class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val btnHapusKategori: MaterialButton = itemView.findViewById(R.id.btnHapusKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kategori_admin, parent, false)

        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = listKategori[position]

        holder.tvNamaKategori.text = kategori.nama

        holder.btnHapusKategori.setOnClickListener {
            onHapusKategori(kategori)
        }
    }

    override fun getItemCount(): Int = listKategori.size
}