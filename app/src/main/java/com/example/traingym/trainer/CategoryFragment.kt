package com.example.traingym.trainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_exercise)
        fab.setOnClickListener {
            showAddCategoryDialog()
        }

        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList()) { clickedCategory ->
            val exerciseListFragment = ExercisesListFragment.newInstance(
                clickedCategory.category_id,
                clickedCategory.category_name
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, exerciseListFragment)
                .addToBackStack(null)
                .commit()
        }
        categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }
    }

    private fun showLoading(isLoading: Boolean) {
        lottieAnimationView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun fetchCategories() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("Category").get().await()
                val categories = snapshot.toObjects(ExerciseCategory::class.java)
                categoryAdapter.updateData(categories)
            } catch (e: Exception) {
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val categoryNameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_category_name)
        val categoryDescEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_category_description)
        val saveButton = dialogView.findViewById<Button>(R.id.button_save_category)
        val closeButton = dialogView.findViewById<ImageView>(R.id.image_view_close_dialog)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val name = categoryNameEditText.text.toString().trim()
            val description = categoryDescEditText.text.toString().trim()

            if (name.isEmpty()) {
                categoryNameEditText.error = "Category name is required"
                return@setOnClickListener
            }

            val newCategoryRef = firestore.collection("Category").document()
            val newCategory = hashMapOf(
                "category_id" to newCategoryRef.id,
                "category_name" to name,
                "category_description" to description
            )

            lifecycleScope.launch {
                try {
                    newCategoryRef.set(newCategory).await()
                    Toast.makeText(context, "Category has been saved!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchCategories()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving category: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
    }
}