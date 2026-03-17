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

        val bio = findViewById<android.widget.EditText>(R.id.bio)
        val save = findViewById<android.widget.Button>(R.id.save_bio)
        val request = findViewById<android.widget.ImageButton>(R.id.request)

        val me = Prefs.getCurrentUser(this)
        if (me == user) {
            request.visibility = android.view.View.GONE
        }

        Thread {
            val found = SupabaseApi.findUser(user)
            val bioValue = Regex("\\\"bio\\\"\\s*:\\\"([^\\\"]*)\\\"").find(found ?: "")?.groupValues?.get(1)
            runOnUiThread { if (bioValue != null) bio.setText(bioValue) }
        }.start()

        save.setOnClickListener {
            val text = bio.text.toString()
            Thread {
                SupabaseApi.updateBio(user, text)
            }.start()
        }

        request.setOnClickListener {
            Thread {
                SupabaseApi.sendRequest(Prefs.getCurrentUser(this), user)
            }.start()
        }
    }
}
