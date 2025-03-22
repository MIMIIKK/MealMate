package com.kanung.mealmate.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kanung.mealmate.R
import com.kanung.mealmate.ui.fragments.GroceryFragment
import com.kanung.mealmate.ui.fragments.HomeFragment
import com.kanung.mealmate.ui.fragments.PlansFragment
import com.kanung.mealmate.ui.fragments.ProfileFragment
import com.kanung.mealmate.ui.fragments.RecipesFragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        // Load the default fragment (HomeFragment)
        loadFragment(HomeFragment())

        // Set up BottomNavigationView
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_recipes -> {
                    loadFragment(RecipesFragment())
                    true
                }
                R.id.nav_plans -> {
                    loadFragment(PlansFragment())
                    true
                }
                R.id.nav_grocery -> {
                    loadFragment(GroceryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}