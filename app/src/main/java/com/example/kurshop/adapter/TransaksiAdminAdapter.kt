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
    private val onUpdateStatus: (Transaksi) -> Unit,
    private val onDetailTransaksi: (Transaksi) -> Unit
) : RecyclerView.Adapter<TransaksiAdminAdapter.TransaksiViewHolder>() {

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdTransaksi: TextView = itemView.findViewById(R.id.tvIdTransaksi)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val btnUpdateStatus: MaterialButton = itemView.findViewById(R.id.btnUpdateStatus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_admin, parent, false)

        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransaksiViewHolder,
        position: Int
    ) {
        val transaksi = listTransaksi[position]
        val status = transaksi.status.lowercase().trim()

        holder.tvIdTransaksi.text = "#TRX-${transaksi.id}"
        holder.chipStatus.text = transaksi.status
        holder.tvTanggal.text = transaksi.tanggal_transaksi
        holder.tvTotalHarga.text = "Rp ${formatRupiah(transaksi.total_harga)}"

        holder.btnUpdateStatus.text = when (status) {
            "pending" -> "Set Dibayar"
            "dibayar" -> "Set Selesai"
            "selesai" -> "Selesai"
            "batal" -> "Dibatalkan"
            else -> "Update"
        }

        holder.btnUpdateStatus.isEnabled =
            status != "selesai" && status != "batal"

        holder.btnUpdateStatus.alpha =
            if (holder.btnUpdateStatus.isEnabled) 1f else 0.5f

        holder.btnUpdateStatus.setOnClickListener {
            onUpdateStatus(transaksi)
        }

        holder.itemView.setOnClickListener {
            onDetailTransaksi(transaksi)
        }
    }

    override fun getItemCount(): Int {
        return listTransaksi.size
    }
}