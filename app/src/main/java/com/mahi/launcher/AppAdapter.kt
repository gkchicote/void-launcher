package com.mahi.launcher

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val context: Context,
    private var apps: List<AppModel>,
    private val showLabels: Boolean = true,
    private val iconSizeDp: Int = 58,
    private val onAppClick: (AppModel, View) -> Unit,
    private val onAppLongClick: ((AppModel, View) -> Boolean)? = null
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private val themeManager = ThemeManager(context)
    private val iconPackManager = IconPackManager(context)
    private val activeIconPack = iconPackManager.getActiveIconPack()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val label: TextView = view.findViewById(R.id.app_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        val colors = themeManager.getColors()

        // icon — try icon pack first
        val customIcon = activeIconPack?.let { iconPackManager.getIconForApp(it, app.packageName) }
        holder.icon.setImageDrawable(customIcon ?: app.icon)

        // icon size
        val sizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, iconSizeDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        holder.icon.layoutParams.width = sizePx
        holder.icon.layoutParams.height = sizePx

        // label
        if (showLabels) {
            holder.label.visibility = View.VISIBLE
            holder.label.text = app.label
            holder.label.setTextColor(colors.textPrimary)
        } else {
            holder.label.visibility = View.GONE
        }

        // click
        holder.itemView.setOnClickListener { view ->
            AnimationHelper.animateAppLaunch(holder.icon) {
                onAppClick(app, view)
            }
        }

        onAppLongClick?.let { callback ->
            holder.itemView.setOnLongClickListener { view ->
                callback(app, view)
            }
        }

        // entry animation
        AnimationHelper.animateGridItemEntry(holder.itemView, position)
    }

    override fun getItemCount() = apps.size

    fun updateApps(newApps: List<AppModel>) {
        apps = newApps
        notifyDataSetChanged()
    }
}
