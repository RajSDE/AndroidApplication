package com.rajsde.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    private lateinit var navView: NavigationView // Make this a class variable
    private var currentUserStatus: String = ""

    private lateinit var rvChats: androidx.recyclerview.widget.RecyclerView
    private lateinit var chatAdapter: HomeChatAdapter
    private val chatList = ArrayList<ChatRoom>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view) // Initialize here
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fabNewChat)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        // REMOVED: updateNavHeader(navView) from here
        // We moved it to onResume so it refreshes when you come back from Profile

        fab.setOnClickListener {
            startActivity(Intent(this, AddChatActivity::class.java))
        }

        // 1. Setup RecyclerView
        rvChats = findViewById(R.id.rvHomeChats)
        rvChats.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        chatAdapter = HomeChatAdapter(chatList)
        rvChats.adapter = chatAdapter

        // 2. Load Chats
        loadActiveChats()
    }

    private fun loadActiveChats() {
        val myUid = auth.currentUser?.uid ?: return

        // Query: Find all chats where "users" array contains MY ID
        FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("users", myUid)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    chatList.clear()
                    for (doc in value.documents) {
                        // Create ChatRoom object manually from document
                        val users = doc.get("users") as? List<String> ?: emptyList()
                        val lastMsg = doc.getString("lastMessage") ?: ""

                        // Only add if data is valid
                        if (users.isNotEmpty()) {
                            chatList.add(ChatRoom(doc.id, users, lastMsg))
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                }
            }
    }

    // 1. Add this function to auto-refresh data
    override fun onResume() {
        super.onResume()
        updateNavHeader()
    }


    private fun updateNavHeader() {
        val headerView = navView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvNavName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvNavEmail)
        val tvInitials = headerView.findViewById<TextView>(R.id.tvNavInitials)
        val tvStatus = headerView.findViewById<TextView>(R.id.tvNavStatus) // NEW

        val user = auth.currentUser

        if (user != null) {
            tvEmail.text = user.email

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"

                        val status = document.getString("status")

                        if (!status.isNullOrEmpty()) {
                            // If status exists, show it with quotes
                            tvStatus.text = "\"$status\""
                            currentUserStatus = status
                        } else {
                            // If no status, show NOTHING (Empty space)
                            tvStatus.text = ""
                            currentUserStatus = ""
                        }
                        tvName.text = name

                        // Calculate Initials
                        val words = name.trim().split(" ")
                        var initials = ""
                        if (words.isNotEmpty()) {
                            initials += words[0].first().uppercase()
                            if (words.size > 1) {
                                initials += words[1].first().uppercase()
                            }
                        }
                        tvInitials.text = initials
                    }
                }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            R.id.nav_status -> {
                // Open the Popup Dialog
                showStatusDialog()
            }

            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.nav_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showStatusDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_update_status, null)

        // Find views in the custom layout
        val etStatus = view.findViewById<TextInputEditText>(R.id.etDialogStatus)
        val btnUpdate = view.findViewById<Button>(R.id.btnDialogUpdate)
        val btnCancel = view.findViewById<TextView>(R.id.btnDialogCancel)
        val btnDelete = view.findViewById<TextView>(R.id.btnDialogDelete)

        // PRE-POPULATE: Set the text to the current status
        etStatus.setText(currentUserStatus)

        builder.setView(view)
        val dialog = builder.create()

        // Make background transparent so our rounded corners show
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnUpdate.setOnClickListener {
            val newStatus = etStatus.text.toString()
            if (newStatus.isNotEmpty()) {
                saveStatusToFirebase(newStatus)
                dialog.dismiss()
            }
        }

        // DELETE Logic: Set status back to default "Available"
        btnDelete.setOnClickListener {
            saveStatusToFirebase("") // Save empty string to clear it
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveStatusToFirebase(status: String) {
        val user = auth.currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(user.uid)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Status Updated!", Toast.LENGTH_SHORT).show()
                // Update the header immediately without restarting app
                updateNavHeader()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}