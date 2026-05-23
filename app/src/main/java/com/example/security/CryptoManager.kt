package com.example.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERACTION_COUNT = 1500
    private const val KEY_LENGTH = 256

    // Secure static salt for PBKDF2 derivation (unique to this app, offline-stable).
    private val SALT = byteArrayOf(
        0x44.toByte(), 0x65.toByte(), 0x61.toByte(), 0x72.toByte(),
        0x44.toByte(), 0x69.toByte(), 0x61.toByte(), 0x72.toByte(),
        0x79.toByte(), 0x53.toByte(), 0x65.toByte(), 0x63.toByte(),
        0x75.toByte(), 0x72.toByte(), 0x65.toByte(), 0x4B.toByte()
    )

    /**
     * Derives a cryptographically strong 256-bit AES key from a PIN string.
     */
    fun deriveKey(pin: String): SecretKeySpec {
        val spec = PBEKeySpec(pin.toCharArray(), SALT, ITERACTION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Encrypts plaintext using AES-256-CBC. Prefixes result with the random 16-byte IV.
     */
    fun encrypt(plaintext: String, keySpec: SecretKeySpec): String {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // Concatenate IV and cipher text so it's fully self-contained on decryptions
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Decrypts ciphertext that contains a 16-byte IV prefix using AES-256-CBC.
     */
    fun decrypt(ciphertext: String, keySpec: SecretKeySpec): String {
        return try {
            val combined = Base64.decode(ciphertext, Base64.NO_WRAP)
            if (combined.size < 16) {
                return "[Invalid Format]"
            }

            val iv = ByteArray(16)
            System.arraycopy(combined, 0, iv, 0, iv.size)

            val encrypted = ByteArray(combined.size - 16)
            System.arraycopy(combined, 16, encrypted, 0, encrypted.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decrypted = cipher.doFinal(encrypted)

            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "[Decryption Failure]"
        }
    }
}
