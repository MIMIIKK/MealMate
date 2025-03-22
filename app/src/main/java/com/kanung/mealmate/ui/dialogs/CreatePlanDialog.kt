package com.kanung.mealmate.ui.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kanung.mealmate.R
import com.kanung.mealmate.databinding.DialogCreatePlanBinding
import com.kanung.mealmate.data.models.MealPlan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreatePlanDialog : DialogFragment() {

    private var _binding: DialogCreatePlanBinding? = null
    private val binding get() = _binding!!

    private var listener: CreatePlanListener? = null
    private var mealPlan: MealPlan? = null
    private var selectedDate: Date = Calendar.getInstance().time

    interface CreatePlanListener {
        fun onPlanCreated(title: String, description: String, startDate: Date)
    }

    companion object {
        private const val ARG_MEAL_PLAN = "meal_plan"

        fun newInstance(mealPlan: MealPlan? = null): CreatePlanDialog {
            val fragment = CreatePlanDialog()
            if (mealPlan != null) {
                // Since MealPlan isn't Parcelable, we'll need to handle it differently
                // Store just the necessary data as primitive types
                val args = Bundle()
                args.putString("id", mealPlan.id)
                args.putString("title", mealPlan.title)
                args.putString("description", mealPlan.description)
                args.putLong("startDate", mealPlan.startDate.time)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)

        // Manually create a MealPlan from the primitives we stored
        arguments?.let { args ->
            if (args.containsKey("id")) {
                mealPlan = MealPlan(
                    id = args.getString("id") ?: "",
                    title = args.getString("title") ?: "",
                    description = args.getString("description") ?: "",
                    startDate = Date(args.getLong("startDate"))
                )
                selectedDate = mealPlan!!.startDate
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreatePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isEdit = mealPlan != null
        binding.textViewTitle.text = getString(if (isEdit) R.string.edit_plan else R.string.create_plan)

        if (isEdit) {
            mealPlan?.let {
                binding.editTextTitle.setText(it.title)
                binding.editTextDescription.setText(it.description)
                updateStartDateText(it.startDate)
            }
        } else {
            updateStartDateText(selectedDate)
        }

        binding.layoutStartDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonCreate.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()

            if (validateInputs()) {
                listener?.onPlanCreated(title, description, selectedDate)
                dismiss()
            }
        }
    }

    private fun updateStartDateText(date: Date) {
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        binding.textViewStartDate.text = dateFormat.format(date)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            selectedDate = calendar.time
            updateStartDateText(selectedDate)
        }, year, month, day).show()
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.editTextTitle.text.toString().isEmpty()) {
            binding.textInputLayoutTitle.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.textInputLayoutTitle.error = null
        }

        return isValid
    }

    fun setCreatePlanListener(listener: CreatePlanListener) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}