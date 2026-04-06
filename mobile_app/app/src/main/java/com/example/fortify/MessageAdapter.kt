package com.example.fortify

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent

data class ScannedMessage(val jobId: String, val sender: String, val messageBody: String, val result: String)
class MessageAdapter(private val messageList: MutableList<ScannedMessage>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.messageCard)
        val senderText: TextView = view.findViewById(R.id.senderTextView)
        val bodyText: TextView = view.findViewById(R.id.bodyTextView)
        val resultText: TextView = view.findViewById(R.id.resultTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = messageList[position]
        holder.senderText.text = "From: ${item.sender}"
        holder.bodyText.text = item.messageBody
        holder.resultText.text = item.result
        holder.cardView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MessageDetailsActivity::class.java).apply {
                putExtra("JOB_ID", item.jobId)
                putExtra("SENDER", item.sender)
                putExtra("MESSAGE_BODY", item.messageBody)
                putExtra("RESULT", item.result)
            }
            context.startActivity(intent)
        }

        // Color Logic!
        when (item.result) {
            "Phishing" -> {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Light Red
                holder.resultText.setTextColor(Color.parseColor("#D32F2F")) // Dark Red
            }
            "Safe" -> {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Light Green
                holder.resultText.setTextColor(Color.parseColor("#388E3C")) // Dark Green
            }
            else -> {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5")) // Grey
                holder.resultText.setTextColor(Color.parseColor("#757575"))
            }
        }
    }

    override fun getItemCount() = messageList.size

    fun addMessage(message: ScannedMessage) {
        messageList.add(0, message) // Add to top of list
        notifyItemInserted(0)
    }
}