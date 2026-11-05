package com.mahi.launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        themeManager = ThemeManager(this)

        setupToolbar()
        setupThemeSection()
        setupGridSection()
        setupGestureSection()
        setupAppearanceSection()
        setupHiddenAppsSection()
        setupIconPackSection()
        setupAboutSection()
        applyTheme()
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.settings_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.settings_title).text = "Void Launcher Settings"
        findViewById<TextView>(R.id.settings_prime_badge).text = "PRIME"
    }

    private fun setupThemeSection() {
        val themeSpinner = findViewById<Spinner>(R.id.theme_spinner)
        val themes = arrayOf("Dark", "Light", "AMOLED Black", "System")
        val themeValues = arrayOf("dark", "light", "amoled", "system")
        themeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, themes)

        val currentTheme = themeManager.getCurrentTheme()
        themeSpinner.setSelection(themeValues.indexOf(currentTheme).coerceAtLeast(0))

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                themeManager.setTheme(themeValues[pos])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // accent color
        val accentInput = findViewById<EditText>(R.id.accent_color_input)
        accentInput.setText(MahiApp.prefs.getString("accent_color", "#6C5CE7"))
        findViewById<Button>(R.id.accent_apply_btn).setOnClickListener {
            val hex = accentInput.text.toString().trim()
            try {
                Color.parseColor(hex)
                themeManager.setAccentColor(hex)
                Toast.makeText(this, "Accent color applied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid hex color", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupGridSection() {
        val colsSeek = findViewById<SeekBar>(R.id.grid_columns_seek)
        val rowsSeek = findViewById<SeekBar>(R.id.grid_rows_seek)
        val colsLabel = findViewById<TextView>(R.id.grid_columns_label)
        val rowsLabel = findViewById<TextView>(R.id.grid_rows_label)

        colsSeek.max = 4 // 3-7 columns
        colsSeek.progress = MahiApp.prefs.getInt("grid_columns", 4) - 3
        colsLabel.text = "Columns: ${colsSeek.progress + 3}"

        rowsSeek.max = 5 // 3-8 rows
        rowsSeek.progress = MahiApp.prefs.getInt("grid_rows", 5) - 3
        rowsLabel.text = "Rows: ${rowsSeek.progress + 3}"

        colsSeek.setOnSeekBarChangeListener(seekListener { value ->
            val cols = value + 3
            MahiApp.prefs.edit().putInt("grid_columns", cols).apply()
            colsLabel.text = "Columns: $cols"
        })

        rowsSeek.setOnSeekBarChangeListener(seekListener { value ->
            val rows = value + 3
            MahiApp.prefs.edit().putInt("grid_rows", rows).apply()
            rowsLabel.text = "Rows: $rows"
        })

        // icon size
        val iconSeek = findViewById<SeekBar>(R.id.icon_size_seek)
        val iconLabel = findViewById<TextView>(R.id.icon_size_label)
        iconSeek.max = 40 // 38-78dp
        iconSeek.progress = MahiApp.prefs.getInt("icon_size", 58) - 38
        iconLabel.text = "Icon Size: ${iconSeek.progress + 38}dp"

        iconSeek.setOnSeekBarChangeListener(seekListener { value ->
            val size = value + 38
            MahiApp.prefs.edit().putInt("icon_size", size).apply()
            iconLabel.text = "Icon Size: ${size}dp"
        })

        // toggles
        val labelsSwitch = findViewById<Switch>(R.id.show_labels_switch)
        labelsSwitch.isChecked = MahiApp.prefs.getBoolean("show_labels", true)
        labelsSwitch.setOnCheckedChangeListener { _, checked ->
            MahiApp.prefs.edit().putBoolean("show_labels", checked).apply()
        }

        val dockSwitch = findViewById<Switch>(R.id.show_dock_switch)
        dockSwitch.isChecked = MahiApp.prefs.getBoolean("show_dock", true)
        dockSwitch.setOnCheckedChangeListener { _, checked ->
            MahiApp.prefs.edit().putBoolean("show_dock", checked).apply()
        }

        val searchSwitch = findViewById<Switch>(R.id.show_search_switch)
        searchSwitch.isChecked = MahiApp.prefs.getBoolean("show_search_bar", true)
        searchSwitch.setOnCheckedChangeListener { _, checked ->
            MahiApp.prefs.edit().putBoolean("show_search_bar", checked).apply()
        }
    }

    private fun setupGestureSection() {
        findViewById<View>(R.id.gesture_settings_btn).setOnClickListener {
            startActivity(Intent(this, GestureSettingsActivity::class.java))
        }
    }

    private fun setupAppearanceSection() {
        val animSpinner = findViewById<Spinner>(R.id.animation_spinner)
        val anims = arrayOf("Slide", "Scale", "Fade", "Bounce", "None")
        val animValues = arrayOf("slide", "scale", "fade", "bounce", "none")
        animSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, anims)

        val currentAnim = MahiApp.prefs.getString("animation_style", "slide")
        animSpinner.setSelection(animValues.indexOf(currentAnim).coerceAtLeast(0))

        animSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                MahiApp.prefs.edit().putString("animation_style", animValues[pos]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // animation speed
        val speedSeek = findViewById<SeekBar>(R.id.animation_speed_seek)
        val speedLabel = findViewById<TextView>(R.id.animation_speed_label)
        speedSeek.max = 20 // 0.0 to 2.0
        speedSeek.progress = (MahiApp.prefs.getFloat("animation_speed", 1.0f) * 10).toInt()
        speedLabel.text = "Speed: ${speedSeek.progress / 10.0f}x"

        speedSeek.setOnSeekBarChangeListener(seekListener { value ->
            val speed = value / 10.0f
            MahiApp.prefs.edit().putFloat("animation_speed", speed).apply()
            speedLabel.text = "Speed: ${speed}x"
        })
    }

    private fun setupHiddenAppsSection() {
        findViewById<View>(R.id.hidden_apps_btn).setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }
    }

    private fun setupIconPackSection() {
        findViewById<View>(R.id.icon_pack_btn).setOnClickListener {
            startActivity(Intent(this, IconPackPickerActivity::class.java))
        }
    }

    private fun setupAboutSection() {
        findViewById<TextView>(R.id.about_text).text =
            "Void Launcher v1.0.0\nPrime Edition — All features unlocked\nNo ads. No tracking. No limits."
    }

    private fun applyTheme() {
        val colors = themeManager.getColors()
        findViewById<View>(R.id.settings_root).setBackgroundColor(colors.drawerBackground)
    }

    private fun seekListener(onChange: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) onChange(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}
