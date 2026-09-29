package com.biplocker.app

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal

object Biometrics {

    private const val STATUS_SUCCESS = 0
    private const val STATUS_NO_HARDWARE = 12
    private const val STATUS_NONE_ENROLLED = 11

    @Suppress("DEPRECATION")
    private fun status(context: Context): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val manager = context.getSystemService(BiometricManager::class.java)
                    ?: return STATUS_NO_HARDWARE
                manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val manager = context.getSystemService(BiometricManager::class.java)
                    ?: return STATUS_NO_HARDWARE
                manager.canAuthenticate()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val manager = context.getSystemService(FingerprintManager::class.java)
                when {
                    manager == null || !manager.isHardwareDetected -> STATUS_NO_HARDWARE
                    manager.hasEnrolledFingerprints() -> STATUS_SUCCESS
                    else -> STATUS_NONE_ENROLLED
                }
            }
            else -> STATUS_NO_HARDWARE
        }
    }

    fun hasHardware(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        if (status(context) != STATUS_NO_HARDWARE) return true
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (pm.hasSystemFeature(PackageManager.FEATURE_FACE)) return true
            if (pm.hasSystemFeature(PackageManager.FEATURE_IRIS)) return true
        }
        return false
    }

    fun isEnrolled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return status(context) == STATUS_SUCCESS
    }

    fun authenticate(
        context: Context,
        title: String,
        subtitle: String,
        negative: String,
        signal: CancellationSignal,
        onSuccess: () -> Unit,
        onFailure: (Int, String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            onFailure(-1, "")
            return
        }
        val executor = context.mainExecutor
        val builder = BiometricPrompt.Builder(context)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButton(negative, executor) { _, _ ->
                onFailure(BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED, "")
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        }
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                onFailure(errorCode, errString?.toString() ?: "")
            }
        }
        builder.build().authenticate(signal, executor, callback)
    }
}
