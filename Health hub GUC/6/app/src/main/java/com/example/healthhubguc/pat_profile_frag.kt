package com.example.healthhubguc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class pat_profile_frag : Fragment() {

    private lateinit var usernameEditText: TextView
    private lateinit var emailTextView: TextView
    private lateinit var passwordTextView: TextView
    private lateinit var userTypeTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var ageEditText: TextView
    private lateinit var saveButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var callBtn: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pat_profile_frag, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        // Initialize UI components
        usernameEditText = rootView.findViewById(R.id.usernameEditText)
        emailTextView = rootView.findViewById(R.id.emailTextView)
        passwordTextView = rootView.findViewById(R.id.passwordTextView)
        userTypeTextView = rootView.findViewById(R.id.userTypeTextView)
        genderTextView = rootView.findViewById(R.id.genderTextView)
        ageEditText = rootView.findViewById(R.id.ageEditText)
        saveButton = rootView.findViewById(R.id.saveButton)
        callBtn = rootView.findViewById(R.id.callButton)

        // Load user data
        loadUserData()

        callBtn.setOnClickListener {
            val intent = Intent(requireContext(), Call::class.java)
            startActivity(intent)
        }

        // Save button click listener
        saveButton.setOnClickListener {
            saveUserData()
        }


        return rootView
    }

    private fun loadUserData() {
        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val username = snapshot.child("name").value.toString()
                val email = snapshot.child("email").value.toString()
                val password = snapshot.child("password").value.toString()
                val userType = snapshot.child("userType").value.toString()
                val gender = snapshot.child("gender").value.toString()
                val age = snapshot.child("age").value.toString()

                // Pre-fill data in UI
                usernameEditText.text = username
                emailTextView.text = "Email: $email"
                passwordTextView.text = "Password: $password"  // Hidden password
                userTypeTextView.text = "User Type: $userType"
                genderTextView.text = "Gender: $gender"
                ageEditText.text = age
            }
        }.addOnFailureListener {
            // Handle errors here (e.g., show a Toast message)
        }
    }

    private fun saveUserData() {
        val newUsername = usernameEditText.text.toString().trim()
        val newAge = ageEditText.text.toString().trim()

        if (newUsername.isEmpty() || newAge.isEmpty()) {
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Update Firebase
        val updates = mapOf(
            "name" to newUsername,
            "age" to newAge
        )

        databaseReference.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
    }
}
