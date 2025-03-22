package com.kanung.mealmate.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.DialogItemEditBinding
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.data.utils.Constants
import com.google.android.material.R as MaterialR

class ItemEditDialog : DialogFragment() {

    private var _binding: DialogItemEditBinding? = null
    private val binding get() = _binding!!

    private var groceryItem: GroceryItem? = null
    private var itemEditListener: ItemEditListener? = null

    interface ItemEditListener {
        fun onItemSaved(name: String, quantity: Int, price: Double, category: String)
    }

    companion object {
        private const val ARG_GROCERY_ITEM = "grocery_item"

        fun newInstance(item: GroceryItem? = null): ItemEditDialog {
            val fragment = ItemEditDialog()
            if (item != null) {
                val args = Bundle()
                args.putParcelable(ARG_GROCERY_ITEM, item)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, MaterialR.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
        groceryItem = arguments?.getParcelable(ARG_GROCERY_ITEM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogItemEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()
        populateFields()
        setupClickListeners()
        setupValidation()
    }

    private fun setupCategoryDropdown() {
        val categories = Constants.GROCERY_CATEGORIES

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        (binding.menuCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun populateFields() {
        val isEdit = groceryItem != null

        binding.textViewDialogTitle.text = getString(if (isEdit) R.string.edit_item else R.string.add_item)

        if (isEdit) {
            groceryItem?.let { item ->
                binding.editTextName.setText(item.name)
                binding.editTextQuantity.setText(item.quantity.toString())
                binding.editTextPrice.setText(String.format("%.2f", item.price))
                (binding.menuCategory.editText as? AutoCompleteTextView)?.setText(item.category, false)
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                val name = binding.editTextName.text.toString().trim()
                val quantity = binding.editTextQuantity.text.toString().toIntOrNull() ?: 1
                val price = binding.editTextPrice.text.toString().toDoubleOrNull() ?: 0.0
                val category = (binding.menuCategory.editText as? AutoCompleteTextView)?.text.toString().trim()

                itemEditListener?.onItemSaved(name, quantity, price, category)
                dismiss()
            }
        }
    }

    private fun setupValidation() {
        binding.editTextName.doAfterTextChanged {
            binding.inputLayoutName.error = if (it.toString().trim().isEmpty())
                getString(R.string.field_required) else null
        }

        binding.editTextQuantity.doAfterTextChanged {
            val quantity = it.toString().toIntOrNull()
            binding.inputLayoutQuantity.error = when {
                it.toString().isEmpty() -> getString(R.string.field_required)
                quantity == null || quantity <= 0 -> getString(R.string.invalid_quantity)
                else -> null
            }
        }

        binding.editTextPrice.doAfterTextChanged {
            val price = it.toString().toDoubleOrNull()
            binding.inputLayoutPrice.error = when {
                it.toString().isEmpty() -> getString(R.string.field_required)
                price == null || price < 0 -> getString(R.string.invalid_price)
                else -> null
            }
        }

        (binding.menuCategory.editText as? AutoCompleteTextView)?.doAfterTextChanged {
            binding.menuCategory.error = if (it.toString().trim().isEmpty())
                getString(R.string.field_required) else null
        }
    }

    private fun validateInputs(): Boolean {
        val nameValid = binding.editTextName.text.toString().trim().isNotEmpty()
        if (!nameValid) binding.inputLayoutName.error = getString(R.string.field_required)

        val quantityText = binding.editTextQuantity.text.toString()
        val quantityValid = quantityText.isNotEmpty() && (quantityText.toIntOrNull() ?: 0) > 0
        if (!quantityValid) binding.inputLayoutQuantity.error =
            if (quantityText.isEmpty()) getString(R.string.field_required) else getString(R.string.invalid_quantity)

        val priceText = binding.editTextPrice.text.toString()
        val priceValid = priceText.isNotEmpty() && (priceText.toDoubleOrNull() ?: -1.0) >= 0
        if (!priceValid) binding.inputLayoutPrice.error =
            if (priceText.isEmpty()) getString(R.string.field_required) else getString(R.string.invalid_price)

        val categoryValid = (binding.menuCategory.editText as? AutoCompleteTextView)?.text.toString().trim().isNotEmpty()
        if (!categoryValid) binding.menuCategory.error = getString(R.string.field_required)

        return nameValid && quantityValid && priceValid && categoryValid
    }

    fun setItemEditListener(listener: ItemEditListener) {
        this.itemEditListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}