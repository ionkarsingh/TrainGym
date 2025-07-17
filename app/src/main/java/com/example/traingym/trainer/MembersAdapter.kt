package com.example.traingym.trainer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.traingym.R

class MembersAdapter(
    private val onEditClick: (Member) -> Unit,
    private val onSuspendClick: (Member) -> Unit
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    private var members: List<Member> = emptyList()

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.text_view_username)
        private val emailTextView: TextView = itemView.findViewById(R.id.text_view_email)
        private val batchTimingTextView: TextView = itemView.findViewById(R.id.text_view_batch_timing)
        private val editIcon: ImageView = itemView.findViewById(R.id.image_view_edit)
        private val suspendIcon: ImageView = itemView.findViewById(R.id.image_view_suspend)
        private val statusDot: ImageView = itemView.findViewById(R.id.image_view_status_dot)

        fun bind(member: Member) {
            usernameTextView.text = member.username
            emailTextView.text = member.email
            batchTimingTextView.text = "${member.batch_start_time} to ${member.batch_end_time}"

            editIcon.setOnClickListener { onEditClick(member) }
            suspendIcon.setOnClickListener { onSuspendClick(member) }

            statusDot.setColorFilter(ContextCompat.getColor(itemView.context, R.color.green_status))
            suspendIcon.setImageResource(R.drawable.ic_suspend)
            suspendIcon.contentDescription = "Suspend Member"
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