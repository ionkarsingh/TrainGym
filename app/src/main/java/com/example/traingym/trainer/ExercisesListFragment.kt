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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExercisesListFragment : Fragment(), ExerciseAdapter.OnExerciseActionsListener {

    private var categoryId: String? = null
    private var categoryName: String? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var noExercisesTextView: TextView
    private var editExerciseDialog: androidx.appcompat.app.AlertDialog? = null
    private lateinit var nameEditText: TextInputEditText
    private lateinit var setsEditText: TextInputEditText
    private lateinit var repsEditText: TextInputEditText
    private lateinit var restEditText: TextInputEditText
    private lateinit var muscleEditText: TextInputEditText
    private lateinit var instructionsEditText: TextInputEditText
    private lateinit var url1EditText: TextInputEditText
    private lateinit var url2EditText: TextInputEditText
    private lateinit var url3EditText: TextInputEditText
    private lateinit var url4EditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var closeButton: ImageView

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
        exercisesRecyclerView = view.findViewById(R.id.exercises_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)
        noExercisesTextView = view.findViewById(R.id.text_view_no_exercises)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_exercise)
        fab.setOnClickListener {
            showAddExerciseDialog()
        }

        setupRecyclerView()
        setupEditExerciseDialog()
        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = categoryName ?: "Exercises"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fetchExercises()
    }

    override fun onStop() {
        super.onStop()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Exercise List"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(emptyList())
        exerciseAdapter.setOnExerciseActionsListener(this)
        exercisesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = exerciseAdapter
        }
    }

    private fun showLoading(isLoading: Boolean) {
        lottieAnimationView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun fetchExercises() {
        showLoading(true)
        exercisesRecyclerView.visibility = View.GONE
        noExercisesTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("Exercises")
                    .whereEqualTo("category_id", categoryId)
                    .get().await()
                val exercises = snapshot.toObjects(Exercise::class.java)

                if (exercises.isEmpty()) {
                    noExercisesTextView.visibility = View.VISIBLE
                    exercisesRecyclerView.visibility = View.GONE
                } else {
                    noExercisesTextView.visibility = View.GONE
                    exercisesRecyclerView.visibility = View.VISIBLE
                    exerciseAdapter.updateData(exercises)
                }
            } catch (e: Exception) {
            } finally {
                showLoading(false)
            }
        }
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

            if (name.isEmpty()) { nameEditText.error = "Required"; return@setOnClickListener }
            if (sets.isEmpty()) { setsEditText.error = "Required"; return@setOnClickListener }
            if (reps.isEmpty()) { repsEditText.error = "Required"; return@setOnClickListener }
            if (restTime.isEmpty()) { restEditText.error = "Required"; return@setOnClickListener }
            if (targetMuscle.isEmpty()) { muscleEditText.error = "Required"; return@setOnClickListener }
            if (instructions.isEmpty()) { instructionsEditText.error = "Required"; return@setOnClickListener }

            val nonEmptyImageUrls = listOfNotNull(
                url1EditText.text.toString().trim().takeIf { it.isNotBlank() },
                url2EditText.text.toString().trim().takeIf { it.isNotBlank() },
                url3EditText.text.toString().trim().takeIf { it.isNotBlank() },
                url4EditText.text.toString().trim().takeIf { it.isNotBlank() }
            )

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
                    fetchExercises()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving exercise: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
    }

    override fun onEditClicked(exercise: Exercise) {
        showEditExerciseDialog(exercise)
    }

    override fun onDeleteClicked(exercise: Exercise) {
        showDeleteConfirmationDialog(exercise)
    }

    private fun setupEditExerciseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_exercise, null)
        nameEditText = dialogView.findViewById(R.id.edit_text_exercise_name)
        setsEditText = dialogView.findViewById(R.id.edit_text_sets)
        repsEditText = dialogView.findViewById(R.id.edit_text_reps)
        restEditText = dialogView.findViewById(R.id.edit_text_rest_time)
        muscleEditText = dialogView.findViewById(R.id.edit_text_target_muscle)
        instructionsEditText = dialogView.findViewById(R.id.edit_text_instructions)
        url1EditText = dialogView.findViewById(R.id.edit_text_image_url1)
        url2EditText = dialogView.findViewById(R.id.edit_text_image_url2)
        url3EditText = dialogView.findViewById(R.id.edit_text_image_url3)
        url4EditText = dialogView.findViewById(R.id.edit_text_image_url4)
        saveButton = dialogView.findViewById(R.id.button_save_exercise)
        closeButton = dialogView.findViewById(R.id.image_view_close_dialog)

        editExerciseDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { editExerciseDialog?.dismiss() }
    }

    private fun showEditExerciseDialog(exercise: Exercise) {
        nameEditText.setText(exercise.name)
        setsEditText.setText(exercise.sets)
        repsEditText.setText(exercise.reps)
        restEditText.setText(exercise.rest_time)
        muscleEditText.setText(exercise.target_muscle)
        instructionsEditText.setText(exercise.instructions)

        val imageUrls = exercise.image_urls
        url1EditText.setText(imageUrls.getOrNull(0) ?: "")
        url2EditText.setText(imageUrls.getOrNull(1) ?: "")
        url3EditText.setText(imageUrls.getOrNull(2) ?: "")
        url4EditText.setText(imageUrls.getOrNull(3) ?: "")

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val sets = setsEditText.text.toString().trim()
            val reps = repsEditText.text.toString().trim()
            val restTime = restEditText.text.toString().trim()
            val targetMuscle = muscleEditText.text.toString().trim()
            val instructions = instructionsEditText.text.toString().trim()

            if (name.isEmpty()) { nameEditText.error = "Required"; return@setOnClickListener }
            if (sets.isEmpty()) { setsEditText.error = "Required"; return@setOnClickListener }
            if (reps.isEmpty()) { repsEditText.error = "Required"; return@setOnClickListener }
            if (restTime.isEmpty()) { restEditText.error = "Required"; return@setOnClickListener }
            if (targetMuscle.isEmpty()) { muscleEditText.error = "Required"; return@setOnClickListener }
            if (instructions.isEmpty()) { instructionsEditText.error = "Required"; return@setOnClickListener }

            val nonEmptyImageUrls = listOfNotNull(
                url1EditText.text.toString().trim().takeIf { it.isNotBlank() },
                url2EditText.text.toString().trim().takeIf { it.isNotBlank() },
                url3EditText.text.toString().trim().takeIf { it.isNotBlank() },
                url4EditText.text.toString().trim().takeIf { it.isNotBlank() }
            )

            if (nonEmptyImageUrls.size < 2) {
                Toast.makeText(context, "Please provide at least two image URLs.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val updatedExercise = hashMapOf(
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
                    firestore.collection("Exercises").document(exercise.exercise_id)
                        .update(updatedExercise as Map<String, Any>).await()
                    Toast.makeText(context, "Exercise updated successfully!", Toast.LENGTH_SHORT)
                        .show()
                    editExerciseDialog?.dismiss()
                    fetchExercises()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error updating exercise: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        editExerciseDialog?.show()
    }

    private fun showDeleteConfirmationDialog(exercise: Exercise) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Exercise")
            .setMessage("Are you sure you want to delete this exercise?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteExercise(exercise)
            }
            .show()
    }

    private fun deleteExercise(exercise: Exercise) {
        lifecycleScope.launch {
            try {
                firestore.collection("Exercises").document(exercise.exercise_id).delete().await()
                Toast.makeText(context, "Exercise deleted successfully", Toast.LENGTH_SHORT).show()
                fetchExercises()
            } catch (e: Exception) {
                Toast.makeText(context, "Error deleting exercise: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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