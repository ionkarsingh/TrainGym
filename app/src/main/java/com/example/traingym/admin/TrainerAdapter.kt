package com.example.traingym.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R
import java.text.SimpleDateFormat
import java.util.Locale

class TrainerAdapter(
    private var trainers: List<Trainer>,
    private val onEditClick: (Trainer) -> Unit,
    private val onSuspendClick: (Trainer) -> Unit
) : RecyclerView.Adapter<TrainerAdapter.TrainerViewHolder>() {

    class TrainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_trainer_name)
        val emailTextView: TextView = itemView.findViewById(R.id.text_view_trainer_email)
        val dateTextView: TextView = itemView.findViewById(R.id.text_view_added_date)
        val editIcon: ImageView = itemView.findViewById(R.id.image_view_edit)
        val suspendIcon: ImageView = itemView.findViewById(R.id.image_view_suspend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trainer, parent, false)
        return TrainerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainerViewHolder, position: Int) {
        val trainer = trainers[position]
        holder.nameTextView.text = trainer.username
        holder.emailTextView.text = trainer.email

        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val dateString = trainer.addedDate?.let { sdf.format(it) } ?: "Date not available"
        holder.dateTextView.text = "Added on: $dateString"

        holder.editIcon.setOnClickListener { onEditClick(trainer) }
        holder.suspendIcon.setOnClickListener { onSuspendClick(trainer) }
    }

    override fun getItemCount() = trainers.size

    fun updateData(newTrainers: List<Trainer>) {
        trainers = newTrainers
        notifyDataSetChanged()
    }
}