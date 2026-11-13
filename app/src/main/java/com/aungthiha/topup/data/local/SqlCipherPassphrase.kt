package com.aungthiha.topup.data.local

import android.content.Context
import android.util.Base64
import java.security.SecureRandom

/**
 * Provides a stable per-install passphrase for whole-database SQLCipher encryption.
 * Generated once and stored in private app prefs (sufficient for this testing app).
 */
internal object SqlCipherPassphrase {
    private const val PREFS_NAME = "topup_sqlcipher"
    private const val KEY_PASSPHRASE = "db_passphrase"
    private const val PASSPHRASE_BYTES = 32

    fun getOrCreate(context: Context): ByteArray {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_PASSPHRASE, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }
        val generated = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(generated, Base64.NO_WRAP))
            .apply()
        return generated
    }
}
