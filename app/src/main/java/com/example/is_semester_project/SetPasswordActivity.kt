package com.example.is_semester_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.is_semester_project.databinding.ActivitySetPasswordBinding

class SetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val pass = binding.etPassword.text.toString()
            val confrim_pass = binding.etConfirmPassword.text.toString()

            if (name.isEmpty() || pass.isEmpty() || confrim_pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (pass != confrim_pass) {
                Toast.makeText(
                    this,
                    "Password and Confirm Password do not match",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (pass.length < 8) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 character long",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("username", name)
                editor.putString("password", pass)
                editor.apply()
                Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}