package com.signalix.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.signalix.app.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<android.widget.Button>(R.id.login).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        findViewById<android.widget.TextView>(R.id.register).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
    }
}
