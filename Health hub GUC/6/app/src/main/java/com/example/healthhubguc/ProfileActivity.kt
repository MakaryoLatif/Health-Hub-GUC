package com.example.healthhubguc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var map_btn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firstNameEditText = findViewById(R.id.first_name)
        lastNameEditText = findViewById(R.id.last_name)
        ageEditText = findViewById(R.id.age)
        bioEditText = findViewById(R.id.bio)
        editButton = findViewById(R.id.edit_button)
        saveButton = findViewById(R.id.save_button)
        map_btn = findViewById(R.id.map_btn)
        // Load saved data (if any)
        loadProfileData()

        // Initially disable editing
        setEditTextsEnabled(false)

        // Edit button listener
        editButton.setOnClickListener {
            // Enable editing when "Edit" is clicked
            setEditTextsEnabled(true)
        }

        // Save button listener
        saveButton.setOnClickListener {
            // Save the profile data
            saveProfileData()

            // Disable editing after saving
            setEditTextsEnabled(false)
        }
        map_btn.setOnClickListener {
            val intent = Intent(this, mapActivity::class.java)
            startActivity(intent)}
    }
    // Function to enable or disable EditTexts
    private fun setEditTextsEnabled(isEnabled: Boolean) {
        firstNameEditText.isEnabled = isEnabled
        lastNameEditText.isEnabled = isEnabled
        ageEditText.isEnabled = isEnabled
        bioEditText.isEnabled = isEnabled
    }

    // Function to save data to SharedPreferences
    private fun saveProfileData() {
        val sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Save data to SharedPreferences
        editor.putString("firstName", firstNameEditText.text.toString())
        editor.putString("lastName", lastNameEditText.text.toString())
        editor.putString("age", ageEditText.text.toString())
        editor.putString("bio", bioEditText.text.toString())

        // Commit changes
        editor.apply()
    }

    // Function to load saved data from SharedPreferences
    private fun loadProfileData() {
        val sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Retrieve saved data or set default values if nothing is saved
        val firstName = sharedPref.getString("firstName", "")
        val lastName = sharedPref.getString("lastName", "")
        val age = sharedPref.getString("age", "")
        val bio = sharedPref.getString("bio", "")

        // Set retrieved data to EditText fields
        firstNameEditText.setText(firstName)
        lastNameEditText.setText(lastName)
        ageEditText.setText(age)
        bioEditText.setText(bio)
    }
}