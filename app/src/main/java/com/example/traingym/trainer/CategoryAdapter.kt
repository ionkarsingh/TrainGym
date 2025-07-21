package com.example.traingym.trainer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R

class CategoryAdapter(
    private var categories: List<ExerciseCategory>,
    private val onItemClick: (ExerciseCategory) -> Unit,
    private val onEditClick: (ExerciseCategory) -> Unit,
    private val onDeleteClick: (ExerciseCategory) -> Unit,
    private val onInfoClick: (ExerciseCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_category_name)
        val idTextView: TextView = itemView.findViewById(R.id.text_view_category_id)
        val editIcon: ImageView = itemView.findViewById(R.id.image_view_edit)
        val deleteIcon: ImageView = itemView.findViewById(R.id.image_view_delete)
        val infoIcon: ImageView = itemView.findViewById(R.id.image_view_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameTextView.text = "Category Name: ${category.category_name}"
        holder.idTextView.text = "ID: ${category.category_id}"

        holder.itemView.setOnClickListener { onItemClick(category) }
        holder.editIcon.setOnClickListener { onEditClick(category) }
        holder.deleteIcon.setOnClickListener { onDeleteClick(category) }
        holder.infoIcon.setOnClickListener { onInfoClick(category) }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<ExerciseCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}