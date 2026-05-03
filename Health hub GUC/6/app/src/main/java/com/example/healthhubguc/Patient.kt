package com.example.healthhubguc

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.healthhubguc.fragments.DocProfFragment
import com.example.healthhubguc.fragments.ReviewFragment
import com.google.android.material.tabs.TabLayout

class Patient : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient)

        val tabLayout = findViewById<TabLayout>(R.id.tabPat)
        if (savedInstanceState == null) {
            replaceFragment(pat_medications_frag()) 
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> replaceFragment(pat_medications_frag())
                    1 -> replaceFragment(Cart())
                    2 -> replaceFragment(Medical_consult()) 
                    3 -> replaceFragment(pat_profile_frag())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.framePat, fragment)
            .commit()
    }
}