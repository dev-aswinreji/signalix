package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
import com.signalix.app.data.Supabase
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {
    private val baseUrl: String
        get() = com.signalix.app.data.Prefs.getServer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        applyPalette()

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val remember = findViewById<android.widget.CheckBox>(R.id.remember)

        val (remembered, userSaved, tokenSaved) = com.signalix.app.data.Prefs.getRemember(this)
        if (remembered) {
            username.setText(userSaved)
            password.setText(tokenSaved)
            remember.isChecked = true
        }

        findViewById<android.widget.ImageButton>(R.id.login_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.login).setOnClickListener {
            Toast.makeText(this, "LOGIN CLICKED", Toast.LENGTH_SHORT).show()
            val u = username.text.toString().trim()
            val p = password.text.toString().trim()
            if (u.isBlank()) {
                username.error = "Username required"
                Toast.makeText(this, "Username required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (p.isBlank()) {
                password.error = "Token required"
                Toast.makeText(this, "Token required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val (ok, msg) = login(u, p)
                runOnUiThread {
                    if (ok) {
                        com.signalix.app.data.Prefs.setRemember(this, remember.isChecked, u, p)
                        com.signalix.app.data.Prefs.setCurrentUser(this, u)
                        startActivity(Intent(this, ChatListActivity::class.java))
                    } else {
                        Toast.makeText(this, msg ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        findViewById<android.widget.Button>(R.id.register).setOnClickListener {
            Toast.makeText(this, "REGISTER CLICKED", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun register(username: String): Pair<String?, String?> {
        return try {
            val url = URL("$baseUrl/register")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write("{\"username\":\"$username\"}".toByteArray()) }
            val code = conn.responseCode
            if (code == 409) return null to "Username already taken"
            if (code != 200) return null to "Server error ($code)"
            val body = conn.inputStream.bufferedReader().readText()
            val token = Regex("\\\"token\\\"\\s*:\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
            token to null
        } catch (e: Exception) {
            null to "Network error"
        }
    }

    private fun login(username: String, token: String): Pair<Boolean, String?> {
        if (username.isBlank() || token.isBlank()) return false to "Missing username or token"
        return try {
            val safeUser = java.net.URLEncoder.encode(username, "UTF-8")
            val url = URL("${baseUrl}/rest/v1/users?select=token&username=eq.$safeUser")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("apikey", Supabase.ANON)
            conn.setRequestProperty("Authorization", "Bearer ${Supabase.ANON}")
            val code = conn.responseCode
            if (code != 200) {
                val err = conn.errorStream?.bufferedReader()?.readText()
                return false to "Server error ($code)"
            }
            val body = conn.inputStream.bufferedReader().readText()
            val saved = Regex("\\\"token\\\"\\s*:\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
            if (saved == null) return false to "User not found"
            if (saved != token) return false to "Token mismatch"
            true to null
        } catch (e: Exception) {
            false to "Network error"
        }
    }

    private fun applyPalette() {
        val palette = Prefs.getPalette(this)
        if (palette == "alt") {
            setTheme(androidx.appcompat.R.style.ThemeOverlay_AppCompat_Dark)
        }
    }
}
