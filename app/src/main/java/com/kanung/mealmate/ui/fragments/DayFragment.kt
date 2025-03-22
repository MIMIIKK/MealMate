package com.kanung.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.FragmentDayBinding
import com.kanung.mealmate.data.models.MealItem
import com.kanung.mealmate.adapters.MealItemAdapter
import com.kanung.mealmate.ui.viewmodels.MealPlanViewModel

class DayFragment : Fragment(), MealItemAdapter.MealItemListener {

    private var _binding: FragmentDayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MealPlanViewModel
    private lateinit var adapter: MealItemAdapter

    private var planId: String = ""
    private var day: String = ""
    private var mealItems: List<MealItem> = emptyList()

    companion object {
        private const val ARG_PLAN_ID = "plan_id"
        private const val ARG_DAY = "day"
        private const val ARG_MEALS = "meals"

        fun newInstance(planId: String, day: String, mealItems: List<MealItem>): DayFragment {
            val fragment = DayFragment()
            val args = Bundle()
            args.putString(ARG_PLAN_ID, planId)
            args.putString(ARG_DAY, day)
            args.putParcelableArrayList(ARG_MEALS, ArrayList(mealItems))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            planId = it.getString(ARG_PLAN_ID, "")
            day = it.getString(ARG_DAY, "")
            @Suppress("DEPRECATION")
            mealItems = it.getParcelableArrayList(ARG_MEALS) ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MealPlanViewModel::class.java]

        setupRecyclerView()
        updateUI()
    }

    private fun setupRecyclerView() {
        adapter = MealItemAdapter(this)
        binding.recyclerViewMeals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DayFragment.adapter
        }
    }

    private fun updateUI() {
        if (mealItems.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerViewMeals.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerViewMeals.visibility = View.VISIBLE
            adapter.submitList(mealItems)
        }
    }

    // MealItemListener implementations
    override fun onViewRecipeClick(recipeId: String) {
        // Navigate to recipe detail
        // This would typically be implemented by navigating to your RecipeDetailFragment
        // For example:
        // val fragment = RecipeDetailFragment.newInstance(recipeId)
        // requireActivity().supportFragmentManager.beginTransaction()
        //     .replace(R.id.fragment_container, fragment)
        //     .addToBackStack(null)
        //     .commit()
    }

    override fun onRemoveMealClick(mealItem: MealItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.remove_meal)
            .setMessage(R.string.confirm_remove_meal)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.remove) { _, _ ->
                viewModel.removeMealFromMealPlan(planId, mealItem.id)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}