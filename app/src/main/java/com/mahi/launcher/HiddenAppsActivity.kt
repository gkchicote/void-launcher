package com.mahi.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var hiddenAppsManager: HiddenAppsManager
    private lateinit var appRepository: AppRepository
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        hiddenAppsManager = HiddenAppsManager(this)
        appRepository = AppRepository(this)
        recyclerView = findViewById(R.id.hidden_apps_list)

        val themeManager = ThemeManager(this)
        val colors = themeManager.getColors()
        findViewById<View>(R.id.hidden_apps_root).setBackgroundColor(colors.drawerBackground)
        findViewById<ImageView>(R.id.hidden_back).setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        refreshList()
    }

    private fun refreshList() {
        val allApps = appRepository.getAllApps(includeHidden = true)
        recyclerView.adapter = HiddenAppsAdapter(allApps)
    }

    inner class HiddenAppsAdapter(private val apps: List<AppModel>) :
        RecyclerView.Adapter<HiddenAppsAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.hidden_app_icon)
            val label: TextView = view.findViewById(R.id.hidden_app_label)
            val toggle: Switch = view.findViewById(R.id.hidden_app_toggle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hidden_app, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = apps[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            holder.toggle.isChecked = !hiddenAppsManager.isHidden(app.packageName)

            holder.toggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    hiddenAppsManager.unhideApp(app.packageName)
                } else {
                    hiddenAppsManager.hideApp(app.packageName)
                }
            }
        }

        override fun getItemCount() = apps.size
    }
}
