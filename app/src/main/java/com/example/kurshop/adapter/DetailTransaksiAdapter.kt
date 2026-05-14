package com.example.kurshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.formatRupiah
import com.example.kurshop.model.DetailTransaksi
import com.example.kurshop.model.Produk

class DetailTransaksiAdapter(
    private val listDetail: List<DetailTransaksi>,
    private val listProduk: List<Produk>
) : RecyclerView.Adapter<DetailTransaksiAdapter.DetailViewHolder>() {

    class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_transaksi, parent, false)

        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val detail = listDetail[position]

        val produk = listProduk.find {
            it.id == detail.id_produk
        }

        holder.tvNamaProduk.text = produk?.nama ?: "Produk ID ${detail.id_produk}"
        holder.tvJumlah.text = "Jumlah: ${detail.jumlah}"
        holder.tvSubtotal.text = "Rp ${formatRupiah(detail.subtotal)}"
    }

    override fun getItemCount(): Int = listDetail.size
}