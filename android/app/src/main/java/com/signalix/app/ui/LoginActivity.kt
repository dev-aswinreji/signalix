package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {
    private val baseUrl = "http://10.0.2.2:3002"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)

        findViewById<android.widget.Button>(R.id.login).setOnClickListener {
            Thread {
                val ok = login(username.text.toString(), password.text.toString())
                runOnUiThread {
                    if (ok) startActivity(Intent(this, ChatListActivity::class.java))
                    else Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }

        findViewById<android.widget.TextView>(R.id.register).setOnClickListener {
            Thread {
                val token = register(username.text.toString())
                runOnUiThread {
                    if (token != null) {
                        password.setText(token)
                        Toast.makeText(this, "Token generated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Register failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun register(username: String): String? {
        if (username.isBlank()) return null
        return try {
            val url = URL("$baseUrl/register")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write("{\"username\":\"$username\"}".toByteArray()) }
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().readText()
            val token = Regex("\"token\"\s*:\"([^\"]+)\"").find(body)?.groupValues?.get(1)
            token
        } catch (e: Exception) {
            null
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
}
