package com.example.is_semester_project.helpers

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.Path

class AESHelper {
    private val transformation = "AES/CBC/PKCS5Padding"  //CBC -> Cipher Block Chaining

    fun encryptFile(fileData: ByteArray, key: String): Pair<ByteArray, ByteArray> {
        //Preparing the Key (32 bytes)... Padding and trimming the user password to exactly 32 bytes (256 bits)
        val keyBytes = key.padEnd(32, ' ').take(32).toByteArray()
        val secretKey = SecretKeySpec(
            keyBytes,
            "AES"
        ) //turning user password into format that math engine understands (android encryption aes understand)

        //Initializing the cipher
        val cipher = Cipher.getInstance(transformation) //getting instance
        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey
        ) //telling encryption mode and provide initial key (secret key)

        //Get the initialization vector needed for decryption
        val iv =
            cipher.iv //In CBC mode, we need a random starting block, so i save this IV + encrypted file to decrypt it later.
        //Encrypting data (file data)
        val encryptedData = cipher.doFinal(fileData)
        //Returning the Encrypted file data with initialization vector for decryption
        return Pair(encryptedData, iv)
    }

    fun decryptFile(encryptedData: ByteArray, key: String, iv: ByteArray): ByteArray {
        //Preparing the Key (32 bytes)
        val keyBytes = key.padEnd(32, ' ').take(32).toByteArray()
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(iv)

        //Initializing the cipher for decryption
        val cipher = Cipher.getInstance(transformation)
        //Initialize with Decrypt mode and use the same initialization vector
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        //Decrypting the data
        return cipher.doFinal(encryptedData)
    }
}