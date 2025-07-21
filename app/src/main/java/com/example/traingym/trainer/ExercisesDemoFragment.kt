package com.example.traingym.trainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.traingym.R

class ExercisesDemoFragment : Fragment() {

    private lateinit var exercise: Exercise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exercise = it.getParcelable(ARG_EXERCISE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises_demo, container, false)

        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager_exercise_images)
        val imageSliderAdapter = ImageSliderAdapter(exercise.image_urls)
        viewPager.adapter = imageSliderAdapter

        val exerciseName: TextView = view.findViewById(R.id.text_view_exercise_name)
        exerciseName.text = exercise.name

        val targetMuscle: TextView = view.findViewById(R.id.text_view_target_muscle)
        targetMuscle.text = "Target: ${exercise.target_muscle}"

        val sets: TextView = view.findViewById(R.id.text_view_sets)
        sets.text = "Sets: ${exercise.sets}"

        val reps: TextView = view.findViewById(R.id.text_view_reps)
        reps.text = "Reps: ${exercise.reps}"

        val restTime: TextView = view.findViewById(R.id.text_view_rest_time)
        restTime.text = "Rest: ${exercise.rest_time}"

        val instructions: TextView = view.findViewById(R.id.text_view_instructions)
        instructions.text = exercise.instructions

        return view
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as AppCompatActivity
        activity.findViewById<TextView>(R.id.toolbar_title).text = exercise.name
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        val activity = activity as AppCompatActivity
        activity.findViewById<TextView>(R.id.toolbar_title).text = "Exercises"
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    companion object {
        private const val ARG_EXERCISE = "exercise"

        fun newInstance(exercise: Exercise): ExercisesDemoFragment {
            val fragment = ExercisesDemoFragment()
            val args = Bundle()
            args.putParcelable(ARG_EXERCISE, exercise)
            fragment.arguments = args
            return fragment
        }
    }
}