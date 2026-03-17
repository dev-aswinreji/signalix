package com.signalix.app.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
import com.signalix.app.data.SupabaseApi
import java.util.Base64

class ChatActivity : AppCompatActivity() {
    private lateinit var messages: LinearLayout
    private lateinit var input: EditText
    private lateinit var peer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        peer = intent.getStringExtra("peer") ?: ""
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.chat_toolbar)
        toolbar.title = peer
        findViewById<android.widget.ImageButton>(R.id.profile).setOnClickListener {
            val intent = android.content.Intent(this, ProfileActivity::class.java)
            intent.putExtra("user", peer)
            startActivity(intent)
        }
        messages = findViewById(R.id.messages)
        input = findViewById(R.id.input)

        findViewById<android.widget.Button>(R.id.send).setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isBlank()) return@setOnClickListener
            Thread {
                val user = Prefs.getCurrentUser(this)
                val enc = com.signalix.app.data.SignalManager.encrypt(text, peer)
                SupabaseApi.sendMessage(user, peer, enc)
                runOnUiThread { input.setText("") }
                loadMessages()
            }.start()
        }

        loadMessages()
    }

    private fun loadMessages() {
        Thread {
            val user = Prefs.getCurrentUser(this)
            val body = SupabaseApi.listMessages(user, peer)
            runOnUiThread {
                messages.removeAllViews()
                Regex("\\\"body\\\"\\s*:\\\"([^\\\"]+)\\\"")
                    .findAll(body)
                    .forEach {
                        val text = com.signalix.app.data.SignalManager.decrypt(it.groupValues[1], peer)
                        val tv = TextView(this)
                        tv.text = text
                        messages.addView(tv)
                    }
            }
        }.start()
    }
}
