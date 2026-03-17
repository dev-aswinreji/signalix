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
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadMessages()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        peer = intent.getStringExtra("peer") ?: ""
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.chat_toolbar)
        toolbar.title = peer
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous)
        toolbar.setNavigationOnClickListener { finish() }
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

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun loadMessages() {
        Thread {
            val user = Prefs.getCurrentUser(this)
            val body = SupabaseApi.listMessages(user, peer)
            val currentUser = user
            runOnUiThread {
                messages.removeAllViews()
                val pattern = Regex("\\\"sender\\\"\\s*:\\\"([^\\\"]+)\\\"[\\s\\S]*?\\\"body\\\"\\s*:\\\"([^\\\"]+)\\\"")
                pattern.findAll(body).forEach { m ->
                    val sender = m.groupValues[1]
                    val text = com.signalix.app.data.SignalManager.decrypt(m.groupValues[2], peer)
                    val row = layoutInflater.inflate(R.layout.item_message, messages, false)
                    val tv = row.findViewById<TextView>(R.id.text)
                    val status = row.findViewById<TextView>(R.id.status)
                    tv.text = text
                    val params = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    if (sender == currentUser) {
                        params.gravity = android.view.Gravity.END
                        row.layoutParams = params
                        row.setBackgroundColor(android.graphics.Color.parseColor("#41533B"))
                        status.text = "✓✓"
                    } else {
                        params.gravity = android.view.Gravity.START
                        row.layoutParams = params
                        row.setBackgroundColor(android.graphics.Color.parseColor("#1C222E"))
                        status.text = ""
                    }
                    messages.addView(row)
                }
            }
        }.start()
    }
}
