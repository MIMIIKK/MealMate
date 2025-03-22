package com.kanung.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.FragmentMealPlanDetailBinding
import com.kanung.mealmate.ui.dialogs.CreatePlanDialog
import com.kanung.mealmate.data.models.MealPlan
import com.kanung.mealmate.adapters.DaysPagerAdapter
import com.kanung.mealmate.ui.dialogs.AddMealDialog
import com.kanung.mealmate.ui.viewmodels.MealPlanViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MealPlanDetailFragment : Fragment() {

    private var _binding: FragmentMealPlanDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MealPlanViewModel
    private var planId: String? = null
    private var currentMealPlan: MealPlan? = null

    companion object {
        private const val ARG_PLAN_ID = "plan_id"

        fun newInstance(planId: String): MealPlanDetailFragment {
            val fragment = MealPlanDetailFragment()
            val args = Bundle()
            args.putString(ARG_PLAN_ID, planId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        planId = arguments?.getString(ARG_PLAN_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPlanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MealPlanViewModel::class.java]

        if (planId != null) {
            viewModel.getMealPlanById(planId!!)
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.currentMealPlan.observe(viewLifecycleOwner) { mealPlan ->
            if (mealPlan != null) {
                currentMealPlan = mealPlan
                updateUI(mealPlan)
                setupViewPager(mealPlan)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                showErrorDialog(error)
            }
        }
    }

    private fun updateUI(mealPlan: MealPlan) {
        binding.textViewPlanTitle.text = mealPlan.title
        binding.textViewPlanDescription.text = mealPlan.description

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateRange = "${dateFormat.format(mealPlan.startDate)} - ${dateFormat.format(mealPlan.endDate)}"
        binding.textViewDateRange.text = dateRange
    }

    private fun setupViewPager(mealPlan: MealPlan) {
        val daysList = mealPlan.meals.keys.sorted()
        val adapter = DaysPagerAdapter(this, daysList, mealPlan)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = daysList[position]
        }.attach()
    }

    private fun setupClickListeners() {
        binding.fabAddMeal.setOnClickListener {
            showAddMealDialog()
        }

        binding.buttonBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.buttonEditPlan.setOnClickListener {
            showEditPlanDialog()
        }
    }

    private fun showAddMealDialog() {
        if (currentMealPlan == null || binding.viewPager.adapter == null) return

        val currentDay = currentMealPlan!!.meals.keys.sorted()[binding.viewPager.currentItem]

        val dialog = AddMealDialog.newInstance(currentDay)
        dialog.setAddMealListener(object : AddMealDialog.AddMealListener {
            override fun onMealAdded(
                recipeId: String,
                recipeName: String,
                mealType: String,
                servings: Int,
                notes: String
            ) {
                planId?.let { id ->
                    viewModel.addMealToMealPlan(
                        planId = id,
                        recipeId = recipeId,
                        recipeName = recipeName,
                        day = currentDay,
                        mealType = mealType,
                        servings = servings,
                        notes = notes
                    )
                }
            }
        })
        dialog.show(childFragmentManager, "AddMealDialog")
    }

    private fun showEditPlanDialog() {
        currentMealPlan?.let { mealPlan ->
            val dialog = CreatePlanDialog.newInstance(mealPlan)
            dialog.setCreatePlanListener(object : CreatePlanDialog.CreatePlanListener {
                override fun onPlanCreated(title: String, description: String, startDate: java.util.Date) {
                    val updatedPlan = mealPlan.copy(
                        title = title,
                        description = description,
                        startDate = startDate
                    )
                    viewModel.updateMealPlan(updatedPlan)
                }
            })
            dialog.show(childFragmentManager, "EditPlanDialog")
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_error)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_button_understood) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}