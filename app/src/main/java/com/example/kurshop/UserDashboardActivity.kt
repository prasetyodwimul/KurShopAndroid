package com.example.kurshop

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurshop.adapter.ProdukAdapter
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.Kategori
import com.example.kurshop.model.Produk
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvSambutan: TextView
    private lateinit var tvEmptyProduk: TextView
    private lateinit var progressProduk: ProgressBar
    private lateinit var etSearch: TextInputEditText
    private lateinit var llKategori: LinearLayout
    private lateinit var rvProduk: RecyclerView
    private lateinit var fabCart: ExtendedFloatingActionButton

    private lateinit var sessionManager: SessionManager

    private var fullList: List<Produk> = listOf()
    private var filteredList: List<Produk> = listOf()
    private var listKategori: List<Kategori> = listOf()

    private var selectedKategori: Int = 0
    private var keywordSearch: String = ""

    private var idUser: Int = 0
    private var namaUser: String = "Pengguna"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        sessionManager = SessionManager(this)

        idUser = intent.getIntExtra(
            "id_user",
            sessionManager.getIdUser()
        )

        if (idUser != 0) {
            CartSession.init(this, idUser)
        }

        namaUser = intent.getStringExtra("nama_user")
            ?: sessionManager.getNamaUser()

        toolbar = findViewById(R.id.toolbar)
        tvSambutan = findViewById(R.id.tvSambutan)
        tvEmptyProduk = findViewById(R.id.tvEmptyProduk)
        progressProduk = findViewById(R.id.progressProduk)
        etSearch = findViewById(R.id.etSearch)
        llKategori = findViewById(R.id.llKategori)
        rvProduk = findViewById(R.id.rvProduk)
        fabCart = findViewById(R.id.fabCart)

        setupToolbarAmanDariNotch()

        tvSambutan.text = "Halo, $namaUser! 👋"

        rvProduk.layoutManager = GridLayoutManager(this, 2)

        updateBadgeKeranjang()

        setupSearch()
        loadKategori()
        loadProduk()

        fabCart.setOnClickListener {
            val intent = Intent(
                this,
                CartActivity::class.java
            )

            intent.putExtra("id_user", idUser)

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if (idUser != 0) {
            CartSession.init(this, idUser)
        }

        updateBadgeKeranjang()
        loadProduk()
    }

    private fun setupToolbarAmanDariNotch() {
        toolbar.title = "KurShop"

        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_user_dashboard)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.menuRiwayat -> {
                    bukaRiwayatTransaksi()
                    true
                }

                R.id.menuLogout -> {
                    logoutUser()
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

    private fun bukaRiwayatTransaksi() {
        val intent = Intent(
            this,
            RiwayatTransaksiActivity::class.java
        )

        intent.putExtra("id_user", idUser)

        startActivity(intent)
    }

    private fun loadProduk() {
        showLoadingProduk(true)

        RetrofitClient.instance.getProduk()
            .enqueue(object : Callback<List<Produk>> {

                override fun onResponse(
                    call: Call<List<Produk>>,
                    response: Response<List<Produk>>
                ) {
                    showLoadingProduk(false)

                    if (response.isSuccessful) {

                        fullList = response.body() ?: emptyList()
                        applyFilter()

                    } else {
                        Toast.makeText(
                            this@UserDashboardActivity,
                            "Gagal mengambil produk",
                            Toast.LENGTH_SHORT
                        ).show()

                        fullList = emptyList()
                        applyFilter()
                    }
                }

                override fun onFailure(
                    call: Call<List<Produk>>,
                    t: Throwable
                ) {
                    showLoadingProduk(false)

                    Toast.makeText(
                        this@UserDashboardActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    fullList = emptyList()
                    applyFilter()
                }
            })
    }

    private fun showLoadingProduk(isLoading: Boolean) {
        if (isLoading) {
            progressProduk.visibility = View.VISIBLE
            rvProduk.visibility = View.GONE
            tvEmptyProduk.visibility = View.GONE
        } else {
            progressProduk.visibility = View.GONE
        }
    }

    private fun loadKategori() {
        RetrofitClient.instance.getKategori()
            .enqueue(object : Callback<List<Kategori>> {

                override fun onResponse(
                    call: Call<List<Kategori>>,
                    response: Response<List<Kategori>>
                ) {
                    if (response.isSuccessful) {

                        listKategori = response.body() ?: emptyList()
                        setupKategoriDynamic()

                    } else {
                        Toast.makeText(
                            this@UserDashboardActivity,
                            "Gagal mengambil kategori",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Kategori>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@UserDashboardActivity,
                        "Error kategori: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                keywordSearch = s.toString()
                applyFilter()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setupKategoriDynamic() {
        llKategori.removeAllViews()

        tambahChipKategori(
            nama = "Semua",
            idKategori = 0
        )

        for (kategori in listKategori) {
            tambahChipKategori(
                nama = kategori.nama,
                idKategori = kategori.id
            )
        }
    }

    private fun tambahChipKategori(
        nama: String,
        idKategori: Int
    ) {
        val chip = Chip(this)

        chip.text = nama
        chip.isCheckable = true
        chip.isChecked = idKategori == selectedKategori

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 12, 0)
        chip.layoutParams = params

        chip.setOnClickListener {
            selectedKategori = idKategori
            refreshChipState()
            applyFilter()
        }

        llKategori.addView(chip)
    }

    private fun refreshChipState() {
        for (i in 0 until llKategori.childCount) {
            val chip = llKategori.getChildAt(i) as Chip

            val idKategoriChip = if (chip.text.toString() == "Semua") {
                0
            } else {
                listKategori.find {
                    it.nama == chip.text.toString()
                }?.id ?: -1
            }

            chip.isChecked = idKategoriChip == selectedKategori
        }
    }

    private fun applyFilter() {
        filteredList = fullList.filter { produk ->

            val cocokKategori =
                selectedKategori == 0 ||
                        produk.id_kategori == selectedKategori

            val cocokSearch =
                produk.nama.contains(
                    keywordSearch,
                    ignoreCase = true
                ) ||
                        produk.deskripsi.contains(
                            keywordSearch,
                            ignoreCase = true
                        )

            cocokKategori && cocokSearch
        }

        rvProduk.adapter = ProdukAdapter(
            listProduk = filteredList,

            onTambahCart = { produk ->
                tambahKeKeranjang(produk)
            },

            onDetailProduk = { produk ->
                bukaDetailProduk(produk)
            }
        )

        if (filteredList.isEmpty()) {
            tvEmptyProduk.visibility = View.VISIBLE
            rvProduk.visibility = View.GONE
        } else {
            tvEmptyProduk.visibility = View.GONE
            rvProduk.visibility = View.VISIBLE
        }
    }

    private fun tambahKeKeranjang(produk: Produk) {
        val berhasil = CartSession.tambahProduk(produk)

        if (berhasil) {

            updateBadgeKeranjang()

            Toast.makeText(
                this,
                "${produk.nama} ditambahkan ke keranjang",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            Toast.makeText(
                this,
                "Stok ${produk.nama} tidak mencukupi",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun bukaDetailProduk(produk: Produk) {
        val intent = Intent(
            this,
            DetailProdukActivity::class.java
        )

        intent.putExtra("id_produk", produk.id)
        intent.putExtra("nama", produk.nama)
        intent.putExtra("deskripsi", produk.deskripsi)
        intent.putExtra("harga", produk.harga)
        intent.putExtra("stok", produk.stok)
        intent.putExtra("foto", produk.foto ?: "")
        intent.putExtra("id_kategori", produk.id_kategori)

        startActivity(intent)
    }

    private fun logoutUser() {
        sessionManager.logout()

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

    private fun updateBadgeKeranjang() {
        val totalItem = CartSession.totalItem()

        fabCart.text = if (totalItem > 0) {
            "Keranjang ($totalItem)"
        } else {
            "Keranjang"
        }
    }
}