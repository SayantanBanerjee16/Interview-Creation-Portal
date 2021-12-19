package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import com.sayantanbanerjee.interviewcreationportal.data.Slot
import com.sayantanbanerjee.interviewcreationportal.data.User
import java.text.SimpleDateFormat

class DisplayActivity : AppCompatActivity() {

    private lateinit var meetingRoomNameDisplay: TextView
    private lateinit var timestampStartDisplay: TextView
    private lateinit var timestampEndDisplay: TextView
    private lateinit var usersInDisplayList: RecyclerView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var adapter: UserAdapter

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        meetingRoomNameDisplay = findViewById(R.id.meetingRoomNameDisplay)
        timestampStartDisplay = findViewById(R.id.timestampStartDisplay)
        timestampEndDisplay = findViewById(R.id.timeStampEndDisplay)
        usersInDisplayList = findViewById(R.id.usersInDisplayList)
        editButton = findViewById(R.id.editMeetingButton)
        deleteButton = findViewById(R.id.deleteMeetingButton)


        val intent = intent
        val meetingId = intent.getStringExtra("MEETING_ID")
        val meetingName = intent.getStringExtra("MEETING_NAME")
        val meetingStartTime = intent.getStringExtra("MEETING_START_TIME")
        val meetingEndTime = intent.getStringExtra("MEETING_END_TIME")

        meetingRoomNameDisplay.text = "Meeting Name : $meetingName"
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh.mm aa")
        val convertedStartDate = simpleDateFormat.format(meetingStartTime!!.toLong() * 1000L)
        timestampStartDisplay.text = "Start : $convertedStartDate"

        val convertedEndDate = simpleDateFormat.format(meetingEndTime!!.toLong() * 1000L)
        timestampEndDisplay.text = "End : $convertedEndDate"

        if (NetworkConnectivity.isNetworkAvailable(this))
            meetingId?.let { fetchUserList(it) }
    }

    private fun fetchUserList(meetingID: String) {

        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).child(meetingID).child("users")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val usersList: MutableList<User> = mutableListOf()
                            for (dataSnapshot in snapshot.children) {
                                val id = dataSnapshot.child("id").value.toString()
                                val name = dataSnapshot.child("name").value.toString()
                                val currentUser =
                                    User(id, name, "")
                                usersList.add(currentUser)
                            }

                            adapter =
                                UserAdapter(applicationContext, usersList)
                            usersInDisplayList.adapter = adapter
                            usersInDisplayList.layoutManager =
                                LinearLayoutManager(applicationContext)


                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }
}
