package com.example.healthhubguc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView


// Updated adapter to handle both consultations and medications
class ReviewAdapter(
    private val items: List<Any>,  // List of either Consultations or Medications
    private val onActionClick: (Any, String) -> Unit  // Callback for action (approve/deny/reply)
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Common UI elements
        val descriptionTextView: TextView = view.findViewById(R.id.medicationNameTextView)
        val statusTextView: TextView = view.findViewById(R.id.medicationStatusTextView)
        val approveButton: Button = view.findViewById(R.id.approveButton)
        val denyButton: Button = view.findViewById(R.id.denyButton)
        val replyButton: Button = view.findViewById(R.id.replyButton) // For consultations
        val replyEditText: TextView = view.findViewById(R.id.replyEditText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_review_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item is Medication) {
            // For medications, show approve/deny buttons
            holder.descriptionTextView.text = item.name
            holder.statusTextView.text = "Status: ${item.status}\nRequested by: ${item.requestedByName ?: "Unknown"}\nRequested Quantity: ${item.requestedQuantity}"
            holder.approveButton.visibility = View.VISIBLE
            holder.denyButton.visibility = View.VISIBLE
            holder.replyButton.visibility = View.GONE
            holder.replyEditText.visibility = View.GONE

            holder.approveButton.setOnClickListener { onActionClick(item, "approve") }
            holder.denyButton.setOnClickListener { onActionClick(item, "deny") }
        } else if (item is Consultation) {
            // For consultations, show reply button instead
            holder.descriptionTextView.text = item.description
            holder.statusTextView.text = "Status: ${item.status}"
            holder.approveButton.visibility = View.GONE
            holder.denyButton.visibility = View.GONE
            holder.replyButton.visibility = View.VISIBLE
            holder.replyEditText.visibility = View.VISIBLE

            holder.replyButton.setOnClickListener {
                // Capture the reply message entered by the doctor
                val replyMessage = holder.replyEditText.text.toString().trim()

                if (replyMessage.isNotEmpty()) {
                    // Call the callback function to send the reply
                    onActionClick(item, replyMessage)
                } else {
                    Toast.makeText(holder.itemView.context, "Please enter a reply message.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
