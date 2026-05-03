package com.example.healthhubguc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthhubguc.databinding.FragmentMedicalConsultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Medical_consult : Fragment() {

    private lateinit var binding: FragmentMedicalConsultBinding
    private lateinit var consultationsReference: DatabaseReference
    private lateinit var globalConsultationsReference: DatabaseReference
    private lateinit var userConsultationsReference: DatabaseReference
    private lateinit var userId: String
    private val consultationsList = mutableListOf<Consultation>()
    private lateinit var consultationsAdapter: ConsultationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalConsultBinding.inflate(inflater, container, false)

        // Get the current user's ID
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // References for database
        globalConsultationsReference = FirebaseDatabase.getInstance().reference.child("consultations")
        consultationsReference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userId)
            .child("consultations")
        userConsultationsReference = consultationsReference

        // Set up RecyclerView
        setupRecyclerView()

        // Load consultations
        loadConsultations()

        // Handle consultation request
        binding.requestConsultationButton.setOnClickListener {
            val issueDescription = binding.consultationDescriptionEditText.text.toString().trim()
            if (issueDescription.isNotEmpty()) {
                requestConsultation(issueDescription)
            } else {
                Toast.makeText(context, "Please describe your issue", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.previousConsultationsRecyclerView.layoutManager = LinearLayoutManager(context)
        consultationsAdapter = ConsultationsAdapter(consultationsList)
        binding.previousConsultationsRecyclerView.adapter = consultationsAdapter
    }

    private fun requestConsultation(description: String) {
        val consultationId = globalConsultationsReference.push().key ?: return
        val consultation = Consultation(
            id = consultationId,
            description = description,
            status = "Pending",
            patientId = userId
        )

        // Add to global consultations
        globalConsultationsReference.child(consultationId).setValue(consultation)
            .addOnSuccessListener {
                // Add to user-specific consultations
                userConsultationsReference.child(consultationId).setValue(consultation)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Consultation request sent!", Toast.LENGTH_SHORT).show()
                        binding.consultationDescriptionEditText.text.clear()
                        loadConsultations()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error saving to user node: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving to global node: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadConsultations() {
        userConsultationsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                consultationsList.clear()
                for (data in snapshot.children) {
                    val consultation = data.getValue(Consultation::class.java)
                    consultation?.let { consultationsList.add(it) }
                }
                consultationsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
