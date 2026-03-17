package com.signalix.app.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
import com.signalix.app.data.SupabaseApi

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        val user = intent.getStringExtra("user") ?: return
        findViewById<TextView>(R.id.username).text = "@$user"
        findViewById<TextView>(R.id.avatar).text = user.first().uppercase()

        findViewById<android.widget.ImageButton>(R.id.request).setOnClickListener {
            Thread {
                SupabaseApi.sendRequest(Prefs.getCurrentUser(this), user)
            }.start()
        }
    }
}
