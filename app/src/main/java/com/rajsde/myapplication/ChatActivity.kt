package com.rajsde.myapplication

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = ArrayList<Message>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Variables to track who we are talking to
    private var chatRoomId: String? = null
    private var friendId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 1. Get Details from Intent (Passed from AddChatActivity)
        val friendName = intent.getStringExtra("friendName")
        friendId = intent.getStringExtra("friendUid")
        val myUid = auth.currentUser?.uid ?: return

        // Setup Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.chatToolbar)
        toolbar.title = friendName ?: "Chat"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // 2. Create Unique Chat Room ID
        // Logic: Sort UIDs alphabetically so "A_B" is always the same as "B_A"
        chatRoomId = if (myUid < friendId!!) {
            "${myUid}_${friendId}"
        } else {
            "${friendId}_${myUid}"
        }

        // Setup Views
        rvMessages = findViewById(R.id.rvChatMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Setup Recycler View
        messageAdapter = MessageAdapter(messageList, myUid)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = messageAdapter

        // 3. Listen for Messages (Real-time!)
        listenForMessages()

        // 4. Send Message Logic
        btnSend.setOnClickListener {
            val text = etMessage.text.toString()
            if (text.isNotEmpty()) {
                sendMessage(text, myUid)
            }
        }
    }

    private fun sendMessage(text: String, myUid: String) {
        val messageMap = hashMapOf(
            "senderId" to myUid,
            "text" to text,
            "timestamp" to Timestamp.now()
        )

        // Add to sub-collection 'messages'
        db.collection("chats").document(chatRoomId!!)
            .collection("messages")
            .add(messageMap)
            .addOnSuccessListener {
                etMessage.text.clear()
            }

        // Optional: Update last message for the Home Screen list
        val lastMsgMap = hashMapOf(
            "users" to listOf(myUid, friendId),
            "lastMessage" to text,
            "lastUpdated" to Timestamp.now()
        )
        db.collection("chats").document(chatRoomId!!).set(lastMsgMap)
    }

    private fun listenForMessages() {
        db.collection("chats").document(chatRoomId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    messageList.clear()
                    for (doc in value.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null) messageList.add(msg)
                    }
                    messageAdapter.notifyDataSetChanged()
                    // Scroll to bottom
                    if (messageList.isNotEmpty()) {
                        rvMessages.scrollToPosition(messageList.size - 1)
                    }
                }
            }
    }
}

// Data Class
data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)

// Simple Adapter Class inside the same file for ease
class MessageAdapter(private val messageList: ArrayList<Message>, private val myUid: String) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tvMessage)
        val container: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.text.text = message.text

        if (message.senderId == myUid) {
            // MY MESSAGE (Right Side, Purple Bubble)
            holder.container.gravity = Gravity.END
            holder.text.setBackgroundResource(R.drawable.bg_bubble_sent) // Use new drawable
            holder.text.setTextColor(0xFFFFFFFF.toInt()) // White text
        } else {
            // FRIEND'S MESSAGE (Left Side, White Bubble)
            holder.container.gravity = Gravity.START
            holder.text.setBackgroundResource(R.drawable.bg_bubble_received) // Use new drawable
            holder.text.setTextColor(0xFF000000.toInt()) // Black text
        }
    }

    override fun getItemCount(): Int = messageList.size
}