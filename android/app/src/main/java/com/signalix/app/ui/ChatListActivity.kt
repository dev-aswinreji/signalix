package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R

class ChatListActivity : AppCompatActivity() {
    private val contactsCache = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        val list = findViewById<android.widget.LinearLayout>(R.id.chat_list)
        val placeholder = findViewById<android.widget.TextView>(R.id.placeholder)
        val search = findViewById<android.widget.EditText>(R.id.search)
        val requestsHeader = findViewById<android.widget.TextView>(R.id.requests_header)
        val requestsList = findViewById<android.widget.LinearLayout>(R.id.requests_list)

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
            val nowVisible = search.visibility == android.view.View.GONE
            search.visibility = if (nowVisible) android.view.View.VISIBLE else android.view.View.GONE
            if (!nowVisible) {
                search.setText("")
                renderContacts(list, placeholder, contactsCache)
            }
        }

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim().orEmpty().lowercase()
                val filtered = if (q.isBlank()) contactsCache else contactsCache.filter { it.lowercase().contains(q) }
                renderContacts(list, placeholder, filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.new_chat_fab).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        loadContacts(list, placeholder)
        loadRequests(requestsHeader, requestsList)
        notifyRequests()
    }

    override fun onResume() {
        super.onResume()
        val list = findViewById<android.widget.LinearLayout>(R.id.chat_list)
        val placeholder = findViewById<android.widget.TextView>(R.id.placeholder)
        val requestsHeader = findViewById<android.widget.TextView>(R.id.requests_header)
        val requestsList = findViewById<android.widget.LinearLayout>(R.id.requests_list)
        loadContacts(list, placeholder)
        loadRequests(requestsHeader, requestsList)
        notifyRequests()
    }

    private fun loadContacts(list: android.widget.LinearLayout, placeholder: android.widget.TextView) {
        Thread {
            val me = com.signalix.app.data.Prefs.getCurrentUser(this)
            val body = com.signalix.app.data.SupabaseApi.listContacts(me)
            val blockedBody = com.signalix.app.data.SupabaseApi.listBlocked(me)
            val blocked = Regex("\\\"blocked\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(blockedBody).map { it.groupValues[1] }.toSet()
            val contacts = Regex("\\\"contact\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(body)
                .map { it.groupValues[1] }
                .filter { it !in blocked }
                .toList()
            runOnUiThread {
                contactsCache.clear()
                contactsCache.addAll(contacts)
                renderContacts(list, placeholder, contactsCache)
            }
        }.start()
    }

    private fun renderContacts(
        list: android.widget.LinearLayout,
        placeholder: android.widget.TextView,
        contacts: List<String>
    ) {
        list.removeAllViews()
        if (contacts.isEmpty()) {
            placeholder.visibility = android.view.View.VISIBLE
        } else {
            placeholder.visibility = android.view.View.GONE
            contacts.forEach { c ->
                val row = layoutInflater.inflate(R.layout.item_chat_row, list, false)
                row.findViewById<android.widget.TextView>(R.id.name).text = c
                row.findViewById<android.widget.TextView>(R.id.avatar).text = c.first().uppercase()
                val unread = com.signalix.app.data.SupabaseApi.countUnread(Prefs.getCurrentUser(this), c)
                val time = row.findViewById<android.widget.TextView>(R.id.time)
                time.text = if (unread > 0) unread.toString() else ""
                row.setOnClickListener {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("peer", c)
                    startActivity(intent)
                }
                list.addView(row)
            }
        }
    }

    private fun loadRequests(header: android.widget.TextView, list: android.widget.LinearLayout) {
        Thread {
            val me = com.signalix.app.data.Prefs.getCurrentUser(this)
            val body = com.signalix.app.data.SupabaseApi.listRequests(me)
            val ids = Regex("\\\"id\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(body).map { it.groupValues[1] }.toList()
            val senders = Regex("\\\"sender\\\"\\s*:\\\"([^\\\"]+)\\\"")
                .findAll(body).map { it.groupValues[1] }.toList()
            runOnUiThread {
                list.removeAllViews()
                if (ids.isEmpty()) {
                    header.visibility = android.view.View.GONE
                    return@runOnUiThread
                }
                header.visibility = android.view.View.VISIBLE
                ids.zip(senders).forEach { (id, sender) ->
                    val row = android.widget.LinearLayout(this)
                    row.orientation = android.widget.LinearLayout.VERTICAL
                    row.setPadding(0, 12, 0, 12)

                    val title = android.widget.TextView(this)
                    title.text = "@$sender sent you a friend request"
                    title.textSize = 15f
                    title.setOnClickListener {
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("user", sender)
                        startActivity(intent)
                    }

                    val actions = android.widget.LinearLayout(this)
                    actions.orientation = android.widget.LinearLayout.HORIZONTAL
                    actions.setPadding(0, 8, 0, 0)

                    val accept = android.widget.Button(this)
                    accept.text = "Accept"
                    accept.setOnClickListener {
                        Thread {
                            com.signalix.app.data.SupabaseApi.acceptRequest(id)
                            com.signalix.app.data.SupabaseApi.addContact(me, sender)
                            com.signalix.app.data.SupabaseApi.addContact(sender, me)
                            runOnUiThread {
                                loadContacts(findViewById(R.id.chat_list), findViewById(R.id.placeholder))
                                loadRequests(header, list)
                            }
                        }.start()
                    }

                    val reject = android.widget.Button(this)
                    reject.text = "Reject"
                    reject.setOnClickListener {
                        Thread {
                            com.signalix.app.data.SupabaseApi.rejectRequest(id)
                            runOnUiThread { loadRequests(header, list) }
                        }.start()
                    }

                    val block = android.widget.Button(this)
                    block.text = "Block"
                    block.setOnClickListener {
                        Thread {
                            com.signalix.app.data.SupabaseApi.blockUser(me, sender)
                            com.signalix.app.data.SupabaseApi.rejectRequest(id)
                            runOnUiThread { loadRequests(header, list) }
                        }.start()
                    }

                    actions.addView(accept)
                    actions.addView(reject)
                    actions.addView(block)

                    row.addView(title)
                    row.addView(actions)
                    list.addView(row)
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
                    .setContentText("You have $count new request(s). Open Signalix to respond.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build()
                nm.notify(1001, notif)
            }
        }.start()
    }
}
