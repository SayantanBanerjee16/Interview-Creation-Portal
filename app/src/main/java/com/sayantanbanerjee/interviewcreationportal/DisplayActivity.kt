package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.User
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import java.text.SimpleDateFormat


class DisplayActivity : AppCompatActivity() {

    private lateinit var meetingRoomNameDisplay: TextView
    private lateinit var timestampStartDisplay: TextView
    private lateinit var timestampEndDisplay: TextView
    private lateinit var usersInDisplayList: RecyclerView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var adapter: UserAdapter

    private lateinit var selectedUserList: MutableList<User>
    private var selectedUserId : ArrayList<String> = arrayListOf()

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

        editButton.setOnClickListener {
            val intent = Intent(this, MeetingActivity::class.java)
            intent.putExtra("EDIT", true)
            intent.putExtra("MEETING_ID", meetingId)
            intent.putExtra("MEETING_NAME", meetingName)
            intent.putExtra("MEETING_START_TIME", meetingStartTime)
            intent.putExtra("MEETING_END_TIME", meetingEndTime)
            intent.putStringArrayListExtra("USER_LIST_ID",selectedUserId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.indigo)
                .setButtonsColorRes(R.color.darkDeepOrange)
                .setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_heading)
                .setPositiveButton(
                    R.string.ok,
                    View.OnClickListener {
                        if (NetworkConnectivity.isNetworkAvailable(this)) {
                            meetingId?.let { meetingId ->
                                FirebaseConnections.deleteMeeting(
                                    applicationContext,
                                    meetingId, selectedUserList, meetingStartTime
                                )
                            }
                            Toast.makeText(
                                applicationContext,
                                "Meeting successfully deleted!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                // close the activity after 0.5 second
                                val mainActivityIntent =
                                    Intent(applicationContext, MainActivity::class.java)
                                mainActivityIntent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(mainActivityIntent)
                                finishAndRemoveTask()
                            }, 500)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "No Network Present. Aborting!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    })
                .show()
        }
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
                                val id = dataSnapshot.child("UID").value.toString()
                                val name = dataSnapshot.child("name").value.toString()
                                val currentUser =
                                    User(id, name, "")
                                usersList.add(currentUser)
                                selectedUserId.add(id)
                            }

                            selectedUserList = usersList

                            adapter =
                                UserAdapter(applicationContext, selectedUserList)
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
