package com.example.kurshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.formatRupiah
import com.example.kurshop.model.Transaksi
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class TransaksiAdminAdapter(
    private val listTransaksi: List<Transaksi>,
    private val onUpdateStatus: (Transaksi) -> Unit
) : RecyclerView.Adapter<TransaksiAdminAdapter.TransaksiViewHolder>() {

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdTransaksi: TextView = itemView.findViewById(R.id.tvIdTransaksi)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvNamaUser: TextView = itemView.findViewById(R.id.tvNamaUser)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val btnUpdateStatus: MaterialButton = itemView.findViewById(R.id.btnUpdateStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_admin, parent, false)

        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val transaksi = listTransaksi[position]

        holder.tvIdTransaksi.text = "#TRX-${transaksi.id}"
        holder.chipStatus.text = transaksi.status
        holder.tvNamaUser.text = "User ID ${transaksi.id_user}"
        holder.tvTanggal.text = transaksi.tanggal_transaksi
        holder.tvTotalHarga.text = "Rp ${formatRupiah(transaksi.total_harga)}"

        holder.btnUpdateStatus.text = when (transaksi.status.lowercase()) {
            "pending" -> "Set Dibayar"
            "dibayar" -> "Set Selesai"
            "selesai" -> "Selesai"
            "batal" -> "Dibatalkan"
            else -> "Update"
        }

        holder.btnUpdateStatus.isEnabled =
            transaksi.status.lowercase() != "selesai" &&
                    transaksi.status.lowercase() != "batal"

        holder.btnUpdateStatus.setOnClickListener {
            onUpdateStatus(transaksi)
        }
    }

    override fun getItemCount(): Int = listTransaksi.size
}