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

        val user = intent.getStringExtra("user") ?: Prefs.getCurrentUser(this)
        findViewById<TextView>(R.id.username).text = "@$user"
        findViewById<TextView>(R.id.avatar).text = user.first().uppercase()

        val bioText = findViewById<TextView>(R.id.bio_text)
        val bio = findViewById<android.widget.EditText>(R.id.bio)
        val edit = findViewById<android.widget.Button>(R.id.edit_bio)
        val save = findViewById<android.widget.Button>(R.id.save_bio)
        val request = findViewById<android.widget.Button>(R.id.request)

        val me = Prefs.getCurrentUser(this)

        Thread {
            val found = SupabaseApi.findUser(user)
            val bioValue = Regex("\\\"bio\\\"\\s*:\\\"([^\\\"]*)\\\"").find(found ?: "")?.groupValues?.get(1)
            runOnUiThread {
                val safeBio = if (bioValue.isNullOrBlank()) "Busy" else bioValue
                bioText.text = safeBio
                if (me == user) {
                    edit.visibility = android.view.View.VISIBLE
                    bio.visibility = android.view.View.GONE
                    save.visibility = android.view.View.GONE
                } else {
                    request.visibility = android.view.View.VISIBLE
                }
            }
        }.start()

        edit.setOnClickListener {
            bio.setText(bioText.text)
            bio.visibility = android.view.View.VISIBLE
            save.visibility = android.view.View.VISIBLE
            edit.visibility = android.view.View.GONE
        }

        save.setOnClickListener {
            val text = bio.text.toString()
            Thread {
                SupabaseApi.updateBio(user, text)
            }.start()
            bioText.text = text
            bio.visibility = android.view.View.GONE
            save.visibility = android.view.View.GONE
            edit.visibility = android.view.View.VISIBLE
        }

        request.setOnClickListener {
            Thread {
                SupabaseApi.sendRequest(Prefs.getCurrentUser(this), user)
            }.start()
            request.text = "Request sent"
            request.isEnabled = false
        }

        if (me != user) {
            Thread {
                val has = SupabaseApi.hasContact(me, user)
                runOnUiThread {
                    if (has) {
                        request.text = "Chat"
                        request.isEnabled = true
                        request.setOnClickListener {
                            val intent = android.content.Intent(this, ChatActivity::class.java)
                            intent.putExtra("peer", user)
                            startActivity(intent)
                        }
                    }
                }
            }.start()
        }
    }
}
