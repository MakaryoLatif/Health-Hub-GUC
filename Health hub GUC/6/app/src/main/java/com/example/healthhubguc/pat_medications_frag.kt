package com.example.healthhubguc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.log

class pat_medications_frag : Fragment() {

    private lateinit var medicationsRecyclerView: RecyclerView
    private lateinit var approveButton: Button
    private lateinit var askForApprovalButton: Button
    private lateinit var medicationsAdapter: MedicationsAdapter
    private lateinit var medicationsList: MutableList<Medication>
    private var selectedMedication: Medication? = null
    private val medicationsViewModel: MedicationsViewModel by activityViewModels()
    private lateinit var database: FirebaseDatabase
    private lateinit var medicationsReference: DatabaseReference
    private lateinit var approvalsReference: DatabaseReference

    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown User"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pat_medications_frag, container, false)

        medicationsRecyclerView = rootView.findViewById(R.id.medicationsRecyclerView)
        approveButton = rootView.findViewById(R.id.approveButton)
        askForApprovalButton = rootView.findViewById(R.id.askForApprovalButton)

        database = FirebaseDatabase.getInstance()
        medicationsReference = database.reference.child("medications")
        approvalsReference = database.reference.child("approvals")

        medicationsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        medicationsList = mutableListOf()
        medicationsAdapter = MedicationsAdapter(medicationsList) { medication ->
            onMedicationSelected(medication)
        }
        medicationsRecyclerView.adapter = medicationsAdapter

        loadMedicationsFromDatabase()

        approveButton.setOnClickListener {
            if (selectedMedication?.status == "Approved") {
                addToCart()
            } else {
                Toast.makeText(context, "Please wait for approval.", Toast.LENGTH_SHORT).show()
            }
        }

        askForApprovalButton.setOnClickListener {
            selectedMedication?.let { medication ->
                if (medication.selectedQuantity > 0) {
                    val userReference = database.reference.child("Users").child(currentUserId)
                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userName = snapshot.child("name").value as? String ?: "Unknown"

                            medication.status = "Pending"
                            medication.requestedBy = currentUserId
                            medication.requestedByName = userName
                            medication.requestedQuantity = medication.selectedQuantity // Set chosen quantity

                            approvalsReference.child(medication.id!!).setValue(medication)

                            Toast.makeText(context, "Approval request sent to the doctor.", Toast.LENGTH_SHORT).show()
                            clearSelection()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Failed to fetch user details.", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Please select a quantity to request.", Toast.LENGTH_SHORT).show()
                }
            }
        }



        return rootView
    }

    private fun loadMedicationsFromDatabase() {
        medicationsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicationsList.clear()
                for (medicationSnapshot in snapshot.children) {
                    val medication = medicationSnapshot.getValue(Medication::class.java)
                    if (medication != null) {
                        medication.id = medicationSnapshot.key
                        medicationsList.add(medication)
                    }
                }
                medicationsAdapter.notifyDataSetChanged()

                // Check if the selected medication's status changed
                selectedMedication?.let { medication ->
                    val updatedMedication = medicationsList.find { it.id == medication.id }
                    if (updatedMedication?.status == "Approved") {
                        approveButton.visibility = View.VISIBLE
                        askForApprovalButton.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load medications.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onMedicationSelected(medication: Medication) {
        selectedMedication = medication
        if (medication.approvedFor == currentUserId && medication.status == "Approved") {
            approveButton.visibility = View.VISIBLE // Show Add to Cart button
            askForApprovalButton.visibility = View.GONE
        } else {
            approveButton.visibility = View.GONE
            askForApprovalButton.visibility = View.VISIBLE // Show Ask for Approval button
        }
    }




    private fun clearSelection() {
        approveButton.visibility = View.GONE
        askForApprovalButton.visibility = View.GONE
    }

    private fun addToCart() {
        selectedMedication?.let { medication ->
            if (medication.approvedFor == currentUserId && medication.status == "Approved") {
                val quantityToAdd = medication.selectedQuantity
                if (medication.quantity >= quantityToAdd && medication.quantity > 0) {
                    medication.quantity -= quantityToAdd
                    medication.quantityInCart += quantityToAdd
                    medication.status = "Used" // Mark as used after addition to cart

                    medicationsViewModel.addMedication(medication.copy(selectedQuantity = quantityToAdd))
                    // Update the database
                    medicationsReference.child(medication.id!!).setValue(medication)

                    Toast.makeText(context, "Medication added to cart.${medication.selectedQuantity}", Toast.LENGTH_SHORT).show()
                    clearSelection()
                } else {
                    Toast.makeText(context, "Insufficient stock.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "You are not approved to add this medication to your cart.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
