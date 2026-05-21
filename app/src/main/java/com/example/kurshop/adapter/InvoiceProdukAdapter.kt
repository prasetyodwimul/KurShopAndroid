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

class InvoiceProdukAdapter(
    private val listDetail: List<DetailTransaksi>,
    private val listProduk: List<Produk>
) : RecyclerView.Adapter<InvoiceProdukAdapter.InvoiceViewHolder>() {

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice_produk, parent, false)

        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: InvoiceViewHolder,
        position: Int
    ) {
        val detail = listDetail[position]

        val produk = listProduk.find {
            it.id == detail.id_produk
        }

        val namaProduk = produk?.nama ?: "Produk ID ${detail.id_produk}"
        val hargaProduk = if (detail.jumlah > 0) {
            detail.subtotal / detail.jumlah
        } else {
            0
        }

        holder.tvNamaProduk.text = namaProduk
        holder.tvJumlah.text =
            "${detail.jumlah} x Rp ${formatRupiah(hargaProduk)}"

        holder.tvSubtotal.text =
            "Rp ${formatRupiah(detail.subtotal)}"
    }

    override fun getItemCount(): Int {
        return listDetail.size
    }
}