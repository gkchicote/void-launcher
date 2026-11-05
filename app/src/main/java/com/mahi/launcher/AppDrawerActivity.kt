package com.mahi.launcher

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var appRepository: AppRepository
    private lateinit var themeManager: ThemeManager
    private lateinit var drawerGrid: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var closeBtn: ImageView

    private var allApps: List<AppModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out)

        // blur behind the drawer window
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 30
        }

        setContentView(R.layout.activity_app_drawer)

        appRepository = AppRepository(this)
        themeManager = ThemeManager(this)

        drawerGrid = findViewById(R.id.drawer_grid)
        searchInput = findViewById(R.id.drawer_search_input)
        closeBtn = findViewById(R.id.drawer_close_btn)

        val columns = MahiApp.prefs.getInt("grid_columns", 4)
        drawerGrid.layoutManager = GridLayoutManager(this, columns)

        allApps = appRepository.getAllApps(includeHidden = false)
        refreshDrawer(allApps)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    refreshDrawer(allApps)
                } else {
                    refreshDrawer(allApps.filter { it.matchesQuery(query) })
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        closeBtn.setOnClickListener { finish() }

        if (intent.getBooleanExtra("focus_search", false)) {
            searchInput.requestFocus()
        }

        applyTheme()

        // animate drawer
        val rootView = findViewById<View>(R.id.drawer_root)
        AnimationHelper.animateDrawerOpen(rootView)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
    }

    private fun refreshDrawer(apps: List<AppModel>) {
        val showLabels = MahiApp.prefs.getBoolean("show_labels", true)
        val iconSize = MahiApp.prefs.getInt("icon_size", 58)

        drawerGrid.adapter = AppAdapter(
            context = this,
            apps = apps,
            showLabels = showLabels,
            iconSizeDp = iconSize,
            onAppClick = { app, _ ->
                try {
                    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.let { startActivity(it) }
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not launch ${app.label}", Toast.LENGTH_SHORT).show()
                }
            },
            onAppLongClick = { app, view ->
                val popup = android.widget.PopupMenu(this, view)
                popup.menu.add(0, 1, 0, "App Info")
                popup.menu.add(0, 2, 0, "Hide App")

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = android.net.Uri.parse("package:${app.packageName}")
                            startActivity(intent)
                            true
                        }
                        2 -> {
                            HiddenAppsManager(this).hideApp(app.packageName)
                            allApps = appRepository.getAllApps(includeHidden = false)
                            refreshDrawer(allApps)
                            Toast.makeText(this, "${app.label} hidden", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
                true
            }
        )
    }

    private fun applyTheme() {
        val colors = themeManager.getColors()
        val root = findViewById<View>(R.id.drawer_root)
        // semi-transparent dark overlay instead of solid color
        root.setBackgroundColor(Color.parseColor("#D0000000"))
        searchInput.setTextColor(colors.searchBarText)
        searchInput.setHintTextColor(colors.textSecondary)
        searchInput.background?.setTint(colors.searchBarBackground)
    }
}
