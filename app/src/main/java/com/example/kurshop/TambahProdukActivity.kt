package com.example.kurshop

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kurshop.api.RetrofitClient
import com.example.kurshop.model.Kategori
import com.example.kurshop.model.Produk
import com.example.kurshop.model.ProdukRequest
import com.example.kurshop.model.UploadResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class TambahProdukActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivPreviewFoto: ImageView
    private lateinit var btnPilihGaleri: MaterialButton
    private lateinit var btnKamera: MaterialButton

    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etDeskripsi: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var spinnerKategori: AutoCompleteTextView
    private lateinit var btnSimpan: MaterialButton

    private var idKategoriDipilih: Int = 0
    private var fotoProduk: String = "default.jpg"
    private var selectedImageUri: Uri? = null

    private var mode: String = "tambah"
    private var idProdukEdit: Int = 0

    private var kategoriList: List<Kategori> = listOf()

    private val pilihGaleriLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri

                Glide.with(this)
                    .load(uri)
                    .placeholder(ColorDrawable(Color.WHITE))
                    .error(ColorDrawable(Color.WHITE))
                    .into(ivPreviewFoto)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_produk)

        toolbar = findViewById(R.id.toolbar)
        ivPreviewFoto = findViewById(R.id.ivPreviewFoto)
        btnPilihGaleri = findViewById(R.id.btnPilihGaleri)
        btnKamera = findViewById(R.id.btnKamera)

        etNamaProduk = findViewById(R.id.etNamaProduk)
        etDeskripsi = findViewById(R.id.etDeskripsi)
        etHarga = findViewById(R.id.etHarga)
        etStok = findViewById(R.id.etStok)
        spinnerKategori = findViewById(R.id.spinnerKategori)
        btnSimpan = findViewById(R.id.btnSimpan)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Ubah fungsi tombol kamera menjadi input link foto
        btnKamera.text = "LINK FOTO"

        cekModeEdit()
        setupKategori()

        btnPilihGaleri.setOnClickListener {
            pilihGaleriLauncher.launch("image/*")
        }

        btnKamera.setOnClickListener {
            tampilkanDialogLinkFoto()
        }

        btnSimpan.setOnClickListener {
            if (selectedImageUri != null) {
                uploadFotoDulu()
            } else {
                prosesSimpanAtauUpdate()
            }
        }
    }

    private fun cekModeEdit() {
        mode = intent.getStringExtra("mode") ?: "tambah"

        if (mode == "edit") {
            idProdukEdit = intent.getIntExtra("id_produk", 0)

            val nama = intent.getStringExtra("nama") ?: ""
            val deskripsi = intent.getStringExtra("deskripsi") ?: ""
            val harga = intent.getIntExtra("harga", 0)
            val stok = intent.getIntExtra("stok", 0)
            val foto = intent.getStringExtra("foto") ?: "default.jpg"
            val idKategori = intent.getIntExtra("id_kategori", 0)

            fotoProduk = foto
            idKategoriDipilih = idKategori

            etNamaProduk.setText(nama)
            etDeskripsi.setText(deskripsi)
            etHarga.setText(harga.toString())
            etStok.setText(stok.toString())

            if (fotoProduk.isNotEmpty()) {
                Glide.with(this)
                    .load(ImageHelper.getImageUrl(fotoProduk))
                    .placeholder(ColorDrawable(Color.WHITE))
                    .error(ColorDrawable(Color.WHITE))
                    .into(ivPreviewFoto)
            }

            toolbar.title = "Edit Produk"
            btnSimpan.text = "UPDATE PRODUK"

        } else {
            toolbar.title = "Tambah Produk"
            btnSimpan.text = "SIMPAN PRODUK"

            ivPreviewFoto.setImageDrawable(ColorDrawable(Color.WHITE))
            ivPreviewFoto.setBackgroundColor(Color.WHITE)
        }
    }

    private fun setupKategori() {
        RetrofitClient.instance.getKategori()
            .enqueue(object : Callback<List<Kategori>> {

                override fun onResponse(
                    call: Call<List<Kategori>>,
                    response: Response<List<Kategori>>
                ) {
                    if (response.isSuccessful) {
                        kategoriList = response.body() ?: emptyList()

                        val namaKategoriList = kategoriList.map {
                            it.nama
                        }

                        val adapter = ArrayAdapter(
                            this@TambahProdukActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            namaKategoriList
                        )

                        spinnerKategori.setAdapter(adapter)

                        spinnerKategori.setOnItemClickListener { _, _, position, _ ->
                            idKategoriDipilih = kategoriList[position].id
                        }

                        setKategoriEditJikaAda()

                    } else {
                        Toast.makeText(
                            this@TambahProdukActivity,
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
                        this@TambahProdukActivity,
                        "Error kategori: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setKategoriEditJikaAda() {
        if (mode == "edit" && idKategoriDipilih != 0) {
            val kategoriEdit = kategoriList.find {
                it.id == idKategoriDipilih
            }

            if (kategoriEdit != null) {
                spinnerKategori.setText(kategoriEdit.nama, false)
            }
        }
    }

    private fun tampilkanDialogLinkFoto() {
        val inputLink = EditText(this)

        inputLink.hint = "https://contoh.com/foto-produk.jpg"
        inputLink.inputType = InputType.TYPE_TEXT_VARIATION_URI
        inputLink.setSingleLine(true)
        inputLink.setPadding(40, 24, 40, 24)

        if (fotoProduk.startsWith("http://") || fotoProduk.startsWith("https://")) {
            inputLink.setText(fotoProduk)
            inputLink.setSelection(inputLink.text.length)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Masukkan Link Foto")
            .setMessage("Tempel link gambar produk di bawah ini.")
            .setView(inputLink)
            .setPositiveButton("Gunakan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val tombolGunakan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            tombolGunakan.setOnClickListener {
                val linkFoto = inputLink.text.toString().trim()

                if (linkFoto.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Link foto tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (
                    !linkFoto.startsWith("http://") &&
                    !linkFoto.startsWith("https://")
                ) {
                    Toast.makeText(
                        this,
                        "Link harus diawali http:// atau https://",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                fotoProduk = linkFoto

                // Penting: kalau pakai link, jangan upload file galeri
                selectedImageUri = null

                Glide.with(this)
                    .load(linkFoto)
                    .placeholder(ColorDrawable(Color.WHITE))
                    .error(ColorDrawable(Color.WHITE))
                    .into(ivPreviewFoto)

                Toast.makeText(
                    this,
                    "Link foto berhasil digunakan",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun uploadFotoDulu() {
        val uri = selectedImageUri ?: return

        val file = uriToFile(uri)

        if (file == null) {
            Toast.makeText(
                this,
                "Gagal membaca file gambar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        btnSimpan.isEnabled = false
        btnSimpan.text = "UPLOAD FOTO..."

        val requestFile = RequestBody.create(
            MediaType.parse("image/*"),
            file
        )

        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestFile
        )

        RetrofitClient.instance.uploadFoto(body)
            .enqueue(object : Callback<UploadResponse> {

                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    if (response.isSuccessful) {
                        fotoProduk = response.body()?.filename ?: "default.jpg"
                        prosesSimpanAtauUpdate()
                    } else {
                        resetButton()

                        Toast.makeText(
                            this@TambahProdukActivity,
                            "Upload foto gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<UploadResponse>,
                    t: Throwable
                ) {
                    resetButton()

                    Toast.makeText(
                        this@TambahProdukActivity,
                        "Error upload foto: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun prosesSimpanAtauUpdate() {
        if (mode == "edit") {
            updateProduk()
        } else {
            simpanProduk()
        }
    }

    private fun validasiInput(): ProdukRequest? {
        val nama = etNamaProduk.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val hargaText = etHarga.text.toString().trim()
        val stokText = etStok.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaProduk.error = "Nama produk wajib diisi"
            etNamaProduk.requestFocus()
            resetButton()
            return null
        }

        if (deskripsi.isEmpty()) {
            etDeskripsi.error = "Deskripsi wajib diisi"
            etDeskripsi.requestFocus()
            resetButton()
            return null
        }

        if (hargaText.isEmpty()) {
            etHarga.error = "Harga wajib diisi"
            etHarga.requestFocus()
            resetButton()
            return null
        }

        if (stokText.isEmpty()) {
            etStok.error = "Stok wajib diisi"
            etStok.requestFocus()
            resetButton()
            return null
        }

        if (idKategoriDipilih == 0) {
            Toast.makeText(
                this,
                "Pilih kategori dulu",
                Toast.LENGTH_SHORT
            ).show()

            resetButton()
            return null
        }

        return ProdukRequest(
            nama = nama,
            deskripsi = deskripsi,
            harga = hargaText.toInt(),
            stok = stokText.toInt(),
            foto = fotoProduk,
            id_kategori = idKategoriDipilih
        )
    }

    private fun simpanProduk() {
        val request = validasiInput() ?: return

        btnSimpan.isEnabled = false
        btnSimpan.text = "MENYIMPAN..."

        RetrofitClient.instance.tambahProduk(request)
            .enqueue(object : Callback<Produk> {

                override fun onResponse(
                    call: Call<Produk>,
                    response: Response<Produk>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@TambahProdukActivity,
                            "Produk berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()

                    } else {
                        resetButton()

                        Toast.makeText(
                            this@TambahProdukActivity,
                            "Gagal menambahkan produk",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<Produk>,
                    t: Throwable
                ) {
                    resetButton()

                    Toast.makeText(
                        this@TambahProdukActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateProduk() {
        val request = validasiInput() ?: return

        if (idProdukEdit == 0) {
            resetButton()

            Toast.makeText(
                this,
                "ID produk tidak ditemukan",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        btnSimpan.isEnabled = false
        btnSimpan.text = "MENGUPDATE..."

        RetrofitClient.instance.updateProduk(idProdukEdit, request)
            .enqueue(object : Callback<Produk> {

                override fun onResponse(
                    call: Call<Produk>,
                    response: Response<Produk>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@TambahProdukActivity,
                            "Produk berhasil diupdate",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()

                    } else {
                        resetButton()

                        Toast.makeText(
                            this@TambahProdukActivity,
                            "Gagal update produk",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<Produk>,
                    t: Throwable
                ) {
                    resetButton()

                    Toast.makeText(
                        this@TambahProdukActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)

            val file = File(
                cacheDir,
                "produk_${System.currentTimeMillis()}.jpg"
            )

            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file

        } catch (e: Exception) {
            null
        }
    }

    private fun resetButton() {
        btnSimpan.isEnabled = true

        btnSimpan.text = if (mode == "edit") {
            "UPDATE PRODUK"
        } else {
            "SIMPAN PRODUK"
        }
    }
}