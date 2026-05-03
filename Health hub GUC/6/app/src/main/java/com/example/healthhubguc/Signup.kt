package com.example.healthhubguc

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {
    private lateinit var nameEdt: EditText
    private lateinit var ageEdt: EditText
    private lateinit var emailEdt: EditText
    private lateinit var passwordEdt: EditText
    private lateinit var checkboxDoctor: CheckBox
    private lateinit var checkboxPatient: CheckBox
    private lateinit var checkboxMale: CheckBox
    private lateinit var checkboxFemale: CheckBox
    private lateinit var btnSignup: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        nameEdt = findViewById(R.id.name_edt)
        ageEdt = findViewById(R.id.age_edt)
        emailEdt = findViewById(R.id.email_edt)
        passwordEdt = findViewById(R.id.password_edt)
        checkboxDoctor = findViewById(R.id.checkbox_doctor)
        checkboxPatient = findViewById(R.id.checkbox_patient)
        checkboxMale = findViewById(R.id.checkbox_male)
        checkboxFemale = findViewById(R.id.checkbox_female)
        btnSignup = findViewById(R.id.btn_signup)

        // Ensure only one user type can be selected at a time
        checkboxDoctor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkboxPatient.isChecked = false
        }

        checkboxPatient.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkboxDoctor.isChecked = false
        }

        // Ensure only one gender can be selected at a time
        checkboxMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkboxFemale.isChecked = false
        }

        checkboxFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkboxMale.isChecked = false
        }

        btnSignup.setOnClickListener {
            val name = nameEdt.text.toString()
            val age = ageEdt.text.toString()
            val email = emailEdt.text.toString()
            val password = passwordEdt.text.toString()
            val userType = if (checkboxDoctor.isChecked) "Doctor" else "Patient"
            val gender = if (checkboxMale.isChecked) "Male" else "Female"

            signupUser(name, age, email, password, userType, gender)
        }
    }

    private fun signupUser(
        name: String,
        age: String,
        email: String,
        password: String,
        userType: String,
        gender: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Retrieve the unique user ID (uid) for the authenticated user
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Obfuscate the password as asterisks
                    val obfuscatedPassword = "*".repeat(password.length)

                    // Create User object with the input data
                    val user = User(
                        name = name,
                        age = age,
                        email = email,
                        userType = userType,
                        gender = gender,
                        userId = userId,
                        password = obfuscatedPassword
                    )

                    // Reference to the "Users" node in the database
                    val userRef = database.getReference("Users").child(userId)

                    // Save the user object to Firebase Database
                    userRef.setValue(user).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            // Redirect to the Login page after successful sign-up
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Sign-up failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}