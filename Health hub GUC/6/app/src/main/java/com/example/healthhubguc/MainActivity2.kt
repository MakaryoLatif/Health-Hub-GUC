package com.example.healthhubguc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    private lateinit var buttonlogin: Button
    private lateinit var buttonsignup: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        buttonlogin = findViewById(R.id.buttonlogin)
        buttonsignup = findViewById(R.id.buttonsignup)
        buttonlogin.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)}
        buttonsignup.setOnClickListener{
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)}

    }
}