package com.example.healthhubguc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhubguc.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ConsultationsAdapter(private val consultations: List<Consultation>) :
    RecyclerView.Adapter<ConsultationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description = view.findViewById<TextView>(R.id.consultationDescriptionTextView)
        val status = view.findViewById<TextView>(R.id.consultationStatusTextView)
        val reply = view.findViewById<TextView>(R.id.consultationReplyTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_consultations_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val consultation = consultations[position]
        holder.description.text = consultation.description

        val consultationRef = FirebaseDatabase.getInstance().reference.child("consultations").child(consultation.id!!)

        consultationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedConsultation = snapshot.getValue(Consultation::class.java)
                if (fetchedConsultation != null) {
                    // If the replyMessage is available, update the TextView
                    holder.status.text = "Status: ${fetchedConsultation.status}"
                    val replyMessage = fetchedConsultation.replyMessage
                    if (!replyMessage.isNullOrEmpty()) {
                        holder.reply.text = "Doctor's Reply: $replyMessage"
                        holder.reply.visibility = View.VISIBLE
                    } else {
                        holder.reply.text = "Doctor's Reply: No reply yet"
                        holder.reply.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if the Firebase query is cancelled
                Toast.makeText(holder.itemView.context, "Error fetching reply message.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount(): Int = consultations.size
}
