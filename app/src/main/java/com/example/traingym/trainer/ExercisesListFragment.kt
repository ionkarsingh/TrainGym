package com.example.traingym.trainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.traingym.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExercisesListFragment : Fragment() {

    private var categoryId: String? = null
    private var categoryName: String? = null
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        arguments?.let {
            categoryId = it.getString(ARG_CATEGORY_ID)
            categoryName = it.getString(ARG_CATEGORY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises_list, container, false)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_exercise)

        val displayTextView = view.findViewById<TextView>(R.id.text_view_category_id_display)
        displayTextView.visibility = View.GONE

        fab.setOnClickListener {
            showAddExerciseDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = categoryName ?: "Exercises"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Exercise List"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun showAddExerciseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exercise, null)

        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_exercise_name)
        val setsEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_sets)
        val repsEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_reps)
        val restEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_rest_time)
        val muscleEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_target_muscle)
        val instructionsEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_instructions)
        val url1EditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_image_url1)
        val url2EditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_image_url2)
        val url3EditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_image_url3)
        val url4EditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_image_url4)
        val saveButton = dialogView.findViewById<Button>(R.id.button_save_exercise)
        val closeButton = dialogView.findViewById<ImageView>(R.id.image_view_close_dialog)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val sets = setsEditText.text.toString().trim()
            val reps = repsEditText.text.toString().trim()
            val restTime = restEditText.text.toString().trim()
            val targetMuscle = muscleEditText.text.toString().trim()
            val instructions = instructionsEditText.text.toString().trim()
            if (name.isEmpty()) {
                nameEditText.error = "Required"
                return@setOnClickListener
            }
            if (sets.isEmpty()) {
                setsEditText.error = "Required"
                return@setOnClickListener
            }
            if (reps.isEmpty()) {
                repsEditText.error = "Required"
                return@setOnClickListener
            }
            if (restTime.isEmpty()) {
                restEditText.error = "Required"
                return@setOnClickListener
            }
            if (targetMuscle.isEmpty()) {
                muscleEditText.error = "Required"
                return@setOnClickListener
            }
            if (instructions.isEmpty()) {
                instructionsEditText.error = "Required"
                return@setOnClickListener
            }
            val imageUrls = listOf(
                url1EditText.text.toString().trim(),
                url2EditText.text.toString().trim(),
                url3EditText.text.toString().trim(),
                url4EditText.text.toString().trim()
            )
            val nonEmptyImageUrls = imageUrls.filter { it.isNotBlank() }

            if (nonEmptyImageUrls.size < 2) {
                Toast.makeText(context, "Please provide at least two image URLs.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val newExerciseRef = firestore.collection("Exercises").document()

            val newExercise = hashMapOf(
                "exercise_id" to newExerciseRef.id,
                "category_id" to categoryId,
                "name" to name,
                "sets" to sets,
                "reps" to reps,
                "rest_time" to restTime,
                "target_muscle" to targetMuscle,
                "instructions" to instructions,
                "image_urls" to nonEmptyImageUrls
            )

            lifecycleScope.launch {
                try {
                    newExerciseRef.set(newExercise).await()
                    Toast.makeText(context, "Exercise saved successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving exercise: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CATEGORY_NAME = "category_name"

        fun newInstance(categoryId: String, categoryName: String): ExercisesListFragment {
            val fragment = ExercisesListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_ID, categoryId)
            args.putString(ARG_CATEGORY_NAME, categoryName)
            fragment.arguments = args
            return fragment
        }
    }
}