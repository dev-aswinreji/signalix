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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow

class ChatActivity : AppCompatActivity() {
    private lateinit var messages: LinearLayout
    private lateinit var input: EditText
    private lateinit var peer: String
    private lateinit var supabase: io.github.jan.supabase.SupabaseClient
    private var realtimeJob: kotlinx.coroutines.Job? = null
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        peer = intent.getStringExtra("peer") ?: ""
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.chat_toolbar)
        toolbar.title = peer
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        supabase = io.github.jan.supabase.createSupabaseClient(
            com.signalix.app.data.Supabase.URL,
            com.signalix.app.data.Supabase.ANON
        ) {
            install(io.github.jan.supabase.realtime.Realtime)
        }
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
        startRealtime()
    }

    override fun onPause() {
        super.onPause()
        stopRealtime()
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

    private fun startRealtime() {
        stopRealtime()
        val currentUser = Prefs.getCurrentUser(this)
        val channel = supabase.channel("chat-$currentUser-$peer")
        realtimeChannel = channel
        realtimeJob = lifecycleScope.launch {
            val flow = channel.postgresChangeFlow<io.github.jan.supabase.realtime.PostgresAction.Insert>(
                schema = "public"
            )
            channel.subscribe()
            flow.collect {
                loadMessages()
            }
        }
    }

    private fun stopRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
        realtimeChannel?.let { ch ->
            lifecycleScope.launch { ch.unsubscribe() }
        }
        realtimeChannel = null
    }
}
