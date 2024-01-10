package com.nivi.multiplegeofence.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nivi.multiplegeofence.R

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize bottomNav by finding it in your layout
        bottomNav = findViewById(R.id.bottomNav)

        loadFragment(MultipleGeoFenceFragment())

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fence -> {
                    loadFragment(MultipleGeoFenceFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
