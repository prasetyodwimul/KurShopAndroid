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

class RiwayatTransaksiAdapter(
    private val listTransaksi: List<Transaksi>,
    private val onClick: (Transaksi) -> Unit,
    private val onBatalPesanan: (Transaksi) -> Unit
) : RecyclerView.Adapter<RiwayatTransaksiAdapter.RiwayatViewHolder>() {

    class RiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdTransaksi: TextView = itemView.findViewById(R.id.tvIdTransaksi)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val btnBatalPesanan: MaterialButton = itemView.findViewById(R.id.btnBatalPesanan)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_transaksi, parent, false)

        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RiwayatViewHolder,
        position: Int
    ) {
        val transaksi = listTransaksi[position]
        val status = transaksi.status.lowercase()

        holder.tvIdTransaksi.text = "#TRX-${transaksi.id}"
        holder.chipStatus.text = transaksi.status
        holder.tvTanggal.text = transaksi.tanggal_transaksi
        holder.tvTotalHarga.text = "Rp ${formatRupiah(transaksi.total_harga)}"

        holder.itemView.setOnClickListener {
            onClick(transaksi)
        }

        if (status == "pending") {
            holder.btnBatalPesanan.visibility = View.VISIBLE
            holder.btnBatalPesanan.isEnabled = true
            holder.btnBatalPesanan.text = "Batalkan"

            holder.btnBatalPesanan.setOnClickListener {
                onBatalPesanan(transaksi)
            }

        } else {
            holder.btnBatalPesanan.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return listTransaksi.size
    }
}