package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Find the buttons
        val btnNotifs = findViewById<TextView>(R.id.btnNotifications)
        val btnPrivacy = findViewById<TextView>(R.id.btnPrivacy)
        val btnHelp = findViewById<TextView>(R.id.btnHelp)
        val btnDelete = findViewById<TextView>(R.id.btnGoToDelete)

        // 1. Placeholder Buttons (Just show toast for now)
        btnNotifs.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }
        btnPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy clicked", Toast.LENGTH_SHORT).show()
        }
        btnHelp.setOnClickListener {
            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show()
        }

        // 2. The REAL Logic: Open the Delete Account Screen
        btnDelete.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }
    }
}