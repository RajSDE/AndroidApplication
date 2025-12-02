package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_form)

        auth = FirebaseAuth.getInstance()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        // If you have a "Sign Up" text on login screen to go to signup
        val tvGoToSignup = findViewById<TextView>(R.id.tvGoToSignup) // Make sure this ID matches

        tvGoToSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            // NOTE: Ensure your XML ID is correct (etPhoneNumber or etEmail)
            val email = findViewById<EditText>(R.id.etPhoneNumber).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // CRITICAL STEP: Check if verified
                        if (user != null && user.isEmailVerified) {
                            // YES: Go to Home
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // NO: Show Error
                            Toast.makeText(this, "Email is not verified yet!", Toast.LENGTH_LONG).show()
                            auth.signOut() // Log them out immediately
                        }
                    } else {
                        Toast.makeText(this, "Login Failed. Check email/password.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}