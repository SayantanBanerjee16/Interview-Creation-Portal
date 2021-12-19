package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import java.text.SimpleDateFormat

class MeetingAdapter(private val context: Context, private val meetings: List<Meeting>) :
    RecyclerView.Adapter<MeetingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_meeting, parent, false)
        //return ViewHolder(view)
        return ViewHolder(view).listen { pos, type ->
            val item = meetings[pos]
            val intent = Intent(context, DisplayActivity::class.java)
            intent.putExtra("MEETING_ID", item.id)
            intent.putExtra("MEETING_NAME", item.name)
            intent.putExtra("MEETING_START_TIME", item.slot.startStamp)
            intent.putExtra("MEETING_END_TIME", item.slot.endStamp)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meeting = meetings[position]
        holder.meetingRoomName.text = meeting.name

        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh.mm aa")
        val convertedStartDate = simpleDateFormat.format(meeting.slot.startStamp.toLong() * 1000L)
        holder.timeStampStart.text = convertedStartDate.toString()

        val convertedEndDate = simpleDateFormat.format(meeting.slot.endStamp.toLong() * 1000L)
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

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}
