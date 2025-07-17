package com.example.traingym.trainer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R

class SuspendedMembersAdapter(
    private val onActivateClick: (Member) -> Unit
) : RecyclerView.Adapter<SuspendedMembersAdapter.ViewHolder>() {

    private var members: List<Member> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.text_view_username)
        val email: TextView = view.findViewById(R.id.text_view_email)
        val batch: TextView = view.findViewById(R.id.text_view_batch_timing)
        val activateIcon: ImageView = view.findViewById(R.id.image_view_activate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suspended_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = members[position]
        holder.username.text = member.username
        holder.email.text = member.email
        holder.batch.text = "${member.batch_start_time} to ${member.batch_end_time}"
        holder.activateIcon.setOnClickListener {
            onActivateClick(member)
        }
    }

    override fun getItemCount() = members.size

    fun updateMembers(newList: List<Member>) {
        this.members = newList
        notifyDataSetChanged()
    }
}