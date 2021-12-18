package com.sayantanbanerjee.interviewcreationportal

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.Meeting
import com.sayantanbanerjee.interviewcreationportal.data.Slot
import com.sayantanbanerjee.interviewcreationportal.data.User

// This class is used to make the app connection with the firebase realtime database.
class FirebaseConnections {
    companion object {

        // To upload a new user to firebase realtime database
        fun uploadUserToFirebase(context: Context, name: String, email: String) {
            val reference = Firebase.database.reference
            // create the User class
            val userId = System.currentTimeMillis().toString()
            val user = User(userId, name, email)
            // Upload user class to firebase
            reference.child(context.getString(R.string.users)).child(userId).setValue(user)
            // Display Toast to user
            Toast.makeText(
                context, context.getString(R.string.user_uploaded_to_firebase), Toast.LENGTH_LONG
            ).show()
        }

        fun uploadMeetingToFirebase(context: Context, meetingName: String, timestampStart: String, timestampEnd: String) {
            val reference = Firebase.database.reference
            // create the Meeting class
            val meetingId = System.currentTimeMillis().toString()
            val meeting = Meeting(meetingId, meetingName, Slot(timestampStart, timestampEnd))
            // Upload meeting class to firebase
            reference.child(context.getString(R.string.meeting)).child(meetingId).setValue(meeting)
            // Display Toast to user
            Toast.makeText(
                context, context.getString(R.string.meeting_uploaded_to_firebase), Toast.LENGTH_LONG
            ).show()
        }

    }
}
