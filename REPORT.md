% Secure File Encryption & Integrity — Project Report

Author: Generated

Date: 2026-05-13

Project path: C:\Users\Muhammad Haseeb\AndroidStudioProjects\IS_Semester_Project

---

Table of contents
-----------------

- Executive summary
- Project purpose and threat model
- Files referenced
- Detailed explanation of `AESHelper.kt`
- Kotlin syntax and idioms used
- How AES (CBC/PKCS5Padding) works
- IVs and key handling (current vs recommended)
- Security weaknesses and remediation
- Alternatives (PBKDF2, AES-GCM, Android Keystore)
- DES/3DES note
- SHA vs HMAC and tamper detection
- Why `object` vs `class` for helpers
- Example usage flows and code snippets
- Best-practices checklist
- Conversion to Word (.docx)
- Appendix: code examples

---

Executive summary
-----------------
This project provides simple file encryption and decryption helpers for an Android/Kotlin app. The primary helper in the repository is `AESHelper.kt`. The code encrypts file bytes with AES in CBC mode with PKCS#5 padding, returning the ciphertext and IV. It also decrypts given ciphertext with the provided key and IV.

This document explains how the code works, what cryptographic choices it makes, security implications, and recommended fixes and alternatives (PBKDF2, AES-GCM, HMAC, Android Keystore). It also documents how to detect file tampering and why utility functions like SHA hashing are often packaged as Kotlin `object` singletons.

Project purpose and threat model
--------------------------------

Purpose
- Provide a way to encrypt files on-device and later decrypt them using a user-supplied password/key.
- Provide file integrity checks (hashes) so the app can detect accidental or malicious tampering.

Assets to protect
- File contents stored on device (user data) and associated metadata (IV, salt, hash/MAC).

Threat model (simple)
- Adversary may obtain file ciphertext stored on device/storage but does not know the user password.
- Adversary may modify ciphertext or metadata (IV, stored hash) — we need tamper detection.
- The device may be lost or stolen; attacker may try offline brute-force attacks against weak password-derived keys.

Assumptions
- The app runs on Android; Android Keystore is available and recommended where possible.

Files referenced
----------------

- `app/src/main/java/com/example/is_semester_project/helpers/AESHelper.kt` — encrypt/decrypt helper (primary focus)
- `SHAHelper.kt` (if present) — helper to compute SHA-based hashes for integrity checks
- Activities that call these helpers: EncryptActivity.kt, DecryptActivity.kt (not included here)

Detailed explanation of `AESHelper.kt`
-------------------------------------

Code excerpt (paraphrased)

```kotlin
package com.example.is_semester_project.helpers

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESHelper {
	private val transformation = "AES/CBC/PKCS5Padding"

	fun encryptFile(fileData: ByteArray, key: String): Pair<ByteArray, ByteArray> { ... }

	fun decryptFile(encryptedData: ByteArray, key: String, iv: ByteArray): ByteArray { ... }
}
```

Key points
- `transformation` = "AES/CBC/PKCS5Padding" — AES block cipher, CBC mode, PKCS#5 padding.
- Key prep uses `key.padEnd(32,' ').take(32).toByteArray()` which pads/trims to 32 bytes (AES-256). This is not a secure KDF.
- Provider-generated IV is obtained via `cipher.iv` after init when encrypting (good), must be stored with ciphertext.

Kotlin syntax & idioms used
--------------------------

- `class AESHelper { ... }` — regular instantiable class.
- `private val transformation` — immutable property.
- `fun encryptFile(...): Pair<ByteArray, ByteArray>` — returns a `Pair` (ciphertext, iv) that callers can destructure.
- `padEnd`, `take`, `toByteArray(Charsets.UTF_8)` — build key bytes (be explicit about charset in production).

How AES (CBC/PKCS5Padding) works (summary)
-----------------------------------------

- AES: 128-bit block, keys 128/192/256 bits.
- CBC: each plaintext block XORed with previous ciphertext block (or IV) before encryption; requires IV and padding.
- PKCS#5/PKCS#7 padding ensures block alignment and is removed on decryption.

IVs and key handling — current behavior vs recommended
-----------------------------------------------------

Current behavior
- Cipher provider generates an IV during `cipher.init` and code uses `cipher.iv`.
- Key derived by padding/trimming password string to 32 bytes.

Recommended
- Use PBKDF2WithHmacSHA256 or Argon2 with salt and iterations to derive AES key from password.
- Prefer AES-GCM (AEAD) or Encrypt-then-MAC pattern.
- Use SecureRandom for salts and IVs, and store salts/IVs alongside ciphertext.

Security weaknesses and remediation
----------------------------------

Summary table: key weaknesses and fixes

| Weakness | Why it's a problem | Fix / Recommendation |
|---|---|---|
| Naive key derivation (pad/trim) | Low entropy, vulnerable to brute-force | Use PBKDF2WithHmacSHA256 or Argon2 with salt and high iterations |
| CBC without authentication | No integrity/authenticity, padding oracle risk | Use AES-GCM or AES-CBC + HMAC (Encrypt-then-MAC) |
| Storing sensitive metadata in plaintext | Metadata can be modified or leaked | Protect metadata, use Keystore for keys; store salts/IVs with ciphertext but protect MAC/keys |

Alternatives and platform features
---------------------------------

- PBKDF2 example: `PBKDF2WithHmacSHA256` with a 16-byte salt and >=100_000 iterations.
- AES-GCM: `AES/GCM/NoPadding`, 12-byte nonce, 128-bit tag; provides confidentiality + integrity.
- Android Keystore: generate and store keys securely, bind to user authentication when needed.

DES/3DES note
--------------

- DES (56-bit key) is insecure and deprecated.
- 3DES is legacy and slow; prefer AES for new designs.

SHA vs HMAC and tamper detection
--------------------------------

- SHA-256: cryptographic hash for fingerprinting; anyone can recompute — does not provide authenticity.
- HMAC-SHA256: keyed MAC providing authenticity/integrity if MAC key is secret.
- AEAD (AES-GCM) gives built-in tamper detection via authentication tag.

Why `object` vs `class` for helpers
-----------------------------------

- `object` = singleton, convenient for stateless utilities and simpler call sites: `SHAHelper.sha256(bytes)`.
- `class` = use when state, dependency injection, or testability is required.

Example usage flows and code snippets
------------------------------------

PBKDF2 key derivation (Kotlin)

```kotlin
val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
val tmp = factory.generateSecret(spec)
val secretKey = SecretKeySpec(tmp.encoded, "AES")
```

AES-GCM encryption skeleton

```kotlin
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
val iv = ByteArray(12)
SecureRandom().nextBytes(iv)
val gcmSpec = GCMParameterSpec(128, iv)
cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
val cipherText = cipher.doFinal(plainBytes)
```

SHA-256 digest (Kotlin)

```kotlin
val md = MessageDigest.getInstance("SHA-256")
val digest = md.digest(fileBytes)
```

Best-practices checklist
------------------------

- Use a KDF (PBKDF2/Argon2) with salt and adequate iterations.
- Prefer AEAD (AES-GCM) or Encrypt-then-MAC.
- Use SecureRandom for salts and IVs; do not reuse nonces.
- Store keys securely (Android Keystore) and avoid storing raw passwords.
- Use constant-time comparison for MAC validation.

Converting this report to Word (.docx)
-------------------------------------

I will attempt an automatic conversion to DOCX now using pandoc. If `pandoc` is not installed on your machine, the command will fail and I'll provide a clear PowerShell command to install pandoc and re-run conversion.

Conversion command I will run (PowerShell):

```powershell
cd "C:\Users\Muhammad Haseeb\AndroidStudioProjects\IS_Semester_Project";
pandoc -s REPORT.md -o REPORT.docx --toc
```

Appendix: code examples
-----------------------

`SHAHelper` as Kotlin `object` example:

```kotlin
object SHAHelper {
	fun sha256(bytes: ByteArray): ByteArray {
		val md = MessageDigest.getInstance("SHA-256")
		return md.digest(bytes)
	}

	fun sha256Hex(bytes: ByteArray): String = sha256(bytes).joinToString("") { "%02x".format(it) }
}
```

PBKDF2 helper example:

```kotlin
fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int = 100_000): SecretKeySpec {
	val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
	val spec = PBEKeySpec(password, salt, iterations, 256)
	val tmp = factory.generateSecret(spec)
	return SecretKeySpec(tmp.encoded, "AES")
}
```

Final notes
-----------

- The current `AESHelper` is functional but needs hardening for production use: replace naive key derivation, prefer AEAD (AES-GCM) or HMAC, and protect keys in Keystore.

---

End of report.

