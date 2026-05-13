package com.example.is_semester_project.helpers

import java.security.MessageDigest

object SHAHelper {
    fun hashData(fileData: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-512") //taking instance of SHA-512
        val digest = md.digest(fileData) //converting input to hash (64 byte array)
        return digest.joinToString("") { "%02x".format(it) }
        //convert bytes to hexadecimal
        //% = format specifier,  02 -> convert single digint to double like 9 to 09
        // x tells hexadecimal format,  it represnt every byte of the string
    }

    fun getFilePreview(fileData: ByteArray): String {
        return if (fileData.size > 100) {
            String(fileData.take(100).toByteArray()) + " ..."
        } else {
            String(fileData)
        }
    }
}