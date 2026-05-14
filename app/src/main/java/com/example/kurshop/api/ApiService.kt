package com.example.kurshop.api

import com.example.kurshop.model.DetailTransaksi
import com.example.kurshop.model.DetailTransaksiRequest
import com.example.kurshop.model.DetailTransaksiResponse
import com.example.kurshop.model.Kategori
import com.example.kurshop.model.KategoriRequest
import com.example.kurshop.model.LoginRequest
import com.example.kurshop.model.LoginResponse
import com.example.kurshop.model.Produk
import com.example.kurshop.model.ProdukRequest
import com.example.kurshop.model.RegisterRequest
import com.example.kurshop.model.Transaksi
import com.example.kurshop.model.TransaksiRequest
import com.example.kurshop.model.TransaksiResponse
import com.example.kurshop.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.kurshop.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {

    // =========================
    // AUTH
    // =========================
    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("users")
    fun register(
        @Body request: RegisterRequest
    ): Call<User>


    // =========================
    // USERS
    // =========================
    @GET("users")
    fun getUsers(): Call<List<User>>

    @DELETE("users/{user_id}")
    fun deleteUser(
        @Path("user_id") userId: Int
    ): Call<Void>


    // =========================
    // PRODUK
    // =========================
    @GET("produk")
    fun getProduk(): Call<List<Produk>>

    @POST("produk")
    fun tambahProduk(
        @Body request: ProdukRequest
    ): Call<Produk>

    @PUT("produk/{produk_id}")
    fun updateProduk(
        @Path("produk_id") produkId: Int,
        @Body request: ProdukRequest
    ): Call<Produk>

    @DELETE("produk/{produk_id}")
    fun deleteProduk(
        @Path("produk_id") produkId: Int
    ): Call<Void>


    // =========================
    // KATEGORI
    // =========================
    @GET("kategori")
    fun getKategori(): Call<List<Kategori>>

    @POST("kategori")
    fun tambahKategori(
        @Body request: KategoriRequest
    ): Call<Kategori>

    @DELETE("kategori/{kategori_id}")
    fun deleteKategori(
        @Path("kategori_id") kategoriId: Int
    ): Call<Void>


    // =========================
    // TRANSAKSI
    // =========================
    @GET("transaksi")
    fun getTransaksi(): Call<List<Transaksi>>

    @POST("transaksi")
    fun createTransaksi(
        @Body request: TransaksiRequest
    ): Call<TransaksiResponse>

    @PUT("transaksi/{transaksi_id}")
    fun updateTransaksi(
        @Path("transaksi_id") transaksiId: Int,
        @Body request: TransaksiRequest
    ): Call<TransaksiResponse>


    // =========================
    // DETAIL TRANSAKSI
    // =========================
    @POST("detail-transaksi")
    fun createDetailTransaksi(
        @Body request: DetailTransaksiRequest
    ): Call<DetailTransaksiResponse>


    @GET("detail-transaksi")
    fun getDetailTransaksi(): Call<List<DetailTransaksi>>

    @Multipart
    @POST("upload-foto")
    fun uploadFoto(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

}

