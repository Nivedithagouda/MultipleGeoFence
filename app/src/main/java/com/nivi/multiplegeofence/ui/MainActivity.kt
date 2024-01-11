package com.nivi.multiplegeofence.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nivi.multiplegeofence.R

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadFragment(RouteMapFragment())
        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fence -> {
                    loadFragment(MultipleGeoFenceFragment())
                    true
                }

                R.id.route -> {
                    // Create a new instance of the RouteMapFragment
                    val routeMapFragment = RouteMapFragment()

                    // Load the RouteMapFragment
                    loadFragment(routeMapFragment)

                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

}
