package com.kanung.mealmate.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.DialogAddMealBinding
import com.kanung.mealmate.data.models.Recipe
import com.kanung.mealmate.ui.viewmodels.MealPlanViewModel

class AddMealDialog : DialogFragment() {

    private var _binding: DialogAddMealBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MealPlanViewModel
    private var listener: AddMealListener? = null
    private var day: String = ""
    private var recipes: List<Recipe> = emptyList()
    private var selectedRecipe: Recipe? = null

    interface AddMealListener {
        fun onMealAdded(
            recipeId: String,
            recipeName: String,
            mealType: String,
            servings: Int,
            notes: String
        )
    }

    companion object {
        private const val ARG_DAY = "day"

        fun newInstance(day: String): AddMealDialog {
            val fragment = AddMealDialog()
            val args = Bundle()
            args.putString(ARG_DAY, day)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        day = arguments?.getString(ARG_DAY) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MealPlanViewModel::class.java]

        binding.textViewTitle.text = getString(R.string.add_meal_for_day, day)

        setupMealTypeDropdown()
        setupRecipeDropdown()
        setupClickListeners()

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            this.recipes = recipes
            setupRecipeDropdown()
        }
    }

    private fun setupMealTypeDropdown() {
        val mealTypes = arrayOf("Breakfast", "Lunch", "Dinner", "Snack")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes)
        binding.spinnerMealType.adapter = adapter
    }

    private fun setupRecipeDropdown() {
        val recipeNames = recipes.map { it.title }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, recipeNames)
        binding.spinnerRecipe.adapter = adapter

        binding.spinnerRecipe.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRecipe = recipes.getOrNull(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAdd.setOnClickListener {
            if (validateInputs()) {
                val recipe = selectedRecipe ?: return@setOnClickListener
                val mealType = binding.spinnerMealType.selectedItem.toString()
                val servings = binding.editTextServings.text.toString().toIntOrNull() ?: 1
                val notes = binding.editTextNotes.text.toString()

                listener?.onMealAdded(
                    recipeId = recipe.id,
                    recipeName = recipe.title,
                    mealType = mealType,
                    servings = servings,
                    notes = notes
                )

                dismiss()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (selectedRecipe == null) {
            // Show an error message that a recipe must be selected
            return false
        }

        val servingsText = binding.editTextServings.text.toString()
        if (servingsText.isEmpty()) {
            binding.textInputLayoutServings.error = getString(R.string.field_required)
            return false
        }

        val servings = servingsText.toIntOrNull()
        if (servings == null || servings <= 0) {
            binding.textInputLayoutServings.error = getString(R.string.invalid_servings)
            return false
        }

        return true
    }

    fun setAddMealListener(listener: AddMealListener) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}