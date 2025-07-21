package com.example.traingym.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.traingym.R
import com.example.traingym.trainer.Exercise

class UserExerciseAdapter(
    private var exercises: List<Exercise>,
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<UserExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_exercise_name)
        val muscleTextView: TextView = itemView.findViewById(R.id.text_view_target_muscle)
        val setsTextView: TextView = itemView.findViewById(R.id.text_view_sets)
        val repsTextView: TextView = itemView.findViewById(R.id.text_view_reps)
        val imageView: ImageView = itemView.findViewById(R.id.image_view_exercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.nameTextView.text = exercise.name
        holder.muscleTextView.text = "Target: ${exercise.target_muscle}"
        holder.setsTextView.text = "Sets: ${exercise.sets}"
        holder.repsTextView.text = "Reps: ${exercise.reps}"
        val lottieDrawable = LottieDrawable()
        holder.itemView.setOnClickListener { onItemClick(exercise) }
        if (exercise.image_urls.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(exercise.image_urls[0])
                .placeholder(lottieDrawable)
                .error(R.color.light_purple_background)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageDrawable(lottieDrawable)
        }
    }

    override fun getItemCount() = exercises.size

    fun updateData(newExercises: List<Exercise>) {
        exercises = newExercises
        notifyDataSetChanged()
    }
} 