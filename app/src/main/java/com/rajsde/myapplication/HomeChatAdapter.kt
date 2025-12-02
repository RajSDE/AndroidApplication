package com.rajsde.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class for the Chat
data class ChatRoom(
    val id: String = "",
    val users: List<String> = emptyList(),
    val lastMessage: String = ""
)

class HomeChatAdapter(private val chatList: List<ChatRoom>) :
    RecyclerView.Adapter<HomeChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRowName)
        val tvMsg: TextView = itemView.findViewById(R.id.tvRowLastMessage)
        val tvInitials: TextView = itemView.findViewById(R.id.tvRowInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Find the other person
        val friendUid = chat.users.find { it != myUid } ?: return

        // 2. Fetch Friend's Details
        FirebaseFirestore.getInstance().collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    holder.tvName.text = name

                    // --- ENHANCED LOGIC WITH STYLING ---
                    val status = document.getString("status")

                    if (!status.isNullOrEmpty()) {
                        // Priority 1: Show Status -> Add Quotes + Italic
                        holder.tvMsg.text = "\"$status\""
                        holder.tvMsg.setTypeface(null, android.graphics.Typeface.ITALIC)
                    } else {
                        // Priority 2: Show Last Message -> Normal Text
                        holder.tvMsg.text = chat.lastMessage
                        holder.tvMsg.setTypeface(null, android.graphics.Typeface.NORMAL)
                    }
                    // -----------------------------------
                    if (name.isNotEmpty()) {
                        val words = name.trim().split(" ")
                        var initials = ""
                        if (words.isNotEmpty()) {
                            // First letter of first name
                            initials += words[0].first().uppercase()
                            // First letter of last name (if it exists)
                            if (words.size > 1) {
                                initials += words[1].first().uppercase()
                            }
                        }
                        holder.tvInitials.text = initials
                    }

                }
            }

        // 3. Handle Click
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("friendName", holder.tvName.text.toString())
            intent.putExtra("friendUid", friendUid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chatList.size
}