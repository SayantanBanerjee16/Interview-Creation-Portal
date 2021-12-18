@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
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

    }

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
