package com.example.kurshop

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.LoginRequest
import com.example.kurshop.model.LoginResponse
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: TextView

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLogin()) {
            pindahSesuaiRole(
                idUser = sessionManager.getIdUser(),
                nama = sessionManager.getNamaUser(),
                email = sessionManager.getEmailUser(),
                role = sessionManager.getRoleUser()
            )
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            loginUser()
        }

        tvRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"
            etPassword.requestFocus()
            return
        }

        val request = LoginRequest(
            email = email,
            password = password
        )

        RetrofitClient.instance.login(request)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val user = response.body()?.user

                        if (user != null) {

                            sessionManager.saveLogin(
                                idUser = user.id,
                                nama = user.nama,
                                email = user.email,
                                role = user.role
                            )

                            Toast.makeText(
                                this@LoginActivity,
                                "Login berhasil, ${user.nama}",
                                Toast.LENGTH_SHORT
                            ).show()

                            pindahSesuaiRole(
                                idUser = user.id,
                                nama = user.nama,
                                email = user.email,
                                role = user.role
                            )

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Data user kosong dari server",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Email atau password salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Gagal terhubung ke server: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun pindahSesuaiRole(
        idUser: Int,
        nama: String,
        email: String,
        role: String
    ) {
        val intent = if (role.lowercase() == "admin") {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, UserDashboardActivity::class.java)
        }

        intent.putExtra("id_user", idUser)
        intent.putExtra("nama_user", nama)
        intent.putExtra("email_user", email)
        intent.putExtra("role_user", role)

        startActivity(intent)
        finish()
    }
}