package com.example.exambroapp

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.exambroapp.databinding.ActivityWebviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isScreenPinningActive = true // Status awal untuk screen pinning

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aktifkan FLAG_SECURE untuk memblokir overlay dan tangkapan layar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Aktifkan Fullscreen Mode
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Tambahkan flag agar layar tetap menyala
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Setup SharedPreferences untuk sesi pengguna
        sharedPreferences = getSharedPreferences("ExamSession", MODE_PRIVATE)

        // Inflate layout WebView
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mulai Screen Pinning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startLockTask() // Kunci aplikasi ke layar
        }

        // Ambil URL dari Intent
        val url = intent.getStringExtra("URL")
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Konfigurasi WebView
        setupWebView()

        // Mulai memuat halaman
        loadWebPage(url)
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false) // Nonaktifkan zoom jika tidak diperlukan
            cacheMode = WebSettings.LOAD_NO_CACHE // Hindari cache
            javaScriptCanOpenWindowsAutomatically = false // Blokir popup otomatis
            setSupportMultipleWindows(false) // Blokir multi-jendela
        }

        // Tambahkan WebViewClient
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                Log.d("WebViewActivity", "Memulai memuat halaman: $url")
                Toast.makeText(this@WebViewActivity, "Memulai memuat halaman.", Toast.LENGTH_SHORT).show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.d("WebViewActivity", "Halaman selesai dimuat: $url")
                Toast.makeText(this@WebViewActivity, "Halaman selesai dimuat.", Toast.LENGTH_SHORT).show()
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                Log.e("WebViewActivity", "Error memuat halaman: $description")
                Toast.makeText(this@WebViewActivity, "Gagal memuat halaman: $description", Toast.LENGTH_SHORT).show()
                finish() // Tutup aktivitas jika terjadi kesalahan
            }
        }

        // Dukungan untuk dialog dalam WebView
        binding.webView.webChromeClient = WebChromeClient()

        // Nonaktifkan hardware acceleration jika diperlukan
        binding.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun loadWebPage(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.webView.loadUrl(url)
                }

                // Tambahkan timeout untuk memuat halaman
                val timeoutHandler = Handler(Looper.getMainLooper())
                timeoutHandler.postDelayed({
                    if (binding.webView.progress < 100) {
                        Toast.makeText(
                            this@WebViewActivity,
                            "Halaman membutuhkan waktu terlalu lama untuk dimuat.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, 10000) // Timeout 10 detik

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WebViewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (isScreenPinningActive && !isInLockTaskMode()) {
            // Jika screen pinning dilepas, ubah status dan langsung keluar
            isScreenPinningActive = false
            exitApplication()
        } else if (hasFocus) {
            // Pastikan Fullscreen Mode tetap aktif saat aplikasi mendapatkan fokus kembali
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    private fun isInLockTaskMode(): Boolean {
        // Periksa apakah aplikasi masih dalam mode screen pinning
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            false
        }
    }

    private fun exitApplication() {
        Toast.makeText(this, "Screen pinning dilepas. Aplikasi akan ditutup.", Toast.LENGTH_SHORT).show()

        // Reset data sesi di SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Keluar dari aplikasi sepenuhnya
        finishAffinity() // Tutup semua aktivitas
        System.exit(0) // Hentikan proses aplikasi
    }

    override fun onBackPressed() {
        // Tampilkan dialog konfirmasi sebelum keluar
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari ujian?")
            .setPositiveButton("Ya") { _, _ ->
                // Langsung keluar dari aplikasi
                exitApplication()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
