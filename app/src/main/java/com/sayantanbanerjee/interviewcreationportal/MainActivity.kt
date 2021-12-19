package com.sayantanbanerjee.interviewcreationportal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import com.sayantanbanerjee.interviewcreationportal.data.Slot

// The activity class of the base screen, where all the scheduled interviews displayed,
// and also have two FAB (floating action button) routing user to Add User Activity and Add/Update Meeting Activity.
class MainActivity : AppCompatActivity() {

    // Variable binding Add User Floating Action Button view to it
    private lateinit var addUserButton: FloatingActionButton
    private lateinit var addMeetingButton: FloatingActionButton
    private lateinit var adapter: MeetingAdapter
    private lateinit var recyclerList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerList =
            findViewById<RecyclerView>(R.id.meetingRooms) as RecyclerView

        // Route User To ADD USER activity, when user clicks on ADD USER Floating Action Button.
        addUserButton = findViewById(R.id.fabAddUser)
        addUserButton.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }

        // Route User To MEETING activity, when user clicks on ADD MEETING Floating Action Button.
        addMeetingButton = findViewById(R.id.fabAddMeeting)
        addMeetingButton.setOnClickListener {
            val intent = Intent(this, MeetingActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if (NetworkConnectivity.isNetworkAvailable(this))
            fetchMeetingList()
    }

    private fun fetchMeetingList() {

        val reference = Firebase.database.reference
        reference.child(getString(R.string.meeting)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val meetingRooms: MutableList<Meeting> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            val id = dataSnapshot.child("id").value.toString()
                            val name = dataSnapshot.child("name").value.toString()
                            val startTimestamp =
                                dataSnapshot.child("slot").child("startStamp").value.toString()
                            val endTimestamp =
                                dataSnapshot.child("slot").child("endStamp").value.toString()
                            val currentMeetingRoom =
                                Meeting(id, name, Slot(startTimestamp, endTimestamp))
                            meetingRooms.add(currentMeetingRoom)
                        }

                        Log.i("###", meetingRooms.size.toString())

                        adapter =
                            MeetingAdapter(applicationContext, meetingRooms)
                        recyclerList.adapter = adapter
                        recyclerList.layoutManager = LinearLayoutManager(applicationContext)


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}
