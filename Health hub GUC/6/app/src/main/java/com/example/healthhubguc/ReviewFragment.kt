package com.example.healthhubguc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhubguc.Consultation
import com.example.healthhubguc.Medication
import com.example.healthhubguc.R
import com.example.healthhubguc.ReviewAdapter
import com.google.firebase.database.*
class ReviewFragment : Fragment(R.layout.fragment_review) {

    private val approvalList = mutableListOf<Medication>()
    private val consultationList = mutableListOf<Consultation>()
    private lateinit var consultationsAdapter: ReviewAdapter
    private lateinit var approvalsAdapter: ReviewAdapter
    private lateinit var approvalsReference: DatabaseReference
    private lateinit var consultationsReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_review, container, false)

        // Firebase references
        approvalsReference = FirebaseDatabase.getInstance().reference.child("approvals")
        consultationsReference = FirebaseDatabase.getInstance().reference.child("consultations")

        // Initialize the adapters
        consultationsAdapter = ReviewAdapter(consultationList) { item, action ->
            if (item is Consultation) {
                handleConsultationAction(item, action)
            }
        }
        approvalsAdapter = ReviewAdapter(approvalList) { item, action ->
            if (item is Medication) {
                handleApprovalAction(item, action)
            }
        }

        // Set up RecyclerViews
        val consultationsRecyclerView = rootView.findViewById<RecyclerView>(R.id.consultationsRecyclerView)
        consultationsRecyclerView.layoutManager = LinearLayoutManager(context)
        consultationsRecyclerView.adapter = consultationsAdapter

        val approvalsRecyclerView = rootView.findViewById<RecyclerView>(R.id.approvalsRecyclerView)
        approvalsRecyclerView.layoutManager = LinearLayoutManager(context)
        approvalsRecyclerView.adapter = approvalsAdapter

        // Load data
        loadConsultationRequests()
        loadApprovalRequests()

        return rootView
    }

    private fun loadConsultationRequests() {
        consultationsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                consultationList.clear()
                for (data in snapshot.children) {
                    val consultation = data.getValue(Consultation::class.java)
                    if (consultation != null && consultation.status != "Replied") consultationList.add(consultation)
                }
                consultationsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadApprovalRequests() {
        approvalsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                approvalList.clear()
                for (data in snapshot.children) {
                    val medication = data.getValue(Medication::class.java)
                    if (medication != null) approvalList.add(medication)
                }
                approvalsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleApprovalAction(medication: Medication, action: String) {
        if (action == "approve") {
            val updates = mapOf(
                "status" to "Approved",
                "approvedFor" to medication.requestedBy, // Grant approval only to the requesting user
                "selectedQuantity" to medication.selectedQuantity
            )
            FirebaseDatabase.getInstance().reference.child("medications").child(medication.id!!)
                .updateChildren(updates)
                .addOnSuccessListener {
                    approvalsReference.child(medication.id!!).removeValue() // Remove from approvals list
                    approvalList.remove(medication)
                    approvalsAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "Medication approved and updated.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Deny request
            approvalsReference.child(medication.id!!).removeValue()
                .addOnSuccessListener {
                    approvalList.remove(medication)
                    approvalsAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "Medication denied.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun handleConsultationAction(consultation: Consultation, replyMessage: String) {
        sendReplyToConsultation(consultation, replyMessage)
    }

    private fun sendReplyToConsultation(consultation: Consultation, replyMessage: String) {
        val consultationPath = FirebaseDatabase.getInstance().reference.child("consultations").child(consultation.id!!)
        val updates = mapOf(
            "replyMessage" to replyMessage,
            "status" to "Replied"
        )


        consultationPath.updateChildren(updates)
            .addOnSuccessListener {
                consultationList.remove(consultation)
                consultationsAdapter.notifyDataSetChanged()
                Toast.makeText(context, "Reply sent to patient.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error sending reply: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}




