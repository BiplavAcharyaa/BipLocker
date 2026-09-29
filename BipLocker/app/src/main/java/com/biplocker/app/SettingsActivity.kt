package com.biplocker.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class SettingsActivity : SecureActivity() {

    private lateinit var storage: LocalStorage
    private lateinit var groupProtection: LinearLayout
    private lateinit var groupSecurity: LinearLayout
    private lateinit var groupService: LinearLayout
    private lateinit var groupApp: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        storage = LocalStorage(this)
        groupProtection = findViewById(R.id.groupProtection)
        groupSecurity = findViewById(R.id.groupSecurity)
        groupService = findViewById(R.id.groupService)
        groupApp = findViewById(R.id.groupApp)

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        groupProtection.fadeIn(0L)
        groupSecurity.fadeIn(80L)
        groupService.fadeIn(160L)
        groupApp.fadeIn(240L)
    }

    override fun onResume() {
        super.onResume()
        rebuild()
    }

    private fun rebuild() {
        groupProtection.removeAllViews()
        groupSecurity.removeAllViews()
        groupService.removeAllViews()
        groupApp.removeAllViews()

        val active = ServiceStatus.isEnabled(this)
        addRow(
            groupProtection,
            R.drawable.ic_shield,
            getString(R.string.setting_accessibility),
            getString(if (active) R.string.setting_accessibility_active else R.string.setting_accessibility_inactive),
            false
        ) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        addRow(
            groupProtection,
            R.drawable.ic_apps,
            getString(R.string.setting_protected_apps),
            getString(R.string.selected_count, storage.getProtectedPackages().size),
            true
        ) { startActivity(Intent(this, AppSelectActivity::class.java)) }

        addRow(
            groupSecurity,
            R.drawable.ic_key,
            getString(R.string.change_pin),
            getString(if (storage.hasPin()) R.string.change_pin_value else R.string.set_pin_value),
            false
        ) { startActivity(Intent(this, ChangePinActivity::class.java)) }
        addRow(
            groupSecurity,
            R.drawable.ic_fingerprint,
            getString(R.string.setting_biometrics),
            getString(if (Biometrics.isEnrolled(this)) R.string.setting_biometrics_available else R.string.setting_biometrics_unavailable),
            true,
            null
        )

        addRow(
            groupService,
            R.drawable.ic_lock,
            getString(R.string.setting_unlock_method),
            getString(R.string.setting_unlock_value),
            false,
            null
        )
        addRow(
            groupService,
            R.drawable.ic_info,
            getString(R.string.setting_privacy),
            getString(R.string.setting_privacy_value),
            true,
            null
        )

        addRow(
            groupApp,
            R.drawable.ic_info,
            getString(R.string.setting_version),
            versionName(),
            false,
            null
        )
        addRow(
            groupApp,
            R.drawable.ic_shield,
            getString(R.string.setting_detection),
            getString(R.string.setting_detection_value),
            true,
            null
        )
    }

    @Suppress("DEPRECATION")
    private fun versionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private fun addRow(
        group: LinearLayout,
        iconRes: Int,
        title: String,
        value: String,
        last: Boolean,
        onClick: (() -> Unit)?
    ) {
        val row = LayoutInflater.from(this).inflate(R.layout.row_setting, group, false)
        row.findViewById<ImageView>(R.id.rowIcon).setImageResource(iconRes)
        row.findViewById<TextView>(R.id.rowTitle).text = title
        row.findViewById<TextView>(R.id.rowValue).text = value
        row.findViewById<View>(R.id.rowDivider).visibility = if (last) View.GONE else View.VISIBLE
        val content = row.findViewById<View>(R.id.rowContent)
        if (onClick != null) {
            content.setOnClickListener { onClick() }
        } else {
            row.findViewById<View>(R.id.rowChevron).visibility = View.GONE
        }
        group.addView(row)
    }
}
