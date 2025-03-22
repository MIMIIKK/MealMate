package com.kanung.mealmate.data.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kanung.mealmate.R
import com.kanung.mealmate.data.models.GroceryItem

/**
 * Helper class for handling SMS operations for the grocery list delegation feature.
 */
class SMSHelper {

    companion object {
        const val SMS_PERMISSION_REQUEST = 101

        /**
         * Format grocery items into a readable text message
         */
        fun formatGroceryList(context: Context, items: List<GroceryItem>): String {
            val builder = StringBuilder()
            builder.append(context.getString(R.string.grocery_list_header)).append("\n\n")

            // Group items by category
            val groupedItems = items.groupBy { it.category }

            groupedItems.forEach { (category, categoryItems) ->
                builder.append("${category}:\n")
                categoryItems.forEach { item ->
                    val status = if (item.isPurchased) "✓ " else ""
                    builder.append("$status${item.quantity}x ${item.name} - $${String.format("%.2f", item.price)}\n")
                }
                builder.append("\n")
            }

            // Add total
            val total = items.sumOf { it.price * it.quantity }
            builder.append(context.getString(R.string.total_cost_value, total))

            return builder.toString()
        }

        /**
         * Send grocery list via SMS using device's default SMS app
         */
        fun sendGroceryListViaSMS(context: Context, phoneNumber: String, message: String, additionalText: String = ""): Boolean {
            return try {
                val fullMessage = if (additionalText.isNotEmpty()) {
                    "$additionalText\n\n$message"
                } else {
                    message
                }

                // Use device's SMS app
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("smsto:$phoneNumber")
                intent.putExtra("sms_body", fullMessage)
                context.startActivity(intent)

                Toast.makeText(context, R.string.delegation_success, Toast.LENGTH_SHORT).show()
                true
            } catch (e: Exception) {
                Toast.makeText(context, R.string.sms_send_error, Toast.LENGTH_SHORT).show()
                false
            }
        }

        /**
         * Check if SMS permission is granted
         */
        fun hasSmsPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Process permission request results for SMS
         */
        fun handlePermissionResult(
            context: Context,
            requestCode: Int,
            grantResults: IntArray,
            phoneNumber: String,
            message: String,
            successCallback: () -> Unit
        ) {
            if (requestCode == SMS_PERMISSION_REQUEST) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendGroceryListViaSMS(context, phoneNumber, message)
                    successCallback()
                } else {
                    Toast.makeText(context, R.string.sms_permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}