package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_form)

        auth = FirebaseAuth.getInstance()

        val btnSignup = findViewById<Button>(R.id.btnCreateAccount)
        val tvLogin = findViewById<TextView>(R.id.tvGoToLogin)

        tvLogin.setOnClickListener { finish() }

        btnSignup.setOnClickListener { signUpUser() }
    }

    private fun signUpUser() {
        val name = findViewById<EditText>(R.id.etName).text.toString()
        val email = findViewById<EditText>(R.id.etEmail).text.toString()
        val password = findViewById<EditText>(R.id.etSignupPassword).text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid // Get the unique ID created by Firebase

                    // --- NEW DATABASE LOGIC STARTS HERE ---
                    val db = FirebaseFirestore.getInstance()

                    // Create a Map of user data
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email
                    )

                    // Save to "users" collection
                    if (uid != null) {
                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                // Only send verification/finish after DB save is successful
                                user.sendEmailVerification()
                                showVerificationDialog(email)
                                auth.signOut()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                    // --- NEW DATABASE LOGIC ENDS HERE ---

                } else {
                    Toast.makeText(
                        this,
                        "Signup Failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showVerificationDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verification Sent")
        builder.setMessage("We have sent a verification link to $email.\n\nPlease check your inbox and click the link to verify your account before logging in.")
        builder.setPositiveButton("OK") { dialog, _ ->
            // Close Signup and go back to Login
            val intent = Intent(this, LoginActivity::class.java)
            // This clears the history so they can't go back to signup
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        builder.show()
    }
}