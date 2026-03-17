package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R
import com.signalix.app.data.Prefs
import com.signalix.app.data.Supabase
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private val baseUrl: String
        get() = Prefs.getServer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        applyInsets(findViewById(android.R.id.content))
        applyFullscreen(this)

        val username = findViewById<EditText>(R.id.username)
        val tokenView = findViewById<TextView>(R.id.token)
        val generate = findViewById<android.widget.Button>(R.id.generate)
        val copy = findViewById<android.widget.ImageButton>(R.id.copy)

        generate.isEnabled = false
        username.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val u = s?.toString()?.trim().orEmpty()
                generate.isEnabled = u.length in 3..20 && u.matches(Regex("[a-zA-Z0-9_]+"))
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        generate.setOnClickListener {
            val u = username.text.toString().trim()
            if (!(u.length in 3..20 && u.matches(Regex("[a-zA-Z0-9_]+")))) {
                username.error = "Use 3–20 letters/numbers/underscore"
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

        copy.setOnClickListener {
            val text = tokenView.text.toString()
            if (text.isNotBlank() && !text.contains("Token will")) {
                val clipboard = getSystemService(android.content.ClipboardManager::class.java)
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("token", text))
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<android.widget.Button>(R.id.continue_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun register(username: String): Pair<String?, String?> {
        return try {
            val token = UUID.randomUUID().toString()
            val url = URL("${baseUrl}/rest/v1/users")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("apikey", Supabase.ANON)
            conn.setRequestProperty("Authorization", "Bearer ${Supabase.ANON}")
            conn.setRequestProperty("Prefer", "return=representation")
            conn.doOutput = true
            conn.outputStream.use {
                it.write("{\"username\":\"$username\",\"token\":\"$token\"}".toByteArray())
            }
            val code = conn.responseCode
            if (code == 409 || code == 400) return null to "Username already taken"
            if (code !in 200..299) return null to "Server error ($code)"
            token to null
        } catch (e: Exception) {
            null to "Network error"
        }
    }
}
