package com.biplocker.app

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object ServiceStatus {

    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val expected = ComponentName(context, AppMonitorService::class.java)
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            val parsed = ComponentName.unflattenFromString(splitter.next())
            if (parsed != null && parsed == expected) return true
        }
        return false
    }
}
