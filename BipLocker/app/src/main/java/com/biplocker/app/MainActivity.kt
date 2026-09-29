package com.biplocker.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : SecureActivity() {

    private lateinit var storage: LocalStorage
    private lateinit var heroCard: View
    private lateinit var statsRow: View
    private lateinit var sectionHeader: View
    private lateinit var statusDot: View
    private lateinit var statusText: TextView
    private lateinit var countText: TextView
    private lateinit var countLabel: TextView
    private lateinit var heroHint: TextView
    private lateinit var heroButton: Button
    private lateinit var serviceValue: TextView
    private lateinit var protectedList: LinearLayout
    private lateinit var emptyState: View

    private val executor = Executors.newSingleThreadExecutor()
    private var serviceEnabled = false
    private var pinPromptShown = false

    @Volatile
    private var destroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = LocalStorage(this)
        heroCard = findViewById(R.id.heroCard)
        statsRow = findViewById(R.id.statsRow)
        sectionHeader = findViewById(R.id.sectionHeader)
        statusDot = findViewById(R.id.statusDot)
        statusText = findViewById(R.id.statusText)
        countText = findViewById(R.id.countText)
        countLabel = findViewById(R.id.countLabel)
        heroHint = findViewById(R.id.heroHint)
        heroButton = findViewById(R.id.heroButton)
        serviceValue = findViewById(R.id.serviceValue)
        protectedList = findViewById(R.id.protectedList)
        emptyState = findViewById(R.id.emptyState)

        findViewById<View>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<View>(R.id.manageLink).setOnClickListener { openSelection() }
        emptyState.setOnClickListener { openSelection() }
        heroButton.setOnClickListener {
            if (serviceEnabled) {
                openSelection()
            } else {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        heroCard.fadeIn(0L)
        statsRow.fadeIn(90L)
        sectionHeader.fadeIn(160L)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        loadProtected()
        if (!storage.hasPin() && !pinPromptShown) {
            pinPromptShown = true
            startActivity(Intent(this, ChangePinActivity::class.java))
        }
    }

    override fun onDestroy() {
        destroyed = true
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun openSelection() {
        startActivity(Intent(this, AppSelectActivity::class.java))
    }

    private fun updateStatus() {
        serviceEnabled = ServiceStatus.isEnabled(this)
        if (serviceEnabled) {
            statusDot.setBackgroundResource(R.drawable.bg_dot_on)
            statusText.setText(R.string.protection_active)
            heroHint.setText(R.string.hero_hint_active)
            heroButton.setText(R.string.manage_apps)
            serviceValue.setText(R.string.on)
            serviceValue.setTextColor(getColor(R.color.success))
        } else {
            statusDot.setBackgroundResource(R.drawable.bg_dot_off)
            statusText.setText(R.string.protection_inactive)
            heroHint.setText(R.string.hero_hint_inactive)
            heroButton.setText(R.string.enable_protection)
            serviceValue.setText(R.string.off)
            serviceValue.setTextColor(getColor(R.color.error))
        }
    }

    private fun loadProtected() {
        val selected = storage.getProtectedPackages()
        val pm = packageManager
        countText.text = selected.size.toString()
        countLabel.setText(if (selected.size == 1) R.string.app_protected_one else R.string.apps_protected)
        executor.execute {
            val result = ArrayList<AppEntry>()
            for (pkg in selected) {
                val entry = AppLookup.find(pm, pkg, true)
                if (entry != null) result.add(entry)
            }
            result.sortBy { it.label.lowercase(Locale.getDefault()) }
            if (destroyed) return@execute
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                render(result)
            }
        }
    }

    private fun render(entries: List<AppEntry>) {
        countText.text = entries.size.toString()
        countLabel.setText(if (entries.size == 1) R.string.app_protected_one else R.string.apps_protected)
        protectedList.removeAllViews()
        emptyState.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        val inflater = LayoutInflater.from(this)
        entries.forEachIndexed { index, entry ->
            val view = inflater.inflate(R.layout.item_protected, protectedList, false)
            view.findViewById<ImageView>(R.id.appIcon).setImageDrawable(entry.icon)
            view.findViewById<TextView>(R.id.appName).text = entry.label
            protectedList.addView(view)
            view.fadeIn(minOf(index, 8) * 40L)
        }
    }
}
