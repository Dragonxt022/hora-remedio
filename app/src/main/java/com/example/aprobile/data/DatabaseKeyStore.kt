package com.example.aprobile.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gera e protege a senha (passphrase) do banco SQLCipher.
 *
 * A passphrase aleatória de 256 bits é cifrada com uma chave AES do Android Keystore
 * (que nunca sai do cofre) e guardada cifrada nas SharedPreferences.
 */
object DatabaseKeyStore {

    private const val KEY_ALIAS = "aprobile_db_key_v1"
    private const val PREFS = "aprobile_secure_prefs"
    private const val PREF_CIPHERTEXT = "db_key_cipher"
    private const val PREF_IV = "db_key_iv"
    private const val GCM_TAG_BITS = 128

    /** Retorna a passphrase do banco como string hexadecimal (ASCII), usada por SQLCipher. */
    fun getOrCreatePassphrase(context: Context): String {
        return getOrCreateKeyBytes(context).joinToString("") { "%02x".format(it) }
    }

    private fun getOrCreateKeyBytes(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedCipher = prefs.getString(PREF_CIPHERTEXT, null)
        val storedIv = prefs.getString(PREF_IV, null)

        if (storedCipher != null && storedIv != null) {
            try {
                val key = getOrCreateKeystoreKey()
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    GCMParameterSpec(GCM_TAG_BITS, Base64.decode(storedIv, Base64.NO_WRAP))
                )
                return cipher.doFinal(Base64.decode(storedCipher, Base64.NO_WRAP))
            } catch (e: Exception) {
                // Se a chave foi invalidada, recria (perda do banco antigo é aceitável nesse cenário)
                e.printStackTrace()
            }
        }

        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        try {
            val key = getOrCreateKeystoreKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val ciphertext = cipher.doFinal(passphrase)
            prefs.edit()
                .putString(PREF_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .putString(PREF_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return passphrase
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
