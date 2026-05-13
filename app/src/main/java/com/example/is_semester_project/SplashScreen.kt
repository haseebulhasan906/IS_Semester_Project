package com.example.is_semester_project

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.is_semester_project.databinding.ActivitySplashScreenBinding
import java.util.logging.Handler
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isPasswordSet = sharedPref.getString("password", null) != null

        lifecycleScope.launch {
            delay(2000)
            val intent = if (isPasswordSet) {
                Intent(this@SplashScreen, MainActivity::class.java)
            } else {
                Intent(this@SplashScreen, SetPasswordActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}