package com.kanung.mealmate.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanung.mealmate.R
import com.kanung.mealmate.adapters.FeaturedRecipesAdapter
import com.kanung.mealmate.adapters.PopularRecipesAdapter
import com.kanung.mealmate.databinding.FragmentRecipesBinding
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.ui.viewmodels.RecipeViewModel

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipeViewModel
    private lateinit var featuredAdapter: FeaturedRecipesAdapter
    private lateinit var popularAdapter: PopularRecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        setupRecyclerViews()
        setupObservers()
        setupListeners()

        // Load recipes when fragment is created
        viewModel.loadAllRecipes()
    }

    private fun setupRecyclerViews() {
        // Featured recipes
        featuredAdapter = FeaturedRecipesAdapter(
            onItemClick = { recipe -> navigateToRecipeDetail(recipe.id) },
            onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) }
        )
        binding.recyclerViewFeaturedRecipes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
        }

        // Popular recipes
        popularAdapter = PopularRecipesAdapter(
            onItemClick = { recipe -> navigateToRecipeDetail(recipe.id) },
            onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) }
        )
        binding.recyclerViewPopularRecipes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = popularAdapter
        }
    }

    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.emptyStateLayout.visibility = View.GONE

                // For demonstration, we'll consider featured recipes as the most recent ones
                // and popular recipes as all recipes sorted by creation date
                val featuredRecipes = recipes.take(5)
                featuredAdapter.submitList(featuredRecipes)

                popularAdapter.submitList(recipes)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                showErrorDialog(error)
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        // Search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchRecipes(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.loadAllRecipes() // Reset to all recipes when search is cleared
                }
                return true
            }
        })

        // Add recipe button
        binding.fabAddRecipe.setOnClickListener {
            navigateToAddRecipe()
        }
    }

    private fun navigateToRecipeDetail(recipeId: String) {
        // Navigation using the Navigation Component would look like this:
        // val action = RecipesFragmentDirections.actionRecipesFragmentToRecipeDetailActivity(recipeId)
        // findNavController().navigate(action)

        // Or using traditional intent:
        val intent = android.content.Intent(requireContext(), com.kanung.mealmate.ui.activities.RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivity(intent)
    }

    private fun navigateToAddRecipe() {
        val intent = android.content.Intent(requireContext(), com.kanung.mealmate.ui.activities.AddRecipeActivity::class.java)
        startActivity(intent)
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