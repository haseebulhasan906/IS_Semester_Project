package com.example.is_semester_project

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.is_semester_project.databinding.ActivityDecryptBinding
import com.example.is_semester_project.helpers.AESHelper
import com.example.is_semester_project.helpers.SHAHelper

class DecryptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDecryptBinding
    private var encryptedFileBytes: ByteArray? = null
    private var decryptedFileBytes: ByteArray? = null

    private val aesHelper = AESHelper()

    // Professional way to save files in Android 10+
    private val saveFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(decryptedFileBytes)
                    }
                    Toast.makeText(this, "File Saved Successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecryptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. File Picker for Encrypted File
        val filePicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    binding.fileName.text = it.path?.substringAfterLast('/') ?: "Encrypted File"

                    // --- RESET PREVIOUS RESULTS ---
                    binding.tvFilePreview.text = "Preview will appear here..."
                    binding.tvFilePreview.setTextColor(android.graphics.Color.BLACK)
                    binding.btnDownloadDecrypted.visibility = View.GONE
                    decryptedFileBytes = null

                    try {
                        encryptedFileBytes = contentResolver.openInputStream(it)?.readBytes()

                        // Show encrypted preview
                        encryptedFileBytes?.let { bytes ->
                            binding.tvFilePreview.text = SHAHelper.getFilePreview(bytes)
                        }
                        Toast.makeText(this, "Encrypted file loaded!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error loading file", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        binding.btnSelectFile.setOnClickListener {
            filePicker.launch("*/*")
        }

        // 2. Decrypt Button Logic
        binding.btnDecrypt.setOnClickListener {
            val password = binding.etPassword.text.toString()
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

            val ivString = sharedPref.getString("last_iv", null)
            val originalHash = sharedPref.getString("last_file_hash", null)
            val savedEncryptedHash = sharedPref.getString("last_encrypted_hash", null)

            if (encryptedFileBytes == null || ivString == null || originalHash == null || savedEncryptedHash == null) {
                Toast.makeText(this, "Missing file or encryption metadata!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // --- STEP 1: TAMPER DETECTION (Before Decryption) ---
            val currentFileHash = SHAHelper.hashData(encryptedFileBytes!!)
            if (currentFileHash != savedEncryptedHash) {
                binding.tvFilePreview.text =
                    "⚠ TAMPERING DETECTED!\nThe file has been modified after encryption."
                binding.tvFilePreview.setTextColor(android.graphics.Color.RED)
                Toast.makeText(this, "Integrity Check Failed!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // --- STEP 2: DECRYPTION ---
            try {
                val iv = ivString.split(",").map { it.toByte() }.toByteArray()
                decryptedFileBytes = aesHelper.decryptFile(encryptedFileBytes!!, password, iv)

                // Double check original integrity after decryption
                val decryptedHash = SHAHelper.hashData(decryptedFileBytes!!)

                if (decryptedHash == originalHash) {
                    binding.tvFilePreview.text = "✅ SUCCESS: File is Authentic!\n\n" +
                            SHAHelper.getFilePreview(decryptedFileBytes!!)
                    binding.tvFilePreview.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                    binding.btnDownloadDecrypted.visibility = View.VISIBLE
                } else {
                    binding.tvFilePreview.text = "⚠ WARNING: Integrity Check Mismatch!"
                    binding.tvFilePreview.setTextColor(android.graphics.Color.RED)
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Decryption Failed! Wrong Password?", Toast.LENGTH_SHORT)
                    .show()
                binding.tvFilePreview.text = "Error: Decryption Failed. Check password."
                binding.tvFilePreview.setTextColor(android.graphics.Color.RED)
            }
        }

        // 3. Download Button (Triggers Save Launcher)
        binding.btnDownloadDecrypted.setOnClickListener {
            if (decryptedFileBytes != null) {
                saveFileLauncher.launch("Decrypted_File_${System.currentTimeMillis()}.txt")
            }
        }
    }
}
