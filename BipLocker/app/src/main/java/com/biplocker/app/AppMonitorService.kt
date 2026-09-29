package com.biplocker.app

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

class AppMonitorService : AccessibilityService() {

    private var storage: LocalStorage? = null
    private var receiverRegistered = false

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            storage?.clearUnlocked()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val store = LocalStorage(this)
        storage = store
        store.clearUnlocked()
        if (!receiverRegistered) {
            registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
            receiverRegistered = true
        }
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            try {
                unregisterReceiver(screenOffReceiver)
            } catch (e: Exception) {
            }
            receiverRegistered = false
        }
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString()
        if (pkg.isNullOrEmpty()) return
        if (pkg == packageName) return

        val store = storage ?: LocalStorage(this).also { storage = it }

        if (!store.hasPin()) return

        if (store.isProtected(pkg)) {
            if (store.getUnlockedPackage() == pkg) return
            showLock(pkg)
            return
        }

        if (store.getUnlockedPackage() == null) return
        if (isTransient(pkg)) return
        store.clearUnlocked()
    }

    override fun onInterrupt() {
    }

    private fun isTransient(pkg: String): Boolean {
        if (pkg == "android" || pkg == "com.android.systemui") return true
        if (pkg.endsWith(".permissioncontroller")) return true
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
        return manager.enabledInputMethodList.any { it.packageName == pkg }
    }

    private fun showLock(targetPackage: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra(LockActivity.EXTRA_TARGET_PACKAGE, targetPackage)
        }
        startActivity(intent)
    }
}
