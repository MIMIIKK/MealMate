package com.kanung.mealmate.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kanung.mealmate.data.models.MealPlan
import com.kanung.mealmate.ui.fragments.DayFragment

class DaysPagerAdapter(
    fragment: Fragment,
    private val days: List<String>,
    private val mealPlan: MealPlan
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = days.size

    override fun createFragment(position: Int): Fragment {
        val day = days[position]
        val mealItems = mealPlan.meals[day] ?: emptyList()
        return DayFragment.newInstance(mealPlan.id, day, mealItems)
    }
}