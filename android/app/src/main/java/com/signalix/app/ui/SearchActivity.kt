package com.signalix.app.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
import com.signalix.app.data.SupabaseApi

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        val query = findViewById<EditText>(R.id.query)
        val results = findViewById<LinearLayout>(R.id.results)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.search_btn).setOnClickListener {
            val raw = query.text.toString().trim().removePrefix("@").lowercase()
            if (raw.isBlank()) return@setOnClickListener
            Thread {
                val body = SupabaseApi.searchUsers(raw)
                runOnUiThread {
                    results.removeAllViews()
                    val me = Prefs.getCurrentUser(this)
                    val users = Regex("\\\"username\\\"\\s*:\\\"([^\\\"]+)\\\"")
                        .findAll(body).map { it.groupValues[1] }
                        .filter { it != me }
                        .toList()
                    if (users.isEmpty()) {
                        val tv = TextView(this)
                        tv.text = "No users found"
                        results.addView(tv)
                    } else {
                        users.forEach { u ->
                            val tv = TextView(this)
                            tv.text = "@$u"
                            tv.textSize = 18f
                            tv.setPadding(0, 16, 0, 16)
                            tv.setOnClickListener {
                                val intent = android.content.Intent(this, ProfileActivity::class.java)
                                intent.putExtra("user", u)
                                startActivity(intent)
                            }
                            results.addView(tv)
                        }
                    }
                }
            }.start()
        }
    }
}
