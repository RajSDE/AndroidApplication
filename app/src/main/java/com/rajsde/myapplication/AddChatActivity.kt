package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

        val etEmail = findViewById<EditText>(R.id.etSearchEmail)
        val btnAdd = findViewById<Button>(R.id.btnAddFriend)
        val db = FirebaseFirestore.getInstance()

        btnAdd.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter an email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // QUERY: Search the database for this email
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    } else {
                        // User Found!
                        for (document in documents) {
                            val friendName = document.getString("name")
                            val friendUid = document.getString("uid")

                            // OPEN CHAT ACTIVITY
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.putExtra("friendName", friendName)
                            intent.putExtra("friendUid", friendUid)
                            startActivity(intent)
                            finish() // Close the search screen
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error finding user", Toast.LENGTH_SHORT).show()
                }
        }
    }
}