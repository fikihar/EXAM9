package com.example.exambroapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exambroapp.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data mapel dari intent
        val mapelList = intent.getParcelableArrayListExtra<Mapel>("MAPEL_LIST") ?: arrayListOf()

        if (mapelList.isEmpty()) {
            Toast.makeText(this, "Tidak ada mapel untuk ditampilkan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView dan Adapter
        val adapter = MapelAdapter(this, mapelList) { link ->
            openWebView(link)
        }

        binding.mapelRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity).apply {
                isItemPrefetchEnabled = true // Optimasi prefetching
            }
            setHasFixedSize(true)
            setItemViewCacheSize(20) // Cache 20 item
            addItemDecoration(DividerItemDecoration(this@DashboardActivity, DividerItemDecoration.VERTICAL))
            this.adapter = adapter
        }
    }

    private fun openWebView(link: String) {
        if (link.isBlank()) {
            Toast.makeText(this, "Tautan tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra("URL", link)
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Konfirmasi Keluar")
            setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            setPositiveButton("Ya") { _, _ ->
                finishAffinity() // Keluar dari seluruh aplikasi
            }
            setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(true) // Izinkan dialog untuk ditutup dengan klik di luar
            create()
            show()
        }
    }
}

