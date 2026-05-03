package com.example.healthhubguc.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthhubguc.Call
import com.example.healthhubguc.R
import com.example.healthhubguc.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DocProfFragment : Fragment(R.layout.fragment_doc_prof) {

    private lateinit var nameEdt: EditText
    private lateinit var ageEdt: EditText
    private lateinit var emailEdt: TextView // Change email to TextView
    private lateinit var passwordTextView: TextView
    private lateinit var userTypeTxt: TextView
    private lateinit var genderTxt: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnUpdate: Button
    private lateinit var callBtn: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize UI elements
        nameEdt = view.findViewById(R.id.edt_name)
        ageEdt = view.findViewById(R.id.edt_age)
        emailEdt = view.findViewById(R.id.txt_email) // Changed to TextView
        passwordTextView = view.findViewById(R.id.passwordTextView2)
        userTypeTxt = view.findViewById(R.id.txt_user_type)
        genderTxt = view.findViewById(R.id.txt_gender)
        btnEdit = view.findViewById(R.id.btn_edit)
        btnUpdate = view.findViewById(R.id.btn_update)
        callBtn = view.findViewById(R.id.Calldoc)

        loadUserProfile()

        btnEdit.setOnClickListener {
            enableEditing(true)
            btnEdit.visibility = View.GONE
            btnUpdate.visibility = View.VISIBLE
        }

        callBtn.setOnClickListener {
            val intent = Intent(requireContext(), Call::class.java)
            startActivity(intent)
        }

        btnUpdate.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("Users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                nameEdt.setText(it.name)
                ageEdt.setText(it.age)
                emailEdt.text = it.email // Set email to TextView
                passwordTextView.text = it.password
                userTypeTxt.text = it.userType
                genderTxt.text = it.gender
            }
        }
    }

    private fun enableEditing(enabled: Boolean) {
        nameEdt.isEnabled = enabled
        ageEdt.isEnabled = enabled
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("Users").child(userId)

        val updatedName = nameEdt.text.toString()
        val updatedAge = ageEdt.text.toString()

        val updates = mapOf<String, Any>(
            "name" to updatedName,
            "age" to updatedAge
        )

        userRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                enableEditing(false)
                btnEdit.visibility = View.VISIBLE
                btnUpdate.visibility = View.GONE
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
