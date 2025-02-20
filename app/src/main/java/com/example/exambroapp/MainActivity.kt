package com.example.exambroapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.exambroapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val googleSheetHelper = GoogleSheetHelper()
    private val spreadsheetId = "16MqYVfyKWbMaG2WpBMR8STaxOjR_Fjr71e4jR3s6D10" // ID Spreadsheet Anda

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Atur tampilan fullscreen
        enableFullscreenMode()

        // Tombol login
        binding.loginButton.setOnClickListener {
            val kodeExam = binding.kodeExamEditText.text.toString().trim()

            if (kodeExam.isEmpty()) {
                Toast.makeText(this, "Kode Exam tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (kodeExam.length < 4) {
                Toast.makeText(this, "Kode Exam minimal harus 4 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            hideKeyboard() // Sembunyikan keyboard sebelum memproses data

            // Tampilkan ProgressBar dan nonaktifkan tombol
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false

            // Ambil data dari Google Sheets
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val mapelList = googleSheetHelper.getMapelByKodeExam(spreadsheetId, kodeExam)
                    withContext(Dispatchers.Main) {
                        if (mapelList.isNotEmpty()) {
                            val intent = Intent(this@MainActivity, DashboardActivity::class.java).apply {
                                putParcelableArrayListExtra("MAPEL_LIST", ArrayList(mapelList))
                            }
                            startActivity(intent)
                            @Suppress("DEPRECATION")
                            overridePendingTransition(
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right
                            ) // Animasi transisi
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Kode Exam Salah atau tidak ditemukan!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Terjadi kesalahan: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } finally {
                    // Sembunyikan ProgressBar dan aktifkan tombol kembali
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.loginButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun enableFullscreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ (Android 11 ke atas)
            window.insetsController?.apply {
                hide(android.view.WindowInsets.Type.systemBars())
                systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API 29 dan sebelumnya
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }


    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        // Tampilkan dialog untuk konfirmasi keluar
        AlertDialog.Builder(this).apply {
            setTitle("Keluar Aplikasi")
            setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            setPositiveButton("Ya") { _, _ ->
                finishAffinity() // Tutup semua aktivitas
            }
            setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

}
