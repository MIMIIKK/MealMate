package com.kanung.mealmate.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kanung.mealmate.R
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.databinding.DialogGroceryItemBinding
import com.google.android.material.R as MaterialR

class GroceryItemDialog : DialogFragment() {

    private var _binding: DialogGroceryItemBinding? = null
    private val binding get() = _binding!!

    private var groceryItem: GroceryItem? = null
    private var itemSaveListener: ((String, Int, Double, String) -> Unit)? = null

    companion object {
        private const val ARG_GROCERY_ITEM = "arg_grocery_item"

        fun newInstance(item: GroceryItem? = null): GroceryItemDialog {
            val fragment = GroceryItemDialog()
            item?.let {
                val args = Bundle()
                args.putParcelable(ARG_GROCERY_ITEM, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, MaterialR.style.Theme_MaterialComponents_Dialog_Alert)

        @Suppress("DEPRECATION")
        groceryItem = arguments?.getParcelable(ARG_GROCERY_ITEM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogGroceryItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()
        setupInitialValues()
        setupButtons()
    }

    private fun setupCategoryDropdown() {
        val categories = GroceryItem.CATEGORIES
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        (binding.categoryInputLayout.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupInitialValues() {
        groceryItem?.let { item ->
            binding.titleTextView.text = getString(R.string.edit_grocery_item)
            binding.nameEditText.setText(item.name)
            binding.quantityEditText.setText(item.quantity.toString())
            binding.priceEditText.setText(String.format("%.2f", item.price))
            (binding.categoryInputLayout.editText as? MaterialAutoCompleteTextView)?.setText(item.category, false)
        } ?: run {
            binding.titleTextView.text = getString(R.string.add_grocery_item)
            binding.quantityEditText.setText("1")
            binding.priceEditText.setText("0.00")
        }
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                val name = binding.nameEditText.text.toString().trim()
                val quantity = binding.quantityEditText.text.toString().toInt()
                val price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val category = (binding.categoryInputLayout.editText as? MaterialAutoCompleteTextView)?.text.toString()

                itemSaveListener?.invoke(name, quantity, price, category)
                dismiss()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate name
        if (binding.nameEditText.text.isNullOrBlank()) {
            binding.nameInputLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }

        // Validate quantity
        val quantityStr = binding.quantityEditText.text.toString()
        if (quantityStr.isBlank()) {
            binding.quantityInputLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            val quantity = quantityStr.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                binding.quantityInputLayout.error = getString(R.string.error_invalid_quantity)
                isValid = false
            } else {
                binding.quantityInputLayout.error = null
            }
        }

        // Validate price
        val priceStr = binding.priceEditText.text.toString()
        if (priceStr.isBlank()) {
            binding.priceInputLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            val price = priceStr.toDoubleOrNull()
            if (price == null || price < 0) {
                binding.priceInputLayout.error = getString(R.string.error_invalid_price)
                isValid = false
            } else {
                binding.priceInputLayout.error = null
            }
        }

        // Validate category
        val category = (binding.categoryInputLayout.editText as? MaterialAutoCompleteTextView)?.text.toString()
        if (category.isBlank()) {
            binding.categoryInputLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.categoryInputLayout.error = null
        }

        return isValid
    }

    fun setItemSaveListener(listener: (String, Int, Double, String) -> Unit) {
        this.itemSaveListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}