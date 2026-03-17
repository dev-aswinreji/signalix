package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R

class ChatListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        findViewById<android.widget.ImageButton>(R.id.settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<android.widget.ImageButton>(R.id.logout).setOnClickListener {
            com.signalix.app.data.Prefs.setRemember(this, false, "", "")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
