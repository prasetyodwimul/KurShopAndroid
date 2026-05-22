package com.example.kurshop

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.Produk
import com.example.kurshop.model.Transaksi
import com.example.kurshop.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabTambah: FloatingActionButton

    private lateinit var tvJumlahProduk: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tvJumlahUser: TextView
    private lateinit var tvTotalPendapatan: TextView

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sessionManager = SessionManager(this)

        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        fabTambah = findViewById(R.id.fabTambah)

        tvJumlahProduk = findViewById(R.id.tvJumlahProduk)
        tvJumlahTransaksi = findViewById(R.id.tvJumlahTransaksi)
        tvJumlahUser = findViewById(R.id.tvJumlahUser)
        tvTotalPendapatan = findViewById(R.id.tvTotalPendapatan)

        setupToolbarAmanDariNotch()
        setupViewPager()
        loadStatistik()

        fabTambah.setOnClickListener {
            val currentTab = viewPager.currentItem

            when (currentTab) {
                0 -> {
                    startActivity(
                        Intent(
                            this,
                            TambahProdukActivity::class.java
                        )
                    )
                }

                1 -> {
                    Toast.makeText(
                        this,
                        "Transaksi dibuat dari checkout user",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                2 -> {
                    Toast.makeText(
                        this,
                        "Tambah kategori bisa dilakukan di tab Kategori",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                3 -> {
                    Toast.makeText(
                        this,
                        "Tambah user dilakukan melalui Register",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatistik()
    }

    private fun setupToolbarAmanDariNotch() {
        toolbar.title = "Admin Dashboard"
        toolbar.subtitle = "KurShop"

        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_logout)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuLogout -> {
                    logoutAdmin()
                    true
                }

                else -> false
            }
        }

        val actionBarHeight = getActionBarHeight()

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(
                WindowInsetsCompat.Type.statusBars()
            ).top

            view.setPadding(
                view.paddingLeft,
                statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )

            val params = view.layoutParams
            params.height = actionBarHeight + statusBarHeight
            view.layoutParams = params

            insets
        }
    }

    private fun getActionBarHeight(): Int {
        val typedValue = TypedValue()

        return if (
            theme.resolveAttribute(
                android.R.attr.actionBarSize,
                typedValue,
                true
            )
        ) {
            TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                resources.displayMetrics
            )
        } else {
            56.dpToPx()
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun setupViewPager() {
        val adapter = AdminPagerAdapter(this)
        viewPager.adapter = adapter

        val tabTitles = listOf(
            "Produk",
            "Transaksi",
            "Kategori",
            "Users"
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun loadStatistik() {
        loadJumlahProduk()
        loadJumlahTransaksi()
        loadJumlahUser()
    }

    private fun loadJumlahProduk() {
        RetrofitClient.instance.getProduk()
            .enqueue(object : Callback<List<Produk>> {

                override fun onResponse(
                    call: Call<List<Produk>>,
                    response: Response<List<Produk>>
                ) {
                    if (response.isSuccessful) {
                        val jumlah = response.body()?.size ?: 0
                        tvJumlahProduk.text = jumlah.toString()
                    } else {
                        tvJumlahProduk.text = "0"
                    }
                }

                override fun onFailure(
                    call: Call<List<Produk>>,
                    t: Throwable
                ) {
                    tvJumlahProduk.text = "0"
                }
            })
    }

    private fun loadJumlahTransaksi() {
        RetrofitClient.instance.getTransaksi()
            .enqueue(object : Callback<List<Transaksi>> {

                override fun onResponse(
                    call: Call<List<Transaksi>>,
                    response: Response<List<Transaksi>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: emptyList()
                        val jumlah = data.size
                        val totalPendapatan = data
                            .filter {
                                it.status.lowercase().trim() == "selesai"
                            }
                            .sumOf {
                                it.total_harga.toLong()
                            }

                        tvJumlahTransaksi.text = jumlah.toString()
                        tvTotalPendapatan.text = formatRupiah(totalPendapatan)
                    } else {
                        tvJumlahTransaksi.text = "0"
                        tvTotalPendapatan.text = formatRupiah(0L)
                    }
                }

                override fun onFailure(
                    call: Call<List<Transaksi>>,
                    t: Throwable
                ) {
                    tvJumlahTransaksi.text = "0"
                    tvTotalPendapatan.text = formatRupiah(0L)
                }
            })
    }

    private fun formatRupiah(jumlah: Long): String {
        val formatIndonesia = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${formatIndonesia.format(jumlah)}"
    }

    private fun loadJumlahUser() {
        RetrofitClient.instance.getUsers()
            .enqueue(object : Callback<List<User>> {

                override fun onResponse(
                    call: Call<List<User>>,
                    response: Response<List<User>>
                ) {
                    if (response.isSuccessful) {
                        val jumlah = response.body()?.size ?: 0
                        tvJumlahUser.text = jumlah.toString()
                    } else {
                        tvJumlahUser.text = "0"
                    }
                }

                override fun onFailure(
                    call: Call<List<User>>,
                    t: Throwable
                ) {
                    tvJumlahUser.text = "0"
                }
            })
    }

    private fun logoutAdmin() {
        sessionManager.logout()

        Toast.makeText(
            this,
            "Logout berhasil",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}