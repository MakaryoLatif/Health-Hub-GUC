package com.example.healthhubguc

// MedicationsViewModel.kt
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MedicationsViewModel : ViewModel() {
    private val _selectedMedications = MutableLiveData<MutableList<Medication>>(mutableListOf())
    val selectedMedications: LiveData<MutableList<Medication>> get() = _selectedMedications
    private lateinit var medicationsReference: DatabaseReference
    private val sharedPreferences = App.getContext().getSharedPreferences("CartData", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val cartReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Cart")

    fun addMedication(medication: Medication) {
        val existingMed = _selectedMedications.value?.find { it.id == medication.id }
        if (existingMed != null) {
            existingMed.selectedQuantity += medication.selectedQuantity
        } else {
            _selectedMedications.value?.add(medication)
        }
        _selectedMedications.value = _selectedMedications.value  // Trigger observers
        saveCartToFirebase()
    }
    fun removeMedication(medication: Medication) {
        _selectedMedications.value?.removeIf { it.id == medication.id }
        _selectedMedications.value = _selectedMedications.value  // Trigger observers
        saveCartToFirebase()
    }

    fun clearCart() {
        _selectedMedications.value?.clear()
        _selectedMedications.value = _selectedMedications.value // Notify observers
        saveCartToFirebase()
    }

    private fun saveCartToFirebase() {
        cartReference.setValue(_selectedMedications.value) // Save cart in Firebase
            .addOnFailureListener { e ->
                println("Error saving cart: ${e.message}")
            }
    }
    fun loadCartFromFirebase() {
        cartReference.get().addOnSuccessListener { snapshot ->
            val cartItems = snapshot.children.mapNotNull { it.getValue(Medication::class.java) }
            _selectedMedications.value = cartItems.toMutableList()
        }.addOnFailureListener { e ->
            println("Error loading cart: ${e.message}")
        }
    }

    // Optionally, maintain shared preferences as a fallback
    private fun saveCartToSharedPreferences() {
        val cartJson = gson.toJson(_selectedMedications.value)
        sharedPreferences.edit().putString("cart_items", cartJson).apply()
    }

    fun loadCartFromSharedPreferences() {
        val cartJson = sharedPreferences.getString("cart_items", "[]")
        val cartItems: List<Medication> = gson.fromJson(cartJson, object : TypeToken<List<Medication>>() {}.type)
        _selectedMedications.value = cartItems.toMutableList()
    }
}
