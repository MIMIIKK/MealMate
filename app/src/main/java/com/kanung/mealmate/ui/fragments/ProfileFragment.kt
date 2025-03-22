package com.kanung.mealmate.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.FragmentProfileBinding
import com.kanung.mealmate.ui.activities.LoginActivity
import com.kanung.mealmate.ui.viewmodels.UserViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Observe user profile data
        observeUserProfile()

        // Observe user stats
        observeUserStats()

        // Setup click listeners
        setupClickListeners()
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.textViewName.text = user.name ?: "User"
                binding.textViewEmail.text = user.email ?: "No Email"

                // Load profile image if available
                if (user != null) {
                    binding.textViewName.text = user.name ?: "User"
                    binding.textViewEmail.text = user.email ?: "No Email"

                    // Load profile image if available
                    //if (!user.photoUrl.isNullOrEmpty()) {
                     //   Glide.with(this).load(user.photoUrl).into(binding.imageViewProfile)
                    //}
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show a loading indicator here if needed
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUserStats() {
        viewModel.recipeCount.observe(viewLifecycleOwner) { count ->
            binding.textViewRecipeCount.text = count.toString()
        }

        viewModel.mealPlanCount.observe(viewLifecycleOwner) { count ->
            binding.textViewMealPlans.text = count.toString()
        }

        viewModel.completedShopsCount.observe(viewLifecycleOwner) { count ->
            binding.textViewCompletedShops.text = count.toString()
        }
    }

    private fun setupClickListeners() {
        binding.buttonEditProfile.setOnClickListener {
            Toast.makeText(context, "Edit Profile functionality coming soon", Toast.LENGTH_SHORT)
                .show()
        }

        binding.fabEditPhoto.setOnClickListener {
            Toast.makeText(context, "Photo editing functionality coming soon", Toast.LENGTH_SHORT)
                .show()
        }

        binding.layoutAccountSettings.setOnClickListener {
            Toast.makeText(
                context,
                "Account settings functionality coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.layoutNotifications.setOnClickListener {
            Toast.makeText(
                context,
                "Notifications settings functionality coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.layoutAppearance.setOnClickListener {
            Toast.makeText(
                context,
                "Appearance settings functionality coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.layoutHelp.setOnClickListener {
            Toast.makeText(context, "Help & Support functionality coming soon", Toast.LENGTH_SHORT)
                .show()
        }

        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        viewModel.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}