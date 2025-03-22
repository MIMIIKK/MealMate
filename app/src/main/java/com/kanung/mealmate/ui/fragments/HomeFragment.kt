package com.kanung.mealmate.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kanung.mealmate.R
import com.kanung.mealmate.adapters.FeaturedRecipesAdapter
import com.kanung.mealmate.adapters.PopularRecipesAdapter
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.ui.activities.RecipeDetailActivity
import com.kanung.mealmate.ui.viewmodels.RecipeViewModel

class HomeFragment : Fragment() {

    private lateinit var featuredRecipesAdapter: FeaturedRecipesAdapter
    private lateinit var popularRecipesAdapter: PopularRecipesAdapter
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        // Set up Featured Recipes RecyclerView
        val featuredRecipesRecycler = view.findViewById<RecyclerView>(R.id.featured_recipes_recycler)
        featuredRecipesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        featuredRecipesAdapter = FeaturedRecipesAdapter(
            onItemClick = { recipe -> navigateToRecipeDetail(recipe) },
            onFavoriteClick = { recipe -> toggleFavorite(recipe) }
        )
        featuredRecipesRecycler.adapter = featuredRecipesAdapter

        // Set up Popular Recipes RecyclerView
        val popularRecipesRecycler = view.findViewById<RecyclerView>(R.id.popular_recipes_recycler)
        popularRecipesRecycler.layoutManager = LinearLayoutManager(requireContext())

        popularRecipesAdapter = PopularRecipesAdapter(
            onItemClick = { recipe -> navigateToRecipeDetail(recipe) },
            onFavoriteClick = { recipe -> toggleFavorite(recipe) }
        )
        popularRecipesRecycler.adapter = popularRecipesAdapter

        // Observe recipe data
        observeRecipeData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load recipes when fragment is visible
        viewModel.loadAllRecipes()
    }

    private fun observeRecipeData() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes.isNotEmpty()) {
                // For featured recipes, we might want to show the most recent ones
                val featuredRecipes = recipes.sortedByDescending { it.createdAt }.take(5)
                featuredRecipesAdapter.submitList(featuredRecipes)

                // For popular recipes, show all recipes
                popularRecipesAdapter.submitList(recipes)
            }
        }
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailActivity::class.java).apply {
            putExtra("RECIPE_ID", recipe.id)
        }
        startActivity(intent)
    }

    private fun toggleFavorite(recipe: Recipe) {
        viewModel.toggleFavorite(recipe.id, !recipe.isFavorite)
    }
}