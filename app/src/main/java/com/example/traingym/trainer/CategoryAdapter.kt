package com.example.traingym.trainer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R

class CategoryAdapter(
    private var categories: List<ExerciseCategory>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_category_name)
        val idTextView: TextView = itemView.findViewById(R.id.text_view_category_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameTextView.text = "Category Name: ${category.category_name}"
        holder.idTextView.text = "ID: ${category.category_id}"
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<ExerciseCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}