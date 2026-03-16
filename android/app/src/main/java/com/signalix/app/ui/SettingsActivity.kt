package com.signalix.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeToggle = findViewById<android.widget.Switch>(R.id.theme_toggle)
        themeToggle.isChecked = Prefs.isDark(this)
        themeToggle.setOnCheckedChangeListener { _, checked ->
            Prefs.setTheme(this, checked)
            recreate()
        }

        findViewById<android.widget.Button>(R.id.palette).setOnClickListener {
            val options = arrayOf("Indigo", "Cyan/Amber")
            val values = arrayOf("indigo", "alt")
            android.app.AlertDialog.Builder(this)
                .setTitle("Color palette")
                .setItems(options) { _, which ->
                    Prefs.setPalette(this, values[which])
                    recreate()
                }
                .show()
        }
    }
}
