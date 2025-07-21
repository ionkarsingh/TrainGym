package com.example.traingym.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.example.traingym.trainer.ExerciseCategory
import com.example.traingym.trainer.ExercisesListFragment
import com.example.traingym.user.UserExercisesListFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserExploreExercisesFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: UserCategoryAdapter
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var noCategoriesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_explore_exercises, container, false)

        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)
        noCategoriesTextView = view.findViewById(R.id.text_view_no_categories)

        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = UserCategoryAdapter(
            emptyList(),
            onItemClick = { clickedCategory ->
                val exerciseListFragment = UserExercisesListFragment.newInstance(
                    clickedCategory.category_id,
                    clickedCategory.category_name
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, exerciseListFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onInfoClick = {
                showCategoryDetailsDialog(it)
            }
        )
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
        categoriesRecyclerView.visibility = View.GONE
        noCategoriesTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("Category").get().await()
                val categories = snapshot.toObjects(ExerciseCategory::class.java)

                if (categories.isEmpty()) {
                    noCategoriesTextView.visibility = View.VISIBLE
                    categoriesRecyclerView.visibility = View.GONE
                } else {
                    noCategoriesTextView.visibility = View.GONE
                    categoriesRecyclerView.visibility = View.VISIBLE
                    categoryAdapter.updateData(categories)
                }
            } catch (e: Exception) {
                // Handle error silently or log it
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showCategoryDetailsDialog(category: ExerciseCategory) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_details, null)

        val nameTextView = dialogView.findViewById<TextView>(R.id.text_view_detail_name)
        val descTextView = dialogView.findViewById<TextView>(R.id.text_view_detail_desc)
        val idTextView = dialogView.findViewById<TextView>(R.id.text_view_detail_id)
        val closeButton = dialogView.findViewById<ImageView>(R.id.image_view_close_dialog)

        nameTextView.text = category.category_name
        descTextView.text = category.category_description
        idTextView.text = category.category_id

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
} 