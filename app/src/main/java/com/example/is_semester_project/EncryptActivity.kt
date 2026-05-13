package com.example.is_semester_project

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.is_semester_project.databinding.ActivityEncryptBinding
import com.example.is_semester_project.helpers.AESHelper
import com.example.is_semester_project.helpers.MalwareScanner
import com.example.is_semester_project.helpers.SHAHelper

class EncryptActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEncryptBinding
    private var fileBytes: ByteArray? = null
    private var encryptedDataForSaving: ByteArray? = null

    private val malwareScanner = MalwareScanner()
    private val aesHelper = AESHelper()

    private val saveFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(encryptedDataForSaving)
                    }
                    Toast.makeText(this, "Encrypted File Saved!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun getFileName(uri: Uri): String {
        var name = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) name = it.getString(nameIndex)
            }
        }
        return name.ifEmpty { uri.path?.substringAfterLast('/') ?: "unknown_file" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEncryptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filePicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    try {
                        val fileName = getFileName(it)
                        val mimeType = contentResolver.getType(it) // Get the REAL file type

                        // --- THE ULTIMATE BLOCK ---
                        if (malwareScanner.isFileDangerous(fileName, mimeType)) {
                            Toast.makeText(
                                this,
                                "⚠ BLOCKED: BIN/EXE files not allowed!",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.fileName.text = "FILE BLOCKED"
                            binding.tvFilePreview.text =
                                "Security Restriction: \nFile: $fileName \nType: $mimeType \n\nExecutable and Binary files cannot be encrypted."
                            fileBytes = null
                            return@let
                        }

                        binding.fileName.text = fileName
                        fileBytes = contentResolver.openInputStream(it)?.readBytes()
                        fileBytes?.let { bytes ->
                            binding.tvFilePreview.text = SHAHelper.getFilePreview(bytes)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        binding.btnSelectFile.setOnClickListener { filePicker.launch("*/*") }

        binding.btnEncrypt.setOnClickListener {
            val password = binding.etPassword.text.toString()
            if (fileBytes == null) {
                Toast.makeText(this, "File is blocked or not selected!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this, "Password too short!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val originalHash = SHAHelper.hashData(fileBytes!!)
                val (encryptedBytes, iv) = aesHelper.encryptFile(fileBytes!!, password)
                encryptedDataForSaving = encryptedBytes

                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("last_file_hash", originalHash)
                editor.putString("last_encrypted_hash", SHAHelper.hashData(encryptedBytes))
                editor.putString("last_iv", iv.joinToString(",") { it.toString() })
                editor.apply()

                Toast.makeText(this, "Encryption Successful!", Toast.LENGTH_SHORT).show()
                binding.btnDownloadEncrypted.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDownloadEncrypted.setOnClickListener {
            if (encryptedDataForSaving != null) saveFileLauncher.launch("Encrypted_File.enc")
        }
    }
}
