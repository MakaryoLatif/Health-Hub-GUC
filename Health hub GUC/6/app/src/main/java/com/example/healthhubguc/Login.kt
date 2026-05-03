package com.example.healthhubguc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {
    private lateinit var emailEdt: EditText
    private lateinit var passwordEdt: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        emailEdt = findViewById(R.id.Email_edt)
        passwordEdt = findViewById(R.id.password_edt)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignup = findViewById(R.id.btnSignup)



        btnSignup.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = emailEdt.text.toString()
            val password = passwordEdt.text.toString()
            loginfunc(email, password)
        }
    }

    private fun loginfunc(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the current user ID
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Reference to the user in Firebase Realtime Database
                    val userRef = database.getReference("Users").child(userId)

                    // Fetch user data to check userType
                    userRef.get().addOnSuccessListener { dataSnapshot ->
                        val userType = dataSnapshot.child("userType").getValue(String::class.java)

                        if (userType == "Doctor") {
                            val intent = Intent(this, Doctor::class.java)
                            startActivity(intent)
                        } else if (userType == "Patient") {
                            val intent = Intent(this, Patient::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "User type not defined", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "User doesn't exist or login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
