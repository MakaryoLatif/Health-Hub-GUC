package com.example.healthhubguc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhubguc.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Cart : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var userId: String
    private val medicationsViewModel: MedicationsViewModel by activityViewModels()
    private lateinit var cartAdapter: CartAdapter
    private lateinit var medicationsReference: DatabaseReference
    private val selectedMedications = mutableListOf<Medication>() // Store selected meds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        // Get the logged-in user's UID
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Reference to the current user's cart in Firebase
        medicationsReference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userId)
            .child("Cart")

        medicationsReference = FirebaseDatabase.getInstance().reference.child("medications")
        medicationsViewModel.loadCartFromFirebase()

        medicationsViewModel.loadCartFromSharedPreferences()

        // Set up RecyclerView
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(context)
        medicationsViewModel.selectedMedications.observe(viewLifecycleOwner) { meds ->
            cartAdapter = CartAdapter(meds.toMutableList()) { medication ->
                // Delete item from the cart when delete button is clicked
                deleteMedication(medication)
            }
            binding.cartRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.cartRecyclerView.adapter = cartAdapter
            cartAdapter.notifyDataSetChanged()

            // Update total price when medications list is updated
            updateTotalPrice(meds)
        }

        // Set up confirm button
        binding.confirmButton.setOnClickListener {
            confirmItems()
        }

        return binding.root
    }

    private fun loadSelectedMedications() {
        // Example of loading from arguments
        arguments?.getParcelable<Medication>("selected_medication")?.let { medication ->
            selectedMedications.add(medication)
            cartAdapter.notifyDataSetChanged()
        }
    }

    private fun updateTotalPrice(medications: List<Medication>) {
        var totalPrice = 0.0
        for (medication in medications) {
            totalPrice += medication.price * medication.selectedQuantity
        }
        binding.totalPriceTextView.text = "Total Price: $${"%.2f".format(totalPrice)}"
    }

    private fun deleteMedication(medication: Medication) {
        val medicationsReference = FirebaseDatabase.getInstance().reference.child("medications")
        medication.quantity += medication.quantityInCart  // Restore quantity
        medication.quantityInCart = 0 // Remove item from cart
        medicationsViewModel.removeMedication(medication) // Remove from ViewModel

        // Update Firebase to reflect that it's no longer in the cart
        medicationsReference.child(medication.id!!).setValue(medication)

        // Notify the user
        Toast.makeText(context, "${medication.name} has been removed from the cart", Toast.LENGTH_SHORT).show()
    }
    private fun confirmItems() {
        if (medicationsViewModel.selectedMedications.value.isNullOrEmpty()) {
            Toast.makeText(context, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }
        medicationsViewModel.selectedMedications.value?.forEach { medication ->
            medication.quantityInCart = 0
            medicationsReference.child(medication.id!!).setValue(medication)
        }

        // Clear the cart in ViewModel
        medicationsViewModel.clearCart()

        // Show success toast
        Toast.makeText(context, "Items purchased successfully!", Toast.LENGTH_SHORT).show()

        // Update UI (cart is now empty)
        cartAdapter.notifyDataSetChanged()
        updateTotalPrice(emptyList())
    }
}


