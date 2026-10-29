package com.example.progr3ssapp.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.example.progr3ssapp.model.AuthResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * A tokent es a bejelentkezett user adatait AES/GCM-mel titkositja.
 * A kulcs az Android Keystore-ban, a titkositott fajl a noBackupFilesDir-ben van.
 * Nem mentunk jelszot, es nem naplozunk tokent.
 */
class SecureSessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val gson = Gson()
    private val file = AtomicFile(File(appContext.noBackupFilesDir, "progr3ss_session.enc"))

    private val alias = "progr3ss_session_aes_v1"
    private val transformation = "AES/GCM/NoPadding"

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    suspend fun save(response: AuthResponse) = withContext(Dispatchers.IO) {
        require(!response.tokens?.accessToken.isNullOrBlank()) {
            "Hianyzik az access token"
        }
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val plain = gson.toJson(response).toByteArray(Charsets.UTF_8)
        val encrypted = cipher.doFinal(plain)
        val iv = cipher.iv
        val outputBytes = byteArrayOf(iv.size.toByte()) + iv + encrypted

        val output = file.startWrite()
        try {
            output.write(outputBytes)
            file.finishWrite(output)
        } catch (e: Exception) {
            file.failWrite(output)
            throw e
        }
    }

    // Kesobbi hitelesitett session-visszaallitas alapja.
    // Onmagaban a token letezese NEM bizonyitja, hogy meg ervenyes.
    suspend fun load(): AuthResponse? = withContext(Dispatchers.IO) {
        try {
            val bytes = file.readFully()
            if (bytes.isEmpty()) return@withContext null
            val ivSize = bytes[0].toInt() and 0xFF
            if (ivSize != 12 || bytes.size <= 1 + ivSize) return@withContext null
            val iv = bytes.copyOfRange(1, 1 + ivSize)
            val encrypted = bytes.copyOfRange(1 + ivSize, bytes.size)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            val plain = cipher.doFinal(encrypted)
            gson.fromJson(String(plain, Charsets.UTF_8), AuthResponse::class.java)
        } catch (e: IOException) {
            null
        } catch (e: java.security.GeneralSecurityException) {
            null
        } catch (e: com.google.gson.JsonParseException) {
            null
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        file.delete()
    }
}
