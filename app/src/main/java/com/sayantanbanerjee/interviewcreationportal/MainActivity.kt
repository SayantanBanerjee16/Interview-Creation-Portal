package com.sayantanbanerjee.interviewcreationportal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

// The activity class of the base screen, where all the scheduled interviews displayed,
// and also have two FAB (floating action button) routing user to Add User Activity and Add/Update Meeting Activity.
class MainActivity : AppCompatActivity() {

    // Variable binding Add User Floating Action Button view to it
    private lateinit var addUserButton: FloatingActionButton
    private lateinit var addMeetingButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
