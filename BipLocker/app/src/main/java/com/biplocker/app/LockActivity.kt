package com.biplocker.app

import android.app.Activity
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.View
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class LockActivity : Activity() {

    private lateinit var storage: LocalStorage
    private lateinit var promptLabel: TextView
    private lateinit var pinDisplay: TextView
    private lateinit var messageText: TextView
    private lateinit var unlockButton: Button
    private lateinit var biometricButton: Button
    private lateinit var orLabel: TextView
    private lateinit var pinPad: PinPad
    private lateinit var appIcon: ImageView
    private lateinit var appNameText: TextView
    private lateinit var lockSubtitle: TextView
    private lateinit var heroCard: View
    private lateinit var heroIcon: ImageView
    private lateinit var lockCard: View

    private val entered = StringBuilder()
    private var targetPackage: String = ""
    private var unlocked = false
    private var biometricRunning = false
    private var autoPrompted = false
    private var biometricSignal: CancellationSignal? = null
    private var backCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        storage = LocalStorage(this)
        promptLabel = findViewById(R.id.promptLabel)
        pinDisplay = findViewById(R.id.pinDisplay)
        messageText = findViewById(R.id.messageText)
        unlockButton = findViewById(R.id.unlockButton)
        biometricButton = findViewById(R.id.biometricButton)
        orLabel = findViewById(R.id.orLabel)
        pinPad = findViewById(R.id.pinPad)
        appIcon = findViewById(R.id.appIcon)
        appNameText = findViewById(R.id.appNameText)
        lockSubtitle = findViewById(R.id.lockSubtitle)
        heroCard = findViewById(R.id.heroCard)
        heroIcon = findViewById(R.id.heroIcon)
        lockCard = findViewById(R.id.lockCard)

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""

        pinPad.onDigit = { digit -> appendDigit(digit) }
        pinPad.onBackspace = { removeDigit() }
        pinPad.onSubmit = { submit() }
        unlockButton.setOnClickListener { submit() }
        biometricButton.setOnClickListener { startBiometric(true) }

        registerBackCallback()
        bindTarget()
        render()
        heroCard.fadeIn(0L)
        lockCard.fadeIn(90L)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val incoming = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: targetPackage
        val changed = incoming != targetPackage
        if (unlocked && !changed) return
        if (changed || unlocked) {
            targetPackage = incoming
            unlocked = false
            autoPrompted = false
            cancelBiometric()
            entered.setLength(0)
            bindTarget()
        }
        render()
    }

    override fun onResume() {
        super.onResume()
        if (unlocked) return
        render()
        if (!autoPrompted && Biometrics.isEnrolled(this)) {
            autoPrompted = true
            startBiometric(false)
        } else if (isBiometricRequired()) {
            startBiometric(false)
        }
    }

    override fun onDestroy() {
        cancelBiometric()
        unregisterBackCallback()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goHome()
    }

    private fun registerBackCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = OnBackInvokedCallback { goHome() }
            backCallback = callback
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
        }
    }

    private fun unregisterBackCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = backCallback
            if (callback != null) {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
                backCallback = null
            }
        }
    }

    private fun isBiometricRequired(): Boolean {
        return storage.getWrongAttempts() >= LocalStorage.MAX_WRONG_ATTEMPTS && Biometrics.isEnrolled(this)
    }

    private fun render() {
        val biometricAvailable = Biometrics.hasHardware(this)
        val required = isBiometricRequired()
        pinDisplay.text = pinDots(entered.length, getString(R.string.pin_placeholder))
        biometricButton.visibility = if (biometricAvailable) View.VISIBLE else View.GONE
        orLabel.visibility = if (biometricAvailable) View.VISIBLE else View.GONE
        pinPad.isEnabled = !required
        unlockButton.isEnabled = !required
        unlockButton.alpha = if (required) 0.4f else 1f
        if (required) {
            promptLabel.setText(R.string.too_many_attempts)
            lockSubtitle.setText(R.string.biometric_required_subtitle)
            heroCard.setBackgroundResource(R.drawable.bg_hero_locked)
            heroIcon.setImageResource(R.drawable.ic_key)
            messageText.setText(R.string.biometric_required_message)
        } else {
            promptLabel.setText(R.string.enter_pin)
            lockSubtitle.setText(R.string.lock_subtitle)
            heroCard.setBackgroundResource(R.drawable.bg_hero)
            heroIcon.setImageResource(R.drawable.ic_shield)
            if (storage.getWrongAttempts() >= LocalStorage.MAX_WRONG_ATTEMPTS) {
                messageText.setText(R.string.too_many_attempts_pin)
            } else if (messageText.text.toString() == getString(R.string.biometric_required_message)) {
                messageText.text = ""
            }
        }
    }

    private fun bindTarget() {
        val entry = if (targetPackage.isEmpty()) null else AppLookup.find(packageManager, targetPackage, true)
        if (entry != null) {
            appIcon.setImageDrawable(entry.icon)
            appNameText.text = entry.label
        } else {
            appIcon.setImageResource(R.drawable.ic_shield)
            appNameText.setText(R.string.protected_app_fallback)
        }
    }

    private fun appendDigit(digit: Char) {
        if (unlocked || isBiometricRequired()) return
        if (entered.length >= LocalStorage.PIN_MAX_LENGTH) return
        entered.append(digit)
        if (storage.getWrongAttempts() < LocalStorage.MAX_WRONG_ATTEMPTS) messageText.text = ""
        pinDisplay.text = pinDots(entered.length, getString(R.string.pin_placeholder))
    }

    private fun removeDigit() {
        if (unlocked || isBiometricRequired()) return
        if (entered.isNotEmpty()) entered.setLength(entered.length - 1)
        pinDisplay.text = pinDots(entered.length, getString(R.string.pin_placeholder))
    }

    private fun submit() {
        if (unlocked || isBiometricRequired()) return
        if (entered.length < LocalStorage.PIN_MIN_LENGTH) {
            messageText.text = getString(R.string.pin_too_short, LocalStorage.PIN_MIN_LENGTH)
            lockCard.shake()
            return
        }
        val candidate = entered.toString()
        entered.setLength(0)
        if (storage.verifyPin(candidate)) {
            storage.registerSuccess()
            finishUnlocked()
            return
        }
        val attempts = storage.registerFailure()
        lockCard.shake()
        if (attempts >= LocalStorage.MAX_WRONG_ATTEMPTS) {
            render()
            heroCard.fadeIn(0L)
            if (isBiometricRequired()) startBiometric(false)
        } else {
            messageText.text = getString(R.string.incorrect_pin)
            pinDisplay.text = pinDots(0, getString(R.string.pin_placeholder))
        }
    }

    private fun startBiometric(manual: Boolean) {
        if (unlocked || biometricRunning) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        if (!Biometrics.isEnrolled(this)) {
            if (manual) messageText.setText(R.string.biometric_not_enrolled)
            return
        }
        val signal = CancellationSignal()
        biometricSignal = signal
        biometricRunning = true
        try {
            Biometrics.authenticate(
                this,
                getString(R.string.app_name),
                getString(R.string.biometric_subtitle),
                getString(R.string.cancel),
                signal,
                {
                    if (biometricSignal === signal) biometricRunning = false
                    if (!unlocked) {
                        storage.registerSuccess()
                        finishUnlocked()
                    }
                },
                { code, message ->
                    if (biometricSignal === signal) biometricRunning = false
                    if (code == BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT ||
                        code == BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT
                    ) {
                        messageText.setText(R.string.biometric_locked_out)
                    } else if (code != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED &&
                        code != BiometricPrompt.BIOMETRIC_ERROR_CANCELED &&
                        message.isNotEmpty()
                    ) {
                        messageText.text = message
                    }
                }
            )
        } catch (e: Exception) {
            biometricRunning = false
        }
    }

    private fun cancelBiometric() {
        val signal = biometricSignal
        biometricSignal = null
        biometricRunning = false
        try {
            signal?.cancel()
        } catch (e: Exception) {
        }
    }

    private fun finishUnlocked() {
        unlocked = true
        cancelBiometric()
        if (targetPackage == packageName) {
            AppSession.unlocked = true
        } else if (targetPackage.isNotEmpty()) {
            storage.setUnlockedPackage(targetPackage)
        }
        finish()
    }

    private fun goHome() {
        cancelBiometric()
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(home)
        finish()
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
    }
}
