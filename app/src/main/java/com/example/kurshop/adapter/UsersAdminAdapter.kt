package com.example.kurshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class UsersAdminAdapter(
    private val listUser: List<User>,
    private val onHapusUser: (User) -> Unit
) : RecyclerView.Adapter<UsersAdminAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaUser: TextView = itemView.findViewById(R.id.tvNamaUser)
        val tvEmailUser: TextView = itemView.findViewById(R.id.tvEmailUser)
        val chipRole: Chip = itemView.findViewById(R.id.chipRole)
        val btnHapusUser: MaterialButton = itemView.findViewById(R.id.btnHapusUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_admin, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = listUser[position]

        holder.tvNamaUser.text = user.nama
        holder.tvEmailUser.text = user.email
        holder.chipRole.text = user.role

        holder.btnHapusUser.setOnClickListener {
            onHapusUser(user)
        }
    }

    override fun getItemCount(): Int = listUser.size
}