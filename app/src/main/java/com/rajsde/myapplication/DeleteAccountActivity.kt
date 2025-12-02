package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var btnDelete: Button
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        btnDelete = findViewById(R.id.btnDeleteAccount)

        btnDelete.setOnClickListener {
            showReAuthenticateDialog()
        }
    }

    // 1. Ask for Password Again (Security Check)
    private fun showReAuthenticateDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Enter your password to confirm"

        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Please enter your password to permanently delete your account.")
            .setView(input)
            .setPositiveButton("DELETE") { _, _ ->
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    reAuthenticateAndDelete(password)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 2. Verify Password with Firebase
    private fun reAuthenticateAndDelete(password: String) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Create a credential with email/password
            val credential = EmailAuthProvider.getCredential(user.email!!, password)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Password is correct! Now delete data.
                    deleteUserData(user.uid)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 3. Delete from Database first
    private fun deleteUserData(uid: String) {
        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // 4. Finally, Delete the Login Account
                deleteUserAccount()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete data. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    // 4. Delete Auth Account
    private fun deleteUserAccount() {
        auth.currentUser?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(this, "Account Deleted", Toast.LENGTH_LONG).show()

                // Go back to Login Screen
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show()
            }
    }
}