package com.kanung.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.FragmentPlansBinding
import com.kanung.mealmate.data.models.MealPlan
import com.kanung.mealmate.adapters.MealPlanAdapter
import com.kanung.mealmate.ui.dialogs.CreatePlanDialog
import com.kanung.mealmate.ui.viewmodels.MealPlanViewModel

class PlansFragment : Fragment(), MealPlanAdapter.MealPlanListener {

    private var _binding: FragmentPlansBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MealPlanViewModel
    private lateinit var adapter: MealPlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MealPlanViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = MealPlanAdapter(this)
        binding.recyclerViewPlans.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlansFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.mealPlans.observe(viewLifecycleOwner) { plans ->
            if (plans.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerViewPlans.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerViewPlans.visibility = View.VISIBLE
                adapter.submitList(plans)
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

    private fun setupClickListeners() {
        binding.fabAddPlan.setOnClickListener {
            showCreatePlanDialog()
        }
    }

    private fun showCreatePlanDialog() {
        val dialog = CreatePlanDialog.newInstance()
        dialog.setCreatePlanListener(object : CreatePlanDialog.CreatePlanListener {
            override fun onPlanCreated(title: String, description: String, startDate: java.util.Date) {
                viewModel.createNewWeeklyPlan(title, description, startDate)
            }
        })
        dialog.show(childFragmentManager, "CreatePlanDialog")
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

    // MealPlanListener implementations
    override fun onMealPlanClick(mealPlan: MealPlan) {
        viewModel.getMealPlanById(mealPlan.id)
        navigateToMealPlanDetail(mealPlan.id)
    }

    override fun onDeleteMealPlan(mealPlan: MealPlan) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_plan)
            .setMessage(getString(R.string.confirm_delete_plan))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteMealPlan(mealPlan.id)
            }
            .show()
    }

    private fun navigateToMealPlanDetail(planId: String) {
        val fragment = MealPlanDetailFragment.newInstance(planId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}