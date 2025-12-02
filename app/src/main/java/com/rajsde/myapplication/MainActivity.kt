package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. CHECK IF USER IS ALREADY LOGGED IN
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is already logged in, skip to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 2. If not logged in, show the landing page
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLoginNavigate)
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val btnSignup = findViewById<Button>(R.id.btnSignupNavigate)
        btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}