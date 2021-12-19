@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.sayantanbanerjee.interviewcreationportal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sayantanbanerjee.interviewcreationportal.data.User
import com.sayantanbanerjee.interviewcreationportal.data.UserSlot
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList


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
    private lateinit var insertOrUpdateDisplay: TextView

    // Initialize all the variables which holds date and time
    private var startTimeClicked: Boolean = false
    var dayChosen = -1
    var monthChosen = -1
    var yearChosen = -1
    var startHour = -1
    var startMinute = -1
    var endHour = -1
    var endMinute = -1
    var timeStampStart = ""
    var timeStampEnd = ""
    var selectedUserList: MutableList<User> = mutableListOf()
    var userTimeSlots: MutableList<UserSlot> = mutableListOf()
    var isUpdate = false
    var meetingId = ""
    var initialStartTimestamp = ""
    var initialSelectedUsers: List<User> = emptyList()
    private lateinit var initialSelectedId : ArrayList<String>

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
        insertOrUpdateDisplay = findViewById(R.id.insertOrUpdateDisplay)

        val intent = intent
        val isEditingVersion = intent.getBooleanExtra("EDIT", false)
        if (isEditingVersion) {
            isUpdate = true
            meetingId = intent.getStringExtra("MEETING_ID").toString()
            val meetingName = intent.getStringExtra("MEETING_NAME")
            val meetingStartTime = intent.getStringExtra("MEETING_START_TIME")
            val meetingEndTime = intent.getStringExtra("MEETING_END_TIME")
            initialSelectedId = intent.getStringArrayListExtra("USER_LIST_ID")!!

            updateViewModify(meetingName!!, meetingStartTime!!, meetingEndTime!!)
        }

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
                    // If network connectivity present, first check if any of the contact gets collided with the given timestamp.
                    parseDateTimeToTimeStamp()
                    if (!collisionCheck(selectedUserList)) {
                        // If no collision present, you are good to go.
                        // If it is update, delete the previous stored info,
                        if (isUpdate) {
                            FirebaseConnections.deleteMeetingWhileUpdating(
                                this,
                                meetingId,
                                initialSelectedId,
                                initialStartTimestamp
                            )
                        }
                        // Add the new info to the server
                        val nameOfMeeting = meetingName.editText?.text.toString()
                        FirebaseConnections.uploadMeetingToFirebase(
                            this,
                            nameOfMeeting,
                            selectedUserList,
                            timeStampStart,
                            timeStampEnd
                        )

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
                        Toast.makeText(this, getString(R.string.collision_found), Toast.LENGTH_LONG)
                            .show()
                    }

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

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun updateViewModify(name: String, meetingStartTime: String, meetingEndTime: String) {
        addMeetingButton.text = getString(R.string.UPDATE_MEETING)
        insertOrUpdateDisplay.text = getString(R.string.UPDATE_A_MEETING)
        meetingName.editText?.setText(name)
        initialStartTimestamp = meetingStartTime
        val dayFormat = SimpleDateFormat("dd")
        dayChosen = dayFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val monthFormat = SimpleDateFormat("MM")
        monthChosen = monthFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val yearFormat = SimpleDateFormat("yyyy")
        yearChosen = yearFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        val hourFormat = SimpleDateFormat("HH")
        startHour = hourFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        endHour = hourFormat.format(meetingEndTime.toLong() * 1000L).toInt()
        val minuteFormat = SimpleDateFormat("mm")
        startMinute = minuteFormat.format(meetingStartTime.toLong() * 1000L).toInt()
        endMinute = minuteFormat.format(meetingEndTime.toLong() * 1000L).toInt()
        val dateText: String = "DATE : $dayChosen  /  $monthChosen  /  $yearChosen"
        chooseDateButton.text = dateText
        val startTimeText: String = "$startHour : $startMinute"
        chooseStartTimeButton.text = "START : $startTimeText"
        val endTimeText: String = "$endHour : $endMinute"
        chooseEndTimeButton.text = "END : $endTimeText"
        fetchInitialUserList(meetingId)
    }

    private fun fetchInitialUserList(meetingID: String) {

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
                            }

                            selectedUserList = usersList
                            initialSelectedUsers = usersList

                            Log.i("######" , initialSelectedUsers.toString())

                            adapter =
                                UserAdapter(applicationContext, selectedUserList)
                            recyclerList.adapter = adapter
                            recyclerList.layoutManager = LinearLayoutManager(applicationContext)


                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }

    private fun collisionCheck(selectedUserList: MutableList<User>): Boolean {
        val setsOfChosenID = mutableSetOf<String>()
        for (users in selectedUserList) {
            setsOfChosenID.add(users.id)
        }
        val setsOfAlreadyRegisteredID = mutableSetOf<String>()
        if (isUpdate) {
            for (userId in initialSelectedId) {
                setsOfAlreadyRegisteredID.add(userId)
            }
        }

        val meetingStart = timeStampStart.toLong()
        val meetingEnd = timeStampEnd.toLong()
        for (timeSlots in userTimeSlots) {
            if (isUpdate) {
                if (setsOfAlreadyRegisteredID.contains(timeSlots.id)) {
                    val slotStart = timeSlots.startStamp
                    if (slotStart == initialStartTimestamp) {
                        continue
                    }
                }
            }

            if (setsOfChosenID.contains(timeSlots.id)) {
                val slotStart = timeSlots.startStamp.toLong()
                val slotEnd = timeSlots.endStamp.toLong()
                if (!(meetingEnd < slotStart || meetingStart > slotEnd)) {
                    return true
                }
            }
        }
        return false
    }

    // This function is run to fetch the user list from the server.
    private fun fetchUsersList() {
        userTimeSlots.clear()
        val reference = Firebase.database.reference
        reference.child(getString(R.string.users)).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val usersList: MutableList<User> = mutableListOf()
                        val usersNameList: MutableList<String> = mutableListOf()
                        for (dataSnapshot in snapshot.children) {
                            val id = dataSnapshot.child("id").value.toString()
                            val name = dataSnapshot.child("name").value.toString()
                            val email = dataSnapshot.child("email").value.toString()
                            val currentUser =
                                User(id, name, email)
                            usersList.add(currentUser)
                            usersNameList.add(name)

                            for (timeSnapshot in dataSnapshot.child("slots").children) {
                                val start = timeSnapshot.child("startStamp").value.toString()
                                val end = timeSnapshot.child("endStamp").value.toString()
                                val userSlot = UserSlot(id, start, end)
                                userTimeSlots.add(userSlot)
                            }

                        }

                        dialog(usersNameList, usersList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    // Dialog to display all the users list to choose from.
    private fun dialog(usersNameList: MutableList<String>, usersList: List<User>) {
        LovelyChoiceDialog(this)
            .setTopColorRes(R.color.darkRed)
            .setTitle(R.string.selectContact)
            .setIcon(R.drawable.ic_baseline_person_add_alt_1_24)
            .setItemsMultiChoice(
                usersNameList
            ) { positions, items ->
                selectedUserList.clear()
                for (pos in positions) {
                    selectedUserList.add(usersList.get(pos))
                }

                Log.i("######AAA##" , initialSelectedUsers.toString())

                adapter =
                    UserAdapter(applicationContext, selectedUserList)
                recyclerList.adapter = adapter
                recyclerList.layoutManager = LinearLayoutManager(applicationContext)
                Log.i("######" , selectedUserList.toString())
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

        if (yearChosen == -1 || startHour == -1 || endHour == -1) {
            Toast.makeText(
                this,
                "Recheck all Date and Time fields!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val calenderCurrent = Calendar.getInstance()
        val currentYear = calenderCurrent.get(Calendar.YEAR)
        var currentMonth = calenderCurrent.get(Calendar.MONTH)
        currentMonth++
        val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)
        if (yearChosen < currentYear || (yearChosen == currentYear && monthChosen < currentMonth) || (yearChosen == currentYear && monthChosen == currentMonth && dayChosen < currentDay)) {
            Toast.makeText(
                this,
                "Start Date cannot be lesser than Current Date!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val hourCurrent = calenderCurrent.get(Calendar.HOUR_OF_DAY)
        val minuteCurrent = calenderCurrent.get(Calendar.MINUTE)
        if (yearChosen == currentYear && monthChosen == currentMonth && dayChosen == currentDay) {
            if (startHour < hourCurrent || (hourCurrent == startHour && startMinute < minuteCurrent)) {
                Toast.makeText(
                    this,
                    "Start Time cannot be lesser than Current Time!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        if (selectedUserList.size <= 1) {
            Toast.makeText(
                this,
                "Minimum participants allowed is Two!",
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
        val calenderCurrent = Calendar.getInstance()
        val currentYear = calenderCurrent.get(Calendar.YEAR)
        val currentMonth = calenderCurrent.get(Calendar.MONTH)
        val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)

        if (year < currentYear || (year == currentYear && month < currentMonth) || (year == currentYear && month == currentMonth && dayOfMonth < currentDay)) {
            Toast.makeText(
                this,
                "Start Date cannot be lesser than Current Date!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

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
                val calenderCurrent = Calendar.getInstance()
                val currentYear = calenderCurrent.get(Calendar.YEAR)
                var currentMonth = calenderCurrent.get(Calendar.MONTH)
                currentMonth++
                val currentDay = calenderCurrent.get(Calendar.DAY_OF_MONTH)
                val hourCurrent = calenderCurrent.get(Calendar.HOUR_OF_DAY)
                val minuteCurrent = calenderCurrent.get(Calendar.MINUTE)
                Log.i(
                    "#######",
                    "$currentDay / $currentMonth / $currentYear -- $hourCurrent : $minuteCurrent ---- $hourOfDay : $minute"
                )
                if (yearChosen == currentYear && monthChosen == currentMonth && dayChosen == currentDay) {
                    if (hourOfDay < hourCurrent || (hourCurrent == hourOfDay && minute < minuteCurrent)) {
                        Log.i("#######", "INSIDE")
                        Toast.makeText(
                            this,
                            "Start Time cannot be lesser than Current Time!",
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
