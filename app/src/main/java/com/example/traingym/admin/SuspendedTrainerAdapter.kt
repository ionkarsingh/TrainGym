package com.example.traingym.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R

class SuspendedTrainerAdapter(
    private var trainers: List<Trainer>,
    private val onActivateClick: (Trainer) -> Unit
) : RecyclerView.Adapter<SuspendedTrainerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_view_trainer_name)
        val emailTextView: TextView = view.findViewById(R.id.text_view_trainer_email)
        val activateButton: Button = view.findViewById(R.id.button_reactivate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suspended_trainer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trainer = trainers[position]
        holder.nameTextView.text = trainer.username
        holder.emailTextView.text = trainer.email
        holder.activateButton.setOnClickListener { onActivateClick(trainer) }
    }

    override fun getItemCount() = trainers.size

    fun updateData(newTrainers: List<Trainer>) {
        trainers = newTrainers
        notifyDataSetChanged()
    }
}