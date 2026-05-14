package com.example.kurshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.formatRupiah
import com.example.kurshop.model.Produk
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.bumptech.glide.Glide
import com.example.kurshop.ImageHelper

class ProdukAdminAdapter(
    private val listProduk: List<Produk>,
    private val onEditProduk: (Produk) -> Unit,
    private val onHapusProduk: (Produk) -> Unit
) : RecyclerView.Adapter<ProdukAdminAdapter.ProdukAdminViewHolder>() {

    class ProdukAdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFotoProduk: ImageView = itemView.findViewById(R.id.ivFotoProduk)
        val chipKategori: Chip = itemView.findViewById(R.id.chipKategori)
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val tvStok: TextView = itemView.findViewById(R.id.tvStok)
        val btnTambahCart: MaterialButton = itemView.findViewById(R.id.btnTambahCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukAdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)

        return ProdukAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukAdminViewHolder, position: Int) {
        val produk = listProduk[position]

        holder.tvNamaProduk.text = produk.nama
        holder.tvDeskripsi.text = produk.deskripsi
        holder.tvHarga.text = "Rp ${formatRupiah(produk.harga)}"
        holder.tvStok.text = "Stok: ${produk.stok}"

        holder.chipKategori.text = when (produk.id_kategori) {
            1 -> "Makanan"
            2 -> "Minuman"
            3 -> "Snack"
            4 -> "Dessert"
            else -> "Lainnya"
        }
        val imageUrl = ImageHelper.getImageUrl(produk.foto)

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.ivFotoProduk)
        holder.btnTambahCart.text = "Edit"

        holder.btnTambahCart.setOnClickListener {
            onEditProduk(produk)
        }

        holder.itemView.setOnLongClickListener {
            onHapusProduk(produk)
            true
        }
    }

    override fun getItemCount(): Int = listProduk.size
}