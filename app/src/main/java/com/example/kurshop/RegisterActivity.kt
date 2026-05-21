package com.example.kurshop

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.RegisterRequest
import com.example.kurshop.model.User
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            etNama.requestFocus()
            return
        }

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

        val request = RegisterRequest(
            nama = nama,
            email = email,
            password = password,
            role = "user"
        )

        RetrofitClient.instance.register(request)
            .enqueue(object : Callback<User> {

                override fun onResponse(
                    call: Call<User>,
                    response: Response<User>
                ) {
                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Register berhasil, silakan login",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(
                            this@RegisterActivity,
                            LoginActivity::class.java
                        )

                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK

                        startActivity(intent)
                        finish()

                    } else {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Register gagal. Email mungkin sudah digunakan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<User>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Gagal terhubung ke server: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}