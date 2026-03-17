package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R

class ChatListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        val list = findViewById<android.widget.LinearLayout>(R.id.chat_list)
        val placeholder = findViewById<android.widget.TextView>(R.id.placeholder)
        val search = findViewById<android.widget.EditText>(R.id.search)

        findViewById<android.widget.ImageButton>(R.id.profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<android.widget.ImageButton>(R.id.menu).setOnClickListener {
            val popup = android.widget.PopupMenu(this, it)
            popup.menu.add("Settings")
            popup.menu.add("Logout")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Settings") {
                    startActivity(Intent(this, SettingsActivity::class.java))
                } else {
                    com.signalix.app.data.Prefs.setRemember(this, false, "", "")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                true
            }
            popup.show()
        }

        findViewById<android.widget.ImageButton>(R.id.search_toggle).setOnClickListener {
            search.visibility = if (search.visibility == android.view.View.GONE) android.view.View.VISIBLE else android.view.View.GONE
        }

        findViewById<android.widget.ImageButton>(R.id.add_contact).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<android.widget.ImageButton>(R.id.new_chat_fab).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        loadContacts(list, placeholder)
        loadRequests(list)
        notifyRequests()
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
                        val row = layoutInflater.inflate(R.layout.item_chat_row, list, false)
                        row.findViewById<android.widget.TextView>(R.id.name).text = c
                        row.findViewById<android.widget.TextView>(R.id.avatar).text = c.first().uppercase()
                        row.setOnClickListener {
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.putExtra("peer", c)
                            startActivity(intent)
                        }
                        list.addView(row)
                    }
                }
            }
        }.start()
    }

    private fun loadRequests(list: android.widget.LinearLayout) {
        Thread {
            val me = com.signalix.app.data.Prefs.getCurrentUser(this)
            val body = com.signalix.app.data.SupabaseApi.listRequests(me)
            val ids = Regex("\\\"id\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(body).map { it.groupValues[1] }.toList()
            val senders = Regex("\\\"sender\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(body).map { it.groupValues[1] }.toList()
            runOnUiThread {
                ids.zip(senders).forEach { (id, sender) ->
                    val tv = android.widget.TextView(this)
                    tv.text = "Request from $sender (tap to accept)"
                    tv.setPadding(0, 8, 0, 8)
                    tv.setOnClickListener {
                        Thread {
                            com.signalix.app.data.SupabaseApi.acceptRequest(id)
                            com.signalix.app.data.SupabaseApi.addContact(me, sender)
                            runOnUiThread {
                                loadContacts(list, findViewById(R.id.placeholder))
                            }
                        }.start()
                    }
                    list.addView(tv)
                }
            }
        }.start()
    }

    private fun notifyRequests() {
        Thread {
            val me = com.signalix.app.data.Prefs.getCurrentUser(this)
            val body = com.signalix.app.data.SupabaseApi.listRequests(me)
            val count = Regex("\\\"id\\\"\\s*:\\\"([^\\\"]+)\\\"").findAll(body).count()
            if (count > 0) {
                val nm = getSystemService(android.app.NotificationManager::class.java)
                val channelId = "requests"
                if (nm.getNotificationChannel(channelId) == null) {
                    nm.createNotificationChannel(android.app.NotificationChannel(channelId, "Requests", android.app.NotificationManager.IMPORTANCE_DEFAULT))
                }
                val notif = androidx.core.app.NotificationCompat.Builder(this, channelId)
                    .setContentTitle("New friend request")
                    .setContentText("You have $count new request(s)")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build()
                nm.notify(1001, notif)
            }
        }.start()
    }
}
