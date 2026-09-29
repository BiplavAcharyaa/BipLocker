package com.biplocker.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class LocalStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProtectedPackages(): Set<String> {
        return HashSet(prefs.getStringSet(KEY_PROTECTED, emptySet()) ?: emptySet())
    }

    fun setProtectedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_PROTECTED, HashSet(packages)).commit()
    }

    fun isProtected(packageName: String): Boolean {
        return getProtectedPackages().contains(packageName)
    }

    fun getWrongAttempts(): Int = prefs.getInt(KEY_WRONG, 0)

    fun registerFailure(): Int {
        val next = getWrongAttempts() + 1
        prefs.edit().putInt(KEY_WRONG, next).commit()
        return next
    }

    fun registerSuccess() {
        prefs.edit().putInt(KEY_WRONG, 0).commit()
    }

    fun hasPin(): Boolean {
        return prefs.getString(KEY_PIN_SALT, null) != null && prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun setPin(pin: String) {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = derive(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putInt(KEY_WRONG, 0)
            .commit()
    }

    fun verifyPin(candidate: String): Boolean {
        val saltText = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val hashText = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return try {
            val salt = Base64.decode(saltText, Base64.NO_WRAP)
            val expected = Base64.decode(hashText, Base64.NO_WRAP)
            MessageDigest.isEqual(derive(candidate, salt), expected)
        } catch (e: Exception) {
            false
        }
    }

    fun setUnlockedPackage(packageName: String) {
        prefs.edit().putString(KEY_UNLOCKED_PACKAGE, packageName).commit()
    }

    fun getUnlockedPackage(): String? = prefs.getString(KEY_UNLOCKED_PACKAGE, null)

    fun clearUnlocked() {
        if (prefs.getString(KEY_UNLOCKED_PACKAGE, null) != null) {
            prefs.edit().remove(KEY_UNLOCKED_PACKAGE).commit()
        }
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val chars = pin.toCharArray()
        val spec = PBEKeySpec(chars, salt, PIN_ITERATIONS, PIN_KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    companion object {
        const val MAX_WRONG_ATTEMPTS = 3
        const val PIN_MIN_LENGTH = 4
        const val PIN_MAX_LENGTH = 8
        private const val SALT_BYTES = 16

        private const val PREFS_NAME = "biplocker_store"
        private const val KEY_PROTECTED = "protected_packages"
        private const val KEY_WRONG = "wrong_attempts"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_UNLOCKED_PACKAGE = "unlocked_package"

        private const val PIN_ITERATIONS = 120000
        private const val PIN_KEY_BITS = 256
    }
}
