package com.sayantanbanerjee.interviewcreationportal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sayantanbanerjee.interviewcreationportal.data.Meeting

class RecyclerAdapter(private val context: Context, private val meetings: List<Meeting>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_meeting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meeting = meetings[position]
        holder.meetingRoomName.text = meeting.name
    }

    override fun getItemCount(): Int {
        return meetings.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val meetingRoomName = view.findViewById<TextView>(R.id.meetingRoomName)!!
    }
}
