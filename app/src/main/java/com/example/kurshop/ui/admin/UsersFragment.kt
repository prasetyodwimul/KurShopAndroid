package com.example.kurshop.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.R
import com.example.kurshop.adapter.UsersAdminAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersFragment : Fragment(R.layout.fragment_users) {

    private lateinit var rvUsersAdmin: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsersAdmin = view.findViewById(R.id.rvUsersAdmin)
        rvUsersAdmin.layoutManager = LinearLayoutManager(requireContext())

        loadUsers()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun loadUsers() {
        RetrofitClient.instance.getUsers()
            .enqueue(object : Callback<List<User>> {

                override fun onResponse(
                    call: Call<List<User>>,
                    response: Response<List<User>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: emptyList()

                        rvUsersAdmin.adapter = UsersAdminAdapter(data) { user ->
                            konfirmasiHapus(user)
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengambil data user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<User>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun konfirmasiHapus(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus User")
            .setMessage("Yakin mau hapus ${user.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusUser(user.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusUser(userId: Int) {
        RetrofitClient.instance.deleteUser(userId)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "User berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadUsers()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal menghapus user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<Void>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}