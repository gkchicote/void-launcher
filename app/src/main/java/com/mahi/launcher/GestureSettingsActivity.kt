package com.mahi.launcher

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class GestureSettingsActivity : AppCompatActivity() {

    private val gestureKeys = listOf(
        "gesture_double_tap" to "Double Tap",
        "gesture_swipe_up" to "Swipe Up",
        "gesture_swipe_down" to "Swipe Down",
        "gesture_swipe_left" to "Swipe Left",
        "gesture_swipe_right" to "Swipe Right",
        "gesture_pinch_in" to "Pinch In",
        "gesture_pinch_out" to "Pinch Out",
        "gesture_long_press" to "Long Press"
    )

    private val actions = arrayOf(
        "None", "App Drawer", "Notifications", "Quick Settings",
        "Lock Screen", "Launcher Settings", "Phone Settings", "Recent Apps"
    )
    private val actionValues = arrayOf(
        "none", "app_drawer", "notifications", "quick_settings",
        "lock_screen", "settings", "phone_settings", "recent_apps"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_settings)

        val container = findViewById<LinearLayout>(R.id.gesture_list_container)
        val themeManager = ThemeManager(this)
        val colors = themeManager.getColors()

        findViewById<View>(R.id.gesture_settings_root).setBackgroundColor(colors.drawerBackground)
        findViewById<ImageView>(R.id.gesture_back).setOnClickListener { finish() }

        for ((key, label) in gestureKeys) {
            val row = layoutInflater.inflate(R.layout.item_gesture_row, container, false)

            val labelText = row.findViewById<TextView>(R.id.gesture_label)
            labelText.text = label
            labelText.setTextColor(colors.textPrimary)

            val spinner = row.findViewById<Spinner>(R.id.gesture_action_spinner)
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, actions)

            val currentValue = MahiApp.prefs.getString(key, "none")
            spinner.setSelection(actionValues.indexOf(currentValue).coerceAtLeast(0))

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    MahiApp.prefs.edit().putString(key, actionValues[pos]).apply()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            container.addView(row)
        }
    }
}
