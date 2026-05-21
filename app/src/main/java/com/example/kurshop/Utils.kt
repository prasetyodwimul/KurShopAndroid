package com.example.kurshop

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(value: Int): String {
    val formatter = NumberFormat.getInstance(Locale("in", "ID"))
    return formatter.format(value)
}