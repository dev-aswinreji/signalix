package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R

class ChatListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val list = findViewById<android.widget.LinearLayout>(R.id.chat_list)
        val placeholder = findViewById<android.widget.TextView>(R.id.placeholder)

        findViewById<android.widget.ImageButton>(R.id.settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<android.widget.ImageButton>(R.id.logout).setOnClickListener {
            com.signalix.app.data.Prefs.setRemember(this, false, "", "")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<android.widget.Button>(R.id.add_contact).setOnClickListener {
            val input = android.widget.EditText(this)
            input.hint = "username"
            android.app.AlertDialog.Builder(this)
                .setTitle("Add contact")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val u = input.text.toString().trim()
                    if (u.isNotBlank()) {
                        Thread {
                            val me = com.signalix.app.data.Prefs.getCurrentUser(this)
                            com.signalix.app.data.SupabaseApi.addContact(me, u)
                            runOnUiThread { loadContacts(list, placeholder) }
                        }.start()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadContacts(list, placeholder)
    }

    private fun loadContacts(list: android.widget.LinearLayout, placeholder: android.widget.TextView) {
        Thread {
            val me = com.signalix.app.data.Prefs.getCurrentUser(this)
            val body = com.signalix.app.data.SupabaseApi.listContacts(me)
            val contacts = Regex("\\\"contact\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(body)
                .map { it.groupValues[1] }
                .toList()
            runOnUiThread {
                list.removeAllViews()
                if (contacts.isEmpty()) {
                    placeholder.visibility = android.view.View.VISIBLE
                } else {
                    placeholder.visibility = android.view.View.GONE
                    contacts.forEach { c ->
                        val tv = android.widget.TextView(this)
                        tv.text = c
                        tv.setPadding(0, 16, 0, 16)
                        tv.setOnClickListener {
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.putExtra("peer", c)
                            startActivity(intent)
                        }
                        list.addView(tv)
                    }
                }
            }
        }.start()
    }
}
