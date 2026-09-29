package com.biplocker.app

import android.app.Activity
import android.content.Intent
import android.view.View

object AppSession {

    @Volatile
    var unlocked = false

    private var visible = 0

    fun started() {
        visible++
    }

    fun stopped(changingConfigurations: Boolean) {
        visible = maxOf(0, visible - 1)
        if (visible == 0 && !changingConfigurations) {
            unlocked = false
        }
    }
}

open class SecureActivity : Activity() {

    override fun onStart() {
        super.onStart()
        AppSession.started()
    }

    override fun onStop() {
        super.onStop()
        AppSession.stopped(isChangingConfigurations)
    }

    override fun onResume() {
        super.onResume()
        val content = findViewById<View>(android.R.id.content)
        val needsPin = LocalStorage(this).hasPin() && !AppSession.unlocked
        content.visibility = if (needsPin) View.INVISIBLE else View.VISIBLE
        if (needsPin) {
            val intent = Intent(this, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(LockActivity.EXTRA_TARGET_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }
}
