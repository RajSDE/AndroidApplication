package com.rajsde.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var tvInitials: TextView
    private lateinit var btnSave: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etName = findViewById(R.id.etProfileName)
        etEmail = findViewById(R.id.etProfileEmail)
        tvInitials = findViewById(R.id.tvProfileInitials)
        btnSave = findViewById(R.id.btnSaveProfile)

        loadUserData()

        btnSave.setOnClickListener {
            updateProfileName()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            etEmail.setText(user.email)

            // Try to get Name from Database
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"
                        etName.setText(name)
                        setInitials(name)
                    } else if (!user.displayName.isNullOrEmpty()) {
                        // Fallback to Auth Name if DB is empty
                        etName.setText(user.displayName)
                        setInitials(user.displayName!!)
                    }
                }
        }
    }

    // Logic to turn "Raj Kumar" into "RK"
    private fun setInitials(name: String) {
        if (name.isNotEmpty()) {
            val words = name.trim().split(" ")
            var initials = ""
            if (words.isNotEmpty()) {
                initials += words[0].first().uppercase() // First letter of First Name
                if (words.size > 1) {
                    initials += words[1].first().uppercase() // First letter of Last Name
                }
            }
            tvInitials.text = initials
        } else {
            tvInitials.text = "--"
        }
    }

    private fun updateProfileName() {
        val user = auth.currentUser ?: return
        val newName = etName.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Update Auth Profile (Internal Firebase Cache)
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        user.updateProfile(profileUpdates)

        // 2. Update Firestore Database (Publicly searchable)
        db.collection("users").document(user.uid)
            .update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show()
                setInitials(newName) // Update the circle immediately
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}