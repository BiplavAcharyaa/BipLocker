package com.biplocker.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.util.concurrent.Executors

class AppSelectActivity : SecureActivity() {

    private lateinit var storage: LocalStorage
    private lateinit var listView: ListView
    private lateinit var progress: ProgressBar
    private lateinit var selectedCount: TextView
    private lateinit var adapter: AppAdapter

    private val entries = ArrayList<AppEntry>()
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var destroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_select)

        storage = LocalStorage(this)
        listView = findViewById(R.id.appList)
        progress = findViewById(R.id.progress)
        selectedCount = findViewById(R.id.selectedCount)
        val saveButton: Button = findViewById(R.id.saveButton)

        adapter = AppAdapter()
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = entries[position]
            entry.enabled = !entry.enabled
            adapter.notifyDataSetChanged()
            updateCount()
        }

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }
        saveButton.setOnClickListener { save() }

        updateCount()
        loadApps()
    }

    override fun onDestroy() {
        destroyed = true
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun updateCount() {
        val count = if (entries.isEmpty()) storage.getProtectedPackages().size else entries.count { it.enabled }
        selectedCount.text = getString(R.string.selected_count, count)
    }

    private fun loadApps() {
        progress.visibility = View.VISIBLE
        val selected = storage.getProtectedPackages()
        val pm = packageManager
        executor.execute {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolved = pm.queryIntentActivities(launcherIntent, 0)
            val seen = HashSet<String>()
            val result = ArrayList<AppEntry>()
            for (info in resolved) {
                val pkg = info.activityInfo.packageName
                if (pkg == packageName) continue
                if (!seen.add(pkg)) continue
                val label = info.loadLabel(pm)?.toString() ?: pkg
                val icon = info.loadIcon(pm)
                result.add(AppEntry(pkg, label, icon, selected.contains(pkg)))
            }
            result.sortWith(compareByDescending<AppEntry> { it.enabled }
                .thenBy { it.label.lowercase(Locale.getDefault()) })
            if (destroyed) return@execute
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                entries.clear()
                entries.addAll(result)
                adapter.notifyDataSetChanged()
                progress.visibility = View.GONE
                updateCount()
            }
        }
    }

    private fun save() {
        val chosen = HashSet<String>()
        for (entry in entries) {
            if (entry.enabled) chosen.add(entry.packageName)
        }
        storage.setProtectedPackages(chosen)
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private inner class AppAdapter : BaseAdapter() {

        override fun getCount(): Int = entries.size

        override fun getItem(position: Int): Any = entries[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@AppSelectActivity)
                .inflate(R.layout.item_app, parent, false)
            val entry = entries[position]
            view.findViewById<ImageView>(R.id.appIcon).setImageDrawable(entry.icon)
            view.findViewById<TextView>(R.id.appName).text = entry.label
            view.findViewById<Switch>(R.id.appSwitch).isChecked = entry.enabled
            view.findViewById<View>(R.id.appCard).setBackgroundResource(
                if (entry.enabled) R.drawable.bg_card_selected else R.drawable.bg_card
            )
            return view
        }
    }
}
