package com.example.kurshop.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.kurshop.ImageHelper
import com.example.kurshop.R
import com.example.kurshop.formatRupiah
import com.example.kurshop.model.Produk
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ProdukAdapter(
    private val listProduk: List<Produk>,
    private val onTambahCart: (Produk) -> Unit,
    private val onDetailProduk: (Produk) -> Unit
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFotoProduk: ImageView = itemView.findViewById(R.id.ivFotoProduk)
        val progressFoto: ProgressBar = itemView.findViewById(R.id.progressFoto)
        val chipKategori: Chip = itemView.findViewById(R.id.chipKategori)
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val tvStok: TextView = itemView.findViewById(R.id.tvStok)
        val btnTambahCart: MaterialButton = itemView.findViewById(R.id.btnTambahCart)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)

        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProdukViewHolder,
        position: Int
    ) {
        val produk = listProduk[position]

        holder.tvNamaProduk.text = produk.nama
        holder.tvDeskripsi.text = produk.deskripsi
        holder.tvHarga.text = "Rp\u00A0${formatRupiah(produk.harga)}"

        holder.chipKategori.text = when (produk.id_kategori) {
            1 -> "Makanan"
            2 -> "Minuman"
            3 -> "Snack"
            4 -> "Dessert"
            else -> "Lainnya"
        }

        // =========================
        // RESET GAMBAR SAAT RECYCLE
        // =========================
        holder.ivFotoProduk.setImageDrawable(ColorDrawable(Color.WHITE))
        holder.ivFotoProduk.setBackgroundColor(Color.WHITE)
        holder.progressFoto.visibility = View.VISIBLE

        val imageUrl = ImageHelper.getImageUrl(produk.foto)

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(ColorDrawable(Color.WHITE))
            .error(ColorDrawable(Color.WHITE))
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressFoto.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressFoto.visibility = View.GONE
                    return false
                }
            })
            .into(holder.ivFotoProduk)

        holder.itemView.setOnClickListener {
            onDetailProduk(produk)
        }

        if (produk.stok <= 0) {
            holder.tvStok.text = "Stok: Habis"

            holder.btnTambahCart.text = "Habis"
            holder.btnTambahCart.isEnabled = false
            holder.btnTambahCart.alpha = 0.5f

            holder.btnTambahCart.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Produk sedang habis",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            holder.tvStok.text = "Stok: ${produk.stok}"

            holder.btnTambahCart.text = "+ Keranjang"
            holder.btnTambahCart.isEnabled = true
            holder.btnTambahCart.alpha = 1f

            holder.btnTambahCart.setOnClickListener {
                onTambahCart(produk)
            }
        }
    }

    override fun getItemCount(): Int {
        return listProduk.size
    }
}