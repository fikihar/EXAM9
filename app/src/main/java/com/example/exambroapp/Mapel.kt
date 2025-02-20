package com.example.exambroapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mapel(
    val name: String,
    val link: String,
    val kelas: String,
    val imageResId: Int = R.drawable.logouji // Tambahkan gambar default
) : Parcelable
