package com.example.traingym.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.example.traingym.trainer.Exercise
import com.example.traingym.trainer.ExercisesDemoFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.traingym.user.UserExerciseAdapter
import androidx.appcompat.app.AppCompatActivity

class UserExercisesListFragment : Fragment() {

    private var categoryId: String? = null
    private var categoryName: String? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: UserExerciseAdapter
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var noExercisesTextView: TextView

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
        val view = inflater.inflate(R.layout.fragment_user_exercises_list, container, false)
        exercisesRecyclerView = view.findViewById(R.id.exercises_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)
        noExercisesTextView = view.findViewById(R.id.text_view_no_exercises)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.findViewById<TextView>(R.id.toolbar_title)?.text = categoryName ?: "Exercises"
        fetchExercises()
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as? AppCompatActivity)?.findViewById<TextView>(R.id.toolbar_title)?.text = "Exercises"
    }

    private fun setupRecyclerView() {
        exerciseAdapter = UserExerciseAdapter(emptyList()) { exercise ->
            val fragment = UserExercisesDemoFragment.newInstance(exercise)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
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

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CATEGORY_NAME = "category_name"

        fun newInstance(categoryId: String, categoryName: String): UserExercisesListFragment {
            val fragment = UserExercisesListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_ID, categoryId)
            args.putString(ARG_CATEGORY_NAME, categoryName)
            fragment.arguments = args
            return fragment
        }
    }
} 