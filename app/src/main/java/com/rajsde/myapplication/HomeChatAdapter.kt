package com.rajsde.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

// 1. UPDATED Data Class (Added timestamp)
data class ChatRoom(
    val id: String = "",
    val users: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastUpdated: Timestamp? = null // NEW FIELD
)

class HomeChatAdapter(private val chatList: List<ChatRoom>) :
    RecyclerView.Adapter<HomeChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRowName)
        val tvMsg: TextView = itemView.findViewById(R.id.tvRowLastMessage)
        val tvInitials: TextView = itemView.findViewById(R.id.tvRowInitials)
        val tvTime: TextView = itemView.findViewById(R.id.tvRowTime) // NEW VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val friendUid = chat.users.find { it != myUid } ?: return

        // 2. NEW: Format and Set Time
        if (chat.lastUpdated != null) {
            val date = chat.lastUpdated.toDate()
            // Format: 10:30 AM
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            holder.tvTime.text = sdf.format(date)
        } else {
            holder.tvTime.text = ""
        }

        // 3. Fetch Friend Details (Same as before)
        FirebaseFirestore.getInstance().collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    holder.tvName.text = name

                    val status = document.getString("status")
                    if (!status.isNullOrEmpty()) {
                        holder.tvMsg.text = "\"$status\""
                        holder.tvMsg.setTypeface(null, android.graphics.Typeface.ITALIC)
                    } else {
                        holder.tvMsg.text = chat.lastMessage
                        holder.tvMsg.setTypeface(null, android.graphics.Typeface.NORMAL)
                    }

                    if (name.isNotEmpty()) {
                        val words = name.trim().split(" ")
                        var initials = ""
                        if (words.isNotEmpty()) {
                            initials += words[0].first().uppercase()
                            if (words.size > 1) {
                                initials += words[1].first().uppercase()
                            }
                        }
                        holder.tvInitials.text = initials
                    }
                }
            }

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