package com.example.traingym.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R
import com.example.traingym.trainer.ExerciseCategory

class UserCategoryAdapter(
    private var categories: List<ExerciseCategory>,
    private val onItemClick: (ExerciseCategory) -> Unit,
    private val onInfoClick: (ExerciseCategory) -> Unit
) : RecyclerView.Adapter<UserCategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_category_name)
        val descriptionTextView: TextView = itemView.findViewById(R.id.text_view_category_description)
        val infoButton: ImageView = itemView.findViewById(R.id.image_view_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameTextView.text = category.category_name
        holder.descriptionTextView.text = category.category_description
        holder.itemView.setOnClickListener { onItemClick(category) }
        holder.infoButton.setOnClickListener { onInfoClick(category) }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<ExerciseCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }
} 