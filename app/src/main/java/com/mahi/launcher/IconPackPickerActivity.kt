package com.mahi.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IconPackPickerActivity : AppCompatActivity() {

    private lateinit var iconPackManager: IconPackManager
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_pack)

        iconPackManager = IconPackManager(this)
        recyclerView = findViewById(R.id.icon_pack_list)

        val themeManager = ThemeManager(this)
        val colors = themeManager.getColors()
        findViewById<View>(R.id.icon_pack_root).setBackgroundColor(colors.drawerBackground)
        findViewById<ImageView>(R.id.icon_pack_back).setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)

        val packs = iconPackManager.getInstalledIconPacks()
        if (packs.isEmpty()) {
            Toast.makeText(this, "No icon packs installed", Toast.LENGTH_LONG).show()
        }

        recyclerView.adapter = IconPackAdapter(packs)
    }

    inner class IconPackAdapter(private val packs: List<IconPackManager.IconPack>) :
        RecyclerView.Adapter<IconPackAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.pack_icon)
            val label: TextView = view.findViewById(R.id.pack_label)
            val active: View = view.findViewById(R.id.pack_active_indicator)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_icon_pack, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val pack = packs[position]
            holder.icon.setImageDrawable(pack.icon)
            holder.label.text = pack.label

            val isActive = iconPackManager.getActiveIconPack() == pack.packageName
            holder.active.visibility = if (isActive) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                if (isActive) {
                    iconPackManager.setActiveIconPack(null)
                    Toast.makeText(this@IconPackPickerActivity, "Default icons restored", Toast.LENGTH_SHORT).show()
                } else {
                    iconPackManager.setActiveIconPack(pack.packageName)
                    Toast.makeText(this@IconPackPickerActivity, "${pack.label} applied", Toast.LENGTH_SHORT).show()
                }
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = packs.size
    }
}
