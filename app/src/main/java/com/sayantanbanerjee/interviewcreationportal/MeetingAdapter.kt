package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import java.text.SimpleDateFormat

class MeetingAdapter(private val context: Context, private val meetings: List<Meeting>) :
    RecyclerView.Adapter<MeetingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_meeting, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meeting = meetings[position]
        holder.meetingRoomName.text = meeting.name

        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh.mm aa")
        val convertedStartDate = simpleDateFormat.format(meeting.slot.startStamp.toLong())
        holder.timeStampStart.text = convertedStartDate.toString()

        val convertedEndDate = simpleDateFormat.format(meeting.slot.endStamp.toLong())
        holder.timeStampEnd.text = convertedEndDate.toString()
    }

    override fun getItemCount(): Int {
        return meetings.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val meetingRoomName = view.findViewById<TextView>(R.id.meetingRoomName)!!
        val timeStampStart = view.findViewById<TextView>(R.id.timestampStart)!!
        val timeStampEnd = view.findViewById<TextView>(R.id.timeStampEnd)!!

    }
}
