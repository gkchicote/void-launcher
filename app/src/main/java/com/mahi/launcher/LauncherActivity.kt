package com.mahi.launcher

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity : AppCompatActivity() {

    private lateinit var appRepository: AppRepository
    private lateinit var gestureHandler: GestureHandler
    private lateinit var themeManager: ThemeManager

    private lateinit var homeGrid: RecyclerView
    private lateinit var dockBar: RecyclerView
    private lateinit var clockText: TextView
    private lateinit var dateText: TextView
    private lateinit var searchBar: LinearLayout
    private lateinit var searchIcon: ImageView

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            updateClock()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // fullscreen immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        setContentView(R.layout.activity_launcher)

        appRepository = AppRepository(this)
        themeManager = ThemeManager(this)

        homeGrid = findViewById(R.id.home_grid)
        dockBar = findViewById(R.id.dock_bar)
        clockText = findViewById(R.id.clock_text)
        dateText = findViewById(R.id.date_text)
        searchBar = findViewById(R.id.search_bar)
        searchIcon = findViewById(R.id.search_icon)

        setupGestures()
        setupGrid()
        setupDock()
        setupSearchBar()
        updateClock()

        registerReceiver(timeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
        })
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
        updateClock()
        applyTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(timeReceiver) } catch (_: Exception) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        gestureHandler = GestureHandler(this) { action ->
            gestureHandler.resolveGestureAction(action)
        }

        val rootView = findViewById<View>(R.id.launcher_root)
        rootView.setOnTouchListener { _, event ->
            gestureHandler.scaleDetector.onTouchEvent(event)
            gestureHandler.gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupGrid() {
        val columns = MahiApp.prefs.getInt("grid_columns", 4)
        homeGrid.layoutManager = GridLayoutManager(this, columns)
        homeGrid.setHasFixedSize(true)
    }

    private fun setupDock() {
        dockBar.layoutManager = GridLayoutManager(this, 4)
        val showDock = MahiApp.prefs.getBoolean("show_dock", true)
        dockBar.visibility = if (showDock) View.VISIBLE else View.GONE
    }

    private fun setupSearchBar() {
        val showSearch = MahiApp.prefs.getBoolean("show_search_bar", true)
        searchBar.visibility = if (showSearch) View.VISIBLE else View.GONE

        searchBar.setOnClickListener {
            startActivity(
                Intent(this, AppDrawerActivity::class.java)
                    .putExtra("focus_search", true)
            )
        }
    }

    private fun refreshApps() {
        val showLabels = MahiApp.prefs.getBoolean("show_labels", true)
        val iconSize = MahiApp.prefs.getInt("icon_size", 58)

        // home grid — first page of apps (grid_rows * grid_columns)
        val columns = MahiApp.prefs.getInt("grid_columns", 4)
        val rows = MahiApp.prefs.getInt("grid_rows", 5)
        val pageSize = columns * rows
        val allApps = appRepository.getAllApps()
        val homeApps = allApps.take(pageSize)

        homeGrid.adapter = AppAdapter(
            context = this,
            apps = homeApps,
            showLabels = showLabels,
            iconSizeDp = iconSize,
            onAppClick = { app, _ -> launchApp(app) },
            onAppLongClick = { app, view -> showAppContextMenu(app, view); true }
        )

        // dock
        val dockApps = appRepository.getDockApps()
        dockBar.adapter = AppAdapter(
            context = this,
            apps = dockApps,
            showLabels = false,
            iconSizeDp = 48,
            onAppClick = { app, _ -> launchApp(app) },
            onAppLongClick = { app, view -> showAppContextMenu(app, view); true }
        )
    }

    private fun launchApp(app: AppModel) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent?.let { startActivity(it) }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not launch ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppContextMenu(app: AppModel, view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "App Info")
        popup.menu.add(0, 2, 0, "Hide App")
        popup.menu.add(0, 3, 0, "Add to Dock")
        popup.menu.add(0, 4, 0, "Uninstall")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:${app.packageName}")
                    startActivity(intent)
                    true
                }
                2 -> {
                    HiddenAppsManager(this).hideApp(app.packageName)
                    refreshApps()
                    Toast.makeText(this, "${app.label} hidden", Toast.LENGTH_SHORT).show()
                    true
                }
                3 -> {
                    val dockApps = MahiApp.prefs.getStringSet("dock_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                    if (dockApps.size < 5) {
                        dockApps.add(app.packageName)
                        MahiApp.prefs.edit().putStringSet("dock_apps", dockApps).apply()
                        refreshApps()
                        Toast.makeText(this, "${app.label} added to dock", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Dock full (max 5)", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                4 -> {
                    val intent = Intent(Intent.ACTION_DELETE)
                    intent.data = android.net.Uri.parse("package:${app.packageName}")
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun updateClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        val now = Date()
        clockText.text = timeFormat.format(now)
        dateText.text = dateFormat.format(now).replaceFirstChar { it.uppercase() }
    }

    private fun applyTheme() {
        val colors = themeManager.getColors()
        clockText.setTextColor(colors.textPrimary)
        dateText.setTextColor(colors.textSecondary)

        val dockBg = findViewById<View>(R.id.dock_container)
        dockBg?.background = ColorDrawable(colors.dockBackground).apply { alpha = 180 }

        searchBar.background?.setTint(colors.searchBarBackground)
    }

    override fun onBackPressed() {
        // launcher is home — back does nothing
    }
}
