package com.example.traingym.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.traingym.R
import com.example.traingym.trainer.Exercise
import com.example.traingym.trainer.ImageSliderAdapter
import android.os.Handler
import android.os.Looper

class UserExercisesDemoFragment : Fragment() {

    private lateinit var exercise: Exercise
    private lateinit var viewPager: ViewPager2
    private lateinit var leftButton: View
    private lateinit var rightButton: View
    private var autoSlideHandler: Handler? = null
    private var autoSlideRunnable: Runnable? = null

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
        val view = inflater.inflate(R.layout.fragment_user_exercises_demo, container, false)

        viewPager = view.findViewById(R.id.view_pager_exercise_images)
        leftButton = view.findViewById(R.id.button_slider_left)
        rightButton = view.findViewById(R.id.button_slider_right)
        val imageSliderAdapter = ImageSliderAdapter(exercise.image_urls)
        viewPager.adapter = imageSliderAdapter

        leftButton.setOnClickListener {
            val prev = if (viewPager.currentItem > 0) viewPager.currentItem - 1 else imageSliderAdapter.itemCount - 1
            viewPager.currentItem = prev
        }
        rightButton.setOnClickListener {
            val next = if (viewPager.currentItem < imageSliderAdapter.itemCount - 1) viewPager.currentItem + 1 else 0
            viewPager.currentItem = next
        }

        startAutoSlide(imageSliderAdapter)

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
        stopAutoSlide()
    }

    private fun startAutoSlide(adapter: ImageSliderAdapter) {
        autoSlideHandler = Handler(Looper.getMainLooper())
        autoSlideRunnable = object : Runnable {
            override fun run() {
                if (adapter.itemCount > 1) {
                    val next = if (viewPager.currentItem < adapter.itemCount - 1) viewPager.currentItem + 1 else 0
                    viewPager.currentItem = next
                }
                autoSlideHandler?.postDelayed(this, 3000)
            }
        }
        autoSlideHandler?.postDelayed(autoSlideRunnable!!, 3000)
    }

    private fun stopAutoSlide() {
        autoSlideHandler?.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val ARG_EXERCISE = "exercise"

        fun newInstance(exercise: Exercise): UserExercisesDemoFragment {
            val fragment = UserExercisesDemoFragment()
            val args = Bundle()
            args.putParcelable(ARG_EXERCISE, exercise)
            fragment.arguments = args
            return fragment
        }
    }
} 