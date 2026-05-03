package com.example.healthhubguc



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.example.healthhubguc.fragments.DocProfFragment
import com.example.healthhubguc.fragments.MedicationsFragmentD


import com.example.healthhubguc.fragments.ReviewFragment

class Doctor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        // Set default fragment to DocProfFragment when the activity starts
        if (savedInstanceState == null) {
            replaceFragment(DocProfFragment())  // First fragment
        }

        // Set up listener for TabLayout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> replaceFragment(DocProfFragment())       // Profile Tab
                    1 -> replaceFragment(MedicationsFragmentD())   // Medications Tab
                    2 -> replaceFragment(ReviewFragment())        // Review Tab
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // Function to replace the fragment in the container
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)  // Use the ID of your container
            .commit()
    }
}
