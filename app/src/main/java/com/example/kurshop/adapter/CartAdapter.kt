package com.example.kurshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurshop.ImageHelper
import com.example.kurshop.R
import com.example.kurshop.formatRupiah
import com.example.kurshop.model.CartItem
import com.google.android.material.button.MaterialButton

class CartAdapter(
    private val listCart: List<CartItem>,
    private val onTambah: (CartItem) -> Unit,
    private val onKurang: (CartItem) -> Unit,
    private val onHapus: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFoto)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val btnKurang: MaterialButton = itemView.findViewById(R.id.btnKurang)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val btnTambah: MaterialButton = itemView.findViewById(R.id.btnTambah)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        val ivHapus: ImageView = itemView.findViewById(R.id.ivHapus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)

        return CartViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) {
        val item = listCart[position]
        val produk = item.produk

        holder.tvNama.text = produk.nama
        holder.tvHarga.text = "Rp ${formatRupiah(produk.harga)}"
        holder.tvJumlah.text = item.jumlah.toString()
        holder.tvSubtotal.text = "Rp ${formatRupiah(item.subtotal())}"

        val imageUrl = ImageHelper.getImageUrl(produk.foto)

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.ivFoto)

        holder.btnTambah.setOnClickListener {
            onTambah(item)
        }

        holder.btnKurang.setOnClickListener {
            onKurang(item)
        }

        holder.ivHapus.setOnClickListener {
            onHapus(item)
        }
    }

    override fun getItemCount(): Int {
        return listCart.size
    }
}