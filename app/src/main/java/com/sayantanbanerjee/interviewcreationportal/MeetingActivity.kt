package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.*

// This activity is responsible for creation / updation of a meeting.
class MeetingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var chooseDateButton: Button
    private lateinit var chooseStartTimeButton: Button
    private lateinit var chooseEndTimeButton: Button

    var startTimeClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)
        chooseDateButton = findViewById(R.id.chooseDatePicker)
        chooseStartTimeButton = findViewById(R.id.chooseStartTimePicker)
        chooseEndTimeButton = findViewById(R.id.chooseEndTimePicker)

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

        // Code to choose the end time from time picker dialog.
        chooseEndTimeButton.setOnClickListener {
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

    // Setting of the chosen date from the date picker dialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val dateText: String = "DATE : $dayOfMonth  /  $month  /  $year"
        chooseDateButton.text = dateText
    }

    @SuppressLint("SetTextI18n")
    // Setting of the chosen time from the time picker dialog
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val timeText: String = "$hourOfDay : $minute"
        if (startTimeClicked) {
            chooseStartTimeButton.text = "START : $timeText"
        } else {
            chooseEndTimeButton.text = "END : $timeText"
        }
    }


}