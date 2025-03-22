package com.kanung.mealmate.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.DialogDelegationBinding
import com.kanung.mealmate.data.models.GroceryItem
import com.kanung.mealmate.data.utils.SMSHelper
import com.google.android.material.R as MaterialR

class DelegationDialog : DialogFragment() {

    private var _binding: DialogDelegationBinding? = null
    private val binding get() = _binding!!

    private var groceryItems: List<GroceryItem> = emptyList()

    companion object {
        private const val ARG_GROCERY_ITEMS = "grocery_items"
        private const val SMS_PERMISSION_REQUEST = 100

        fun newInstance(items: List<GroceryItem>): DelegationDialog {
            val fragment = DelegationDialog()
            val args = Bundle().apply {
                putParcelableArrayList(ARG_GROCERY_ITEMS, ArrayList(items))
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, MaterialR.style.ThemeOverlay_MaterialComponents_Dialog_Alert)

        @Suppress("DEPRECATION")
        groceryItems = arguments?.getParcelableArrayList(ARG_GROCERY_ITEMS) ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDelegationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Format grocery list for preview
        val previewText = SMSHelper.formatGroceryList(requireContext(), groceryItems)
        binding.textViewPreview.text = previewText

        // Setup click listeners
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSend.setOnClickListener {
            val phoneNumber = binding.editTextPhone.text.toString().trim()
            val message = binding.editTextMessage.text.toString().trim()

            if (validateInputs(phoneNumber)) {
                checkSmsPermissionAndSend(phoneNumber, message, previewText)
            }
        }
    }

    private fun validateInputs(phoneNumber: String): Boolean {
        if (phoneNumber.isEmpty()) {
            binding.inputLayoutPhone.error = getString(R.string.field_required)
            return false
        }

        // Simple phone number validation
        if (!phoneNumber.matches(Regex("^[+]?[0-9]{10,15}$"))) {
            binding.inputLayoutPhone.error = getString(R.string.invalid_phone)
            return false
        }

        binding.inputLayoutPhone.error = null
        return true
    }

    private fun checkSmsPermissionAndSend(phoneNumber: String, message: String, groceryList: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST
            )
        } else {
            sendSms(phoneNumber, message, groceryList)
        }
    }

    private fun sendSms(phoneNumber: String, message: String, groceryList: String) {
        val success = SMSHelper.sendGroceryListViaSMS(
            requireContext(),
            phoneNumber,
            groceryList,
            message
        )

        if (success) {
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        SMSHelper.handlePermissionResult(
            requireContext(),
            requestCode,
            grantResults,
            binding.editTextPhone.text.toString().trim(),
            binding.textViewPreview.text.toString(),
            { dismiss() }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}