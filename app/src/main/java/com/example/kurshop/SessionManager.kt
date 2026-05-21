package com.example.kurshop

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("kurshop_session", Context.MODE_PRIVATE)

    fun saveLogin(
        idUser: Int,
        nama: String,
        email: String,
        role: String
    ) {
        val editor = sharedPreferences.edit()

        editor.putBoolean("is_login", true)
        editor.putInt("id_user", idUser)
        editor.putString("nama_user", nama)
        editor.putString("email_user", email)
        editor.putString("role_user", role)

        editor.apply()
    }

    fun isLogin(): Boolean {
        return sharedPreferences.getBoolean("is_login", false)
    }

    fun getIdUser(): Int {
        return sharedPreferences.getInt("id_user", 0)
    }

    fun getNamaUser(): String {
        return sharedPreferences.getString("nama_user", "Pengguna") ?: "Pengguna"
    }

    fun getEmailUser(): String {
        return sharedPreferences.getString("email_user", "") ?: ""
    }

    fun getRoleUser(): String {
        return sharedPreferences.getString("role_user", "user") ?: "user"
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}