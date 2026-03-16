package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {
    private val baseUrl: String
        get() = Prefs.getServer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val username = findViewById<EditText>(R.id.username)
        val tokenView = findViewById<TextView>(R.id.token)

        findViewById<android.widget.Button>(R.id.generate).setOnClickListener {
            val u = username.text.toString().trim()
            if (u.isBlank()) {
                username.error = "Username required"
                return@setOnClickListener
            }
            Thread {
                val (token, err) = register(u)
                runOnUiThread {
                    if (token != null) {
                        tokenView.text = token
                        Toast.makeText(this, "Token generated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, err ?: "Register failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        findViewById<android.widget.Button>(R.id.continue_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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
}
