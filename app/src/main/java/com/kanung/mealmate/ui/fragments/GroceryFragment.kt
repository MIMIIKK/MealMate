package com.kanung.mealmate.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.kanung.mealmate.R
import com.kanung.mealmate.adapters.GroceryListAdapter
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.databinding.FragmentGroceryBinding
import com.kanung.mealmate.ui.dialogs.GroceryItemDialog
import com.kanung.mealmate.ui.viewmodels.GroceryViewModel

class GroceryFragment : Fragment() {

    private var _binding: FragmentGroceryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GroceryViewModel
    private lateinit var adapter: GroceryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroceryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GroceryViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Load grocery items when fragment is created
        viewModel.loadAllGroceryItems()
    }

    private fun setupRecyclerView() {
        adapter = GroceryListAdapter(
            onItemClick = { item -> showEditItemDialog(item) },
            onCheckChanged = { item, isChecked -> viewModel.updateItemPurchasedStatus(item.id, isChecked) },
            onDeleteClick = { item -> confirmDeleteItem(item) }
        )

        binding.recyclerViewGroceries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GroceryFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.groceryItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerViewGroceries.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerViewGroceries.visibility = View.VISIBLE
                adapter.submitList(items)
            }
        }

        viewModel.totalCost.observe(viewLifecycleOwner) { total ->
            binding.textViewTotalCost.text = getString(R.string.total_cost_value, total)
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
        binding.fabAddItem.setOnClickListener {
            showAddItemDialog()
        }

        binding.buttonClearCompleted.setOnClickListener {
            confirmClearCompletedItems()
        }

        binding.buttonShareList.setOnClickListener {
            shareBySMS()
        }
    }

    private fun showAddItemDialog() {
        val dialog = GroceryItemDialog.newInstance()
        dialog.setItemSaveListener { name, quantity, price, category ->
            val newItem = GroceryItem(
                name = name,
                quantity = quantity,
                price = price,
                category = category
            )
            viewModel.addGroceryItem(newItem)
        }
        dialog.show(childFragmentManager, "AddGroceryItemDialog")
    }

    private fun showEditItemDialog(item: GroceryItem) {
        val dialog = GroceryItemDialog.newInstance(item)
        dialog.setItemSaveListener { name, quantity, price, category ->
            val updatedItem = item.copy(
                name = name,
                quantity = quantity,
                price = price,
                category = category
            )
            viewModel.updateGroceryItem(updatedItem)
        }
        dialog.show(childFragmentManager, "EditGroceryItemDialog")
    }

    private fun confirmDeleteItem(item: GroceryItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_item)
            .setMessage(getString(R.string.confirm_delete_item))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteGroceryItem(item.id)
            }
            .show()
    }

    private fun confirmClearCompletedItems() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_completed)
            .setMessage(getString(R.string.confirm_clear_completed))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearCompletedItems()
            }
            .show()
    }

    private fun shareBySMS() {
        val items = viewModel.groceryItems.value ?: emptyList()
        if (items.isEmpty()) {
            showErrorDialog(getString(R.string.empty_grocery_list_message))
            return
        }

        val phoneNumberDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.share_grocery_list)
            .setMessage(getString(R.string.enter_phone_number))
            .setView(R.layout.dialog_phone_input)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.share) { dialog, _ ->
                val phoneInput = (dialog as androidx.appcompat.app.AlertDialog)
                    .findViewById<TextInputEditText>(R.id.editTextPhone)
                val phoneNumber = phoneInput?.text.toString()

                if (phoneNumber.isNotEmpty()) {
                    sendSMS(phoneNumber, items)
                }
            }
            .create()

        phoneNumberDialog.show()
    }

    private fun sendSMS(phoneNumber: String, items: List<GroceryItem>) {
        val message = formatGroceryListForSMS(items)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("smsto:$phoneNumber")
        intent.putExtra("sms_body", message)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            showErrorDialog(getString(R.string.error_no_sms_app))
        }
    }

    private fun formatGroceryListForSMS(items: List<GroceryItem>): String {
        val sb = StringBuilder()
        sb.appendLine("--- GROCERY LIST ---")
        sb.appendLine()

        // Group by category
        val groupedItems = items.groupBy { it.category }

        groupedItems.forEach { (category, categoryItems) ->
            sb.appendLine("$category:")
            categoryItems.forEach { item ->
                val status = if (item.isPurchased) "✓ " else ""
                sb.appendLine("$status${item.quantity}x ${item.name} - $${String.format("%.2f", item.price)}")
            }
            sb.appendLine()
        }

        // Add total
        val total = items.sumOf { it.price * it.quantity }
        sb.appendLine("Total: $${String.format("%.2f", total)}")

        return sb.toString()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_error)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}