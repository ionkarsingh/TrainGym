package com.example.traingym.trainer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.traingym.R

class ExerciseAdapter(
    private var exercises: List<Exercise>
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var listener: OnExerciseActionsListener? = null

    interface OnExerciseActionsListener {
        fun onEditClicked(exercise: Exercise)
        fun onDeleteClicked(exercise: Exercise)
    }

    fun setOnExerciseActionsListener(listener: OnExerciseActionsListener) {
        this.listener = listener
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_exercise_name)
        val muscleTextView: TextView = itemView.findViewById(R.id.text_view_target_muscle)
        val imageView: ImageView = itemView.findViewById(R.id.image_view_exercise)
        val setsTextView: TextView = itemView.findViewById(R.id.text_view_sets)
        val repsTextView: TextView = itemView.findViewById(R.id.text_view_reps)
        val editButton: ImageView = itemView.findViewById(R.id.image_view_edit)
        val deleteButton: ImageView = itemView.findViewById(R.id.image_view_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.nameTextView.text = exercise.name
        holder.muscleTextView.text = "Target: ${exercise.target_muscle}"
        holder.setsTextView.text = "Sets: ${exercise.sets}"
        holder.repsTextView.text = "Reps: ${exercise.reps}"
        val lottieDrawable = LottieDrawable()
        LottieCompositionFactory.fromRawRes(holder.itemView.context, R.raw.loading_blue)
            .addListener { composition ->
                lottieDrawable.composition = composition
                lottieDrawable.repeatCount = LottieDrawable.INFINITE
                lottieDrawable.playAnimation()
            }

        if (exercise.image_urls.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(exercise.image_urls[0])
                .placeholder(lottieDrawable)
                .error(R.color.light_purple_background)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageDrawable(lottieDrawable)
        }

        holder.editButton.setOnClickListener {
            listener?.onEditClicked(exercise)
        }

        holder.deleteButton.setOnClickListener {
            listener?.onDeleteClicked(exercise)
        }
    }

    override fun getItemCount() = exercises.size

    fun updateData(newExercises: List<Exercise>) {
        exercises = newExercises
        notifyDataSetChanged()
    }
}