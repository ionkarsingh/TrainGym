package com.example.traingym.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.traingym.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class UserBmiFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_bmi, container, false)

        val heightEditText = view.findViewById<TextInputEditText>(R.id.edit_text_height)
        val weightEditText = view.findViewById<TextInputEditText>(R.id.edit_text_weight)
        val calculateButton = view.findViewById<MaterialButton>(R.id.button_calculate_bmi)
        val bmiResultText = view.findViewById<TextView>(R.id.text_view_bmi_result)
        val bmiCategoryText = view.findViewById<TextView>(R.id.text_view_bmi_category)

        calculateButton.setOnClickListener {
            val heightStr = heightEditText.text?.toString()?.trim()
            val weightStr = weightEditText.text?.toString()?.trim()

            var hasError = false

            if (heightStr.isNullOrEmpty()) {
                heightEditText.error = "Enter height"
                hasError = true
            }
            if (weightStr.isNullOrEmpty()) {
                weightEditText.error = "Enter weight"
                hasError = true
            }
            val heightCm = heightStr?.toFloatOrNull()
            val weightKg = weightStr?.toFloatOrNull()
            if (!hasError && (heightCm == null || heightCm <= 0f)) {
                heightEditText.error = "Enter valid height"
                hasError = true
            }
            if (!hasError && (weightKg == null || weightKg <= 0f)) {
                weightEditText.error = "Enter valid weight"
                hasError = true
            }

            if (hasError) {
                bmiResultText.visibility = View.GONE
                bmiCategoryText.visibility = View.GONE
                return@setOnClickListener
            }

            val heightM = heightCm!! / 100f
            val bmi = weightKg!! / (heightM * heightM)
            val bmiRounded = String.format("%.1f", bmi)
            val (category, colorRes, message) = when {
                bmi < 18.5 -> Triple("Underweight", R.color.blue_500, "You are Underweight.")
                bmi < 25 -> Triple("Normal weight", R.color.green_status, "You are in the Normal range.")
                bmi < 30 -> Triple("Overweight", R.color.selected_item_background, "You are Overweight.")
                else -> Triple("Obese", R.color.red_status, "You are Obese.")
            }

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val bmiData = hashMapOf(
                    "userId" to userId,
                    "bmi" to bmi,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("Bmi").document(userId)
                    .set(bmiData)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to save BMI: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            bmiResultText.text = "BMI: $bmiRounded"
            bmiResultText.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            bmiResultText.visibility = View.VISIBLE

            bmiCategoryText.text = category
            bmiCategoryText.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            bmiCategoryText.visibility = View.VISIBLE

            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("BMI Result")
                .setMessage("Your BMI is $bmiRounded\n$message")
                .setPositiveButton("OK", null)
                .show()
        }

        return view
    }
} 