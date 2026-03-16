package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
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

        findViewById<android.widget.Button>(R.id.login).setOnClickListener {
            Thread {
                val ok = login(username.text.toString().trim(), password.text.toString().trim())
                runOnUiThread {
                    if (ok) startActivity(Intent(this, ChatListActivity::class.java))
                    else Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }

        findViewById<android.widget.TextView>(R.id.register).setOnClickListener {
            Thread {
                val (token, err) = register(username.text.toString().trim())
                runOnUiThread {
                    if (token != null) {
                        password.setText(token)
                        Toast.makeText(this, "Token generated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, err ?: "Register failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun register(username: String): Pair<String?, String?> {
        if (username.isBlank()) return null to "Username required"
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
            val url = URL("$baseUrl/login")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write("{\"username\":\"$username\",\"token\":\"$token\"}".toByteArray()) }
            conn.responseCode == 200
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
