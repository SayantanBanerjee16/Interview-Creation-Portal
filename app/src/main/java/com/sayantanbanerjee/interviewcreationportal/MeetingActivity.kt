@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.User
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


// This activity is responsible for creation / updation of a meeting.
class MeetingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    // Initialize all the variables which holds the views
    private lateinit var meetingName: TextInputLayout
    private lateinit var chooseDateButton: Button
    private lateinit var chooseStartTimeButton: Button
    private lateinit var chooseEndTimeButton: Button
    private lateinit var addMeetingButton: Button
    private lateinit var addUserToTheMeeting: Button
    private lateinit var adapter: UserAdapter
    private lateinit var recyclerList: RecyclerView

    // Initialize all the variables which holds date and time
    var startTimeClicked: Boolean = false
    var dayChosen = -1
    var monthChosen = -1
    var yearChosen = -1
    var startHour = -1
    var startMinute = -1
    var endHour = -1
    var endMinute = -1
    var timeStampStart = ""
    var timeStampEnd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)
        meetingName = findViewById(R.id.nameOfMeeting)
        chooseDateButton = findViewById(R.id.chooseDatePicker)
        chooseStartTimeButton = findViewById(R.id.chooseStartTimePicker)
        chooseEndTimeButton = findViewById(R.id.chooseEndTimePicker)
        addMeetingButton = findViewById(R.id.addMeetingButton)
        addUserToTheMeeting = findViewById(R.id.addUserToMeetingButton)
        recyclerList =
            findViewById<RecyclerView>(R.id.usersInInterview) as RecyclerView

        // Code to choose the date from date picker dialog.
        chooseDateButton.setOnClickListener {
            val calendarDate: Calendar = Calendar.getInstance()
            val datePickerDialog =
                DatePickerDialog(
                    this@MeetingActivity,
                    this@MeetingActivity,
                    calendarDate.get(Calendar.YEAR),
                    calendarDate.get(Calendar.MONTH),
                    calendarDate.get(Calendar.DAY_OF_MONTH)
                )
            datePickerDialog.show()
        }

        // Code to choose the start time from time picker dialog.
        chooseStartTimeButton.setOnClickListener {
            if (dayChosen == -1) {
                Toast.makeText(
                    this,
                    "First Select Date before selecting start time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startTimeClicked = true
                val calendarStartTime: Calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this@MeetingActivity,
                    this@MeetingActivity,
                    calendarStartTime.get(Calendar.HOUR),
                    calendarStartTime.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }

        }

        // Code to choose the end time from time picker dialog.
        chooseEndTimeButton.setOnClickListener {
            if (startHour == -1) {
                Toast.makeText(
                    this,
                    "Choose Start Time before choosing End Time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                startTimeClicked = false
                val calendarEndTime: Calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this@MeetingActivity,
                    this@MeetingActivity,
                    calendarEndTime.get(Calendar.HOUR),
                    calendarEndTime.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }
        }

        // On clicking the button to add meeting, it will first validate the input fields,
        // and upon passing validation, it will add the meeting on the firebase.
        addMeetingButton.setOnClickListener {
            if (validationOfFields()) {
                if (NetworkConnectivity.isNetworkAvailable(this)) {
                    // If network connectivity present, then update the meeting to server.
                    val nameOfMeeting = meetingName.editText?.text.toString()
                    parseDateTimeToTimeStamp()
                    FirebaseConnections.uploadMeetingToFirebase(
                        this,
                        nameOfMeeting,
                        timeStampStart,
                        timeStampEnd
                    )
                    Handler(Looper.getMainLooper()).postDelayed({
                        // close the activity after 0.5 second
                        finish()
                    }, 500)
                } else {
                    Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show()
                }
            }
        }

        // First it will fetch the user details from the server and display in a list.
        // From the list, it will display in a recycler view,
        // then afterwards it will update at the firebase.
        addUserToTheMeeting.setOnClickListener {
            if (NetworkConnectivity.isNetworkAvailable(this))
                fetchUsersList()
            else {
                Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun fetchUsersList() {
        val reference = Firebase.database.reference
        reference.child(getString(R.string.users)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val usersList: MutableList<User> = mutableListOf()
                        val usersNameList: MutableList<String> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            Log.i("###", dataSnapshot.toString())
                            val id = dataSnapshot.child("id").value.toString()
                            val name = dataSnapshot.child("name").value.toString()
                            val email = dataSnapshot.child("email").value.toString()
                            val currentUser =
                                User(id, name, email)
                            usersList.add(currentUser)
                            usersNameList.add(name)
                        }

                        dialog(usersNameList, usersList)


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun dialog(usersNameList: MutableList<String>, usersList: List<User>) {
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.darkRed)
            .setTitle(R.string.selectContact)
            .setIcon(R.drawable.ic_baseline_person_add_alt_1_24)
            .setItemsMultiChoice(
                usersNameList
            ) { positions, items ->
                val selectedUserList: MutableList<User> = mutableListOf()
                for (pos in positions) {
                    selectedUserList.add(usersList.get(pos))
                }
                adapter =
                    UserAdapter(applicationContext, selectedUserList)
                recyclerList.adapter = adapter
                recyclerList.layoutManager = LinearLayoutManager(applicationContext)
                Toast.makeText(
                    this@MeetingActivity,
                    "Contact List Updated",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            .setConfirmButtonText(R.string.confirm)
            .show()

    }

    // Validation of fields before uploading in the server.
    private fun validationOfFields(): Boolean {
        val nameOfMeeting = meetingName.editText?.text.toString()
        if (nameOfMeeting == "") {
            Toast.makeText(
                this,
                "Choose Name!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        if (endHour == -1) {
            Toast.makeText(
                this,
                "Recheck all Date and Time fields!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }

    // Conversion of start dateTime and end dateTime to EPOCH timeStamp
    private fun parseDateTimeToTimeStamp() {

        val dateTimeStart =
            LocalDateTime.of(yearChosen, monthChosen, dayChosen, startHour, startMinute, 0)
        timeStampStart =
            dateTimeStart.atZone(ZoneOffset.ofHoursMinutes(5, 30)).toEpochSecond().toString()

        val dateTimeEnd =
            LocalDateTime.of(yearChosen, monthChosen, dayChosen, endHour, endMinute, 0)
        timeStampEnd =
            dateTimeEnd.atZone(ZoneOffset.ofHoursMinutes(5, 30)).toEpochSecond().toString()

    }

    // Setting of the chosen date from the date picker dialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        yearChosen = year
        monthChosen = month + 1
        dayChosen = dayOfMonth
        val dateText: String = "DATE : $dayOfMonth  /  $monthChosen  /  $year"
        chooseDateButton.text = dateText
    }

    @SuppressLint("SetTextI18n")
    // Setting of the chosen time from the time picker dialog
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val timeText: String = "$hourOfDay : $minute"
        if (startTimeClicked) {
            if (endHour != -1) {
                if (endHour < hourOfDay || (endHour == hourOfDay && endMinute <= minute)) {
                    Toast.makeText(
                        this,
                        "Start Time cannot be lesser than End Time!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    startHour = hourOfDay
                    startMinute = minute
                    chooseStartTimeButton.text = "START : $timeText"
                }
            } else {
                startHour = hourOfDay
                startMinute = minute
                chooseStartTimeButton.text = "START : $timeText"
            }

        } else {
            if (startHour > hourOfDay || (startHour == hourOfDay && startMinute >= minute)) {
                Toast.makeText(
                    this,
                    "Start Time cannot be greater than End Time!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                endHour = hourOfDay
                endMinute = minute
                chooseEndTimeButton.text = "END : $timeText"
            }

        }

    }

}
