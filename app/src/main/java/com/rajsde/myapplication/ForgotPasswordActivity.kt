package com.rajsde.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<EditText>(R.id.etResetEmail)
        val btnReset = findViewById<Button>(R.id.btnResetPassword)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)
        val auth = FirebaseAuth.getInstance()

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // FIREBASE MAGIC LINE
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
                    finish() // Close screen and go back to login
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvBack.setOnClickListener {
            finish()
        }
    }
}