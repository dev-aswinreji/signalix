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
            val u = username.text.toString().trim()
            val p = password.text.toString().trim()
            if (u.isBlank()) {
                username.error = "Username required"
                return@setOnClickListener
            }
            Thread {
                val ok = login(u, p)
                runOnUiThread {
                    if (ok) {
                        com.signalix.app.data.Prefs.setRemember(this, remember.isChecked, u, p)
                        startActivity(Intent(this, ChatListActivity::class.java))
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        findViewById<android.widget.Button>(R.id.register).setOnClickListener {
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

    private fun login(username: String, token: String): Boolean {
        if (username.isBlank() || token.isBlank()) return false
        return try {
            val url = URL("${baseUrl}/rest/v1/users?username=eq.$username")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", Supabase.ANON)
            conn.setRequestProperty("Authorization", "Bearer ${Supabase.ANON}")
            val body = conn.inputStream.bufferedReader().readText()
            val saved = Regex("\\\"token\\\"\\s*:\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
            saved == token
        } catch (e: Exception) {
            false
        }
    }

    private fun applyPalette() {
        val palette = Prefs.getPalette(this)
        if (palette == "alt") {
            setTheme(androidx.appcompat.R.style.ThemeOverlay_AppCompat_Dark)
        }
    }
}
