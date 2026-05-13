package com.example.is_semester_project.helpers

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class DESHelper {
    private val transformation = "DES/ECB/PKCS5Padding"

    fun encryptDES(fileData: ByteArray, key: String): ByteArray {
        //DES key must be exactly 8 bytes (64 bits)
        val keyBytes = key.padEnd(8, ' ').take(8).toByteArray()
        val secretKey = SecretKeySpec(keyBytes, "DES")

        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        return cipher.doFinal(fileData)
    }

    fun decryptDES(encryptedData: ByteArray, key: String): ByteArray {
        val keyBytes = key.padEnd(8, ' ').take(8).toByteArray()
        val secretKey = SecretKeySpec(keyBytes, "DES")

        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        return cipher.doFinal(encryptedData)
    }
}