package com.example.is_semester_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.is_semester_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val name = sharedPref.getString("username", "User")
        binding.tvWelcome.text = "Welcome, $name!"

        binding.btnGoToEncrypt.setOnClickListener {
            val intent = Intent(this, EncryptActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoToDecrypt.setOnClickListener {
            val intent = Intent(this, DecryptActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoToCompare.setOnClickListener {
            val intent = Intent(this, CompareActivity::class.java)
            startActivity(intent)
        }

        binding.btnResetUser.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            val intent = Intent(this, SetPasswordActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}