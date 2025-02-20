package com.example.exambroapp

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.exambroapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout menggunakan View Binding sebelum mode fullscreen
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aktifkan mode fullscreen
        enableFullscreenMode()

        // Matikan Bluetooth jika diperlukan
        disableBluetooth()

        // Tambahkan animasi fade-in
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.logoImageView.startAnimation(fadeIn)
        binding.welcomeTextView.startAnimation(fadeIn)
        binding.taglineTextView.startAnimation(fadeIn)

        // Tunda selama 2 detik sebelum pindah ke MainActivity menggunakan Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000) // Tunda selama 2 detik
            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                @Suppress("DEPRECATION")
                overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                ) // Animasi transisi
                finish()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun disableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            bluetoothAdapter.disable()
            println("Bluetooth dinonaktifkan oleh aplikasi.")
        }
    }

    private fun enableFullscreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ (Android 11 ke atas)
            window?.decorView?.post {
                window.insetsController?.let { controller ->
                    controller.hide(android.view.WindowInsets.Type.systemBars())
                    controller.systemBarsBehavior =
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } ?: run {
                    println("InsetsController tidak tersedia.")
                }
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
}
