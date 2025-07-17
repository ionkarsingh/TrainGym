package com.example.traingym.trainer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R

class MembersAdapter(
    private var members: List<Member>,
    private val onItemClick: (Member) -> Unit
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.text_view_username)
        private val emailTextView: TextView = itemView.findViewById(R.id.text_view_email)
        private val batchTimingTextView: TextView = itemView.findViewById(R.id.text_view_batch_timing)

        fun bind(member: Member) {
            usernameTextView.text = member.username
            emailTextView.text = member.email
            batchTimingTextView.text = "${member.batch_start_time} to ${member.batch_end_time}"

            itemView.setOnClickListener {
                onItemClick(member)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    fun updateMembers(newList: List<Member>) {
        members = newList
        notifyDataSetChanged()
    }
}
