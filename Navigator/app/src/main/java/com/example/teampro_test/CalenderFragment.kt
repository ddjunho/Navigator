package com.example.teampro_test

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.islandparadise14.mintable.MinTimeTableView
import com.islandparadise14.mintable.model.ScheduleDay
import com.islandparadise14.mintable.model.ScheduleEntity
import com.islandparadise14.mintable.tableinterface.OnScheduleLongClickListener
import kotlin.random.Random

class CalenderFragment : Fragment() {

    private lateinit var timeTableView: MinTimeTableView
    private val scheduleList = arrayListOf<Schedule>()
    private val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calender, container, false)

        timeTableView = view.findViewById(R.id.table)

        timeTableView.initTable(days)

        // Sample schedule
        val schedule = Schedule(
            id = 1, name = "Database", room = "IT Building 301", day = "Tuesday",
            startTime = "08:20", endTime = "10:30", backgroundColor = "#73fcae68"
        )
        scheduleList.add(schedule)
        updateTableWithSchedules()

        val addButton: Button = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            showAddDialog()
        }

        timeTableView.setOnScheduleLongClickListener(object : OnScheduleLongClickListener {
            override fun scheduleLongClicked(entity: ScheduleEntity) {
                // Find the corresponding Schedule object from your custom list using the originId
                val schedule = scheduleList.find { it.id == entity.originId }

                if (schedule != null) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("작업 선택")
                        .setMessage("이 스케줄을 편집하거나 삭제하시겠습니까?")
                        .setPositiveButton("편집") { _, _ ->
                            showEditDialog(schedule)
                        }
                        .setNegativeButton("삭제") { _, _ ->
                            AlertDialog.Builder(requireContext())
                                .setTitle("스케줄 삭제")
                                .setMessage("정말로 이 스케줄을 삭제하시겠습니까?")
                                .setPositiveButton("삭제") { _, _ ->
                                    // Remove the Schedule object from the custom list
                                    scheduleList.remove(schedule)
                                    updateTableWithSchedules() // Update the timetable view
                                }
                                .setNegativeButton("취소", null)
                                .show()
                        }
                        .setNeutralButton("취소", null)
                        .show()
                }
            }
        })


        return view
    }
    private fun getScheduleDayFromString(day: String): Int {
        return when (day) {
            "Mon" -> ScheduleDay.MONDAY
            "Tue" -> ScheduleDay.TUESDAY
            "Wed" -> ScheduleDay.WEDNESDAY
            "Thu" -> ScheduleDay.THURSDAY
            "Fri" -> ScheduleDay.FRIDAY
            "Sat" -> ScheduleDay.SATURDAY
            "Sun" -> ScheduleDay.SUNDAY
            else -> ScheduleDay.MONDAY // Default to Monday or handle the error appropriately
        }
    }
    private fun updateTableWithSchedules() {
        val entities = scheduleList.map { schedule ->
            ScheduleEntity(
                originId = schedule.id,
                scheduleName = schedule.name,
                roomInfo = schedule.room,
                scheduleDay = getScheduleDayFromString(schedule.day), // Use the mapping function here
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                backgroundColor = schedule.backgroundColor,
                textColor = schedule.textColor
            )
        }
        timeTableView.updateSchedules(entities as ArrayList<ScheduleEntity>)
    }

    private fun showEditDialog(schedule: Schedule) {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_edit_schedule, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.schedule_name)
        val roomInfoInput = dialogView.findViewById<EditText>(R.id.room_info)
        val daySpinner = dialogView.findViewById<Spinner>(R.id.day_spinner)
        val startTimePicker = dialogView.findViewById<TimePicker>(R.id.start_time)
        val endTimePicker = dialogView.findViewById<TimePicker>(R.id.end_time)

        val daysAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = daysAdapter

        nameInput.setText(schedule.name)
        roomInfoInput.setText(schedule.room)
        daySpinner.setSelection(days.indexOf(schedule.day))
        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)

        val startTime = schedule.startTime.split(":")
        val endTime = schedule.endTime.split(":")
        startTimePicker.hour = startTime[0].toInt()
        startTimePicker.minute = startTime[1].toInt()
        endTimePicker.hour = endTime[0].toInt()
        endTimePicker.minute = endTime[1].toInt()

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("스케줄 편집")
            .setView(dialogView)
            .setPositiveButton("저장") { dialog, _ ->
                val updatedName = nameInput.text.toString()
                val updatedRoomInfo = roomInfoInput.text.toString()
                val selectedDay = daySpinner.selectedItem.toString()
                val updatedStartTime = "${startTimePicker.hour}:${startTimePicker.minute}"
                val updatedEndTime = "${endTimePicker.hour}:${endTimePicker.minute}"

                schedule.name = updatedName
                schedule.room = updatedRoomInfo
                schedule.day = selectedDay
                schedule.startTime = updatedStartTime
                schedule.endTime = updatedEndTime

                updateTableWithSchedules()
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showAddDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_edit_schedule, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.schedule_name)
        val roomInfoInput = dialogView.findViewById<EditText>(R.id.room_info)
        val daySpinner = dialogView.findViewById<Spinner>(R.id.day_spinner)
        val startTimePicker = dialogView.findViewById<TimePicker>(R.id.start_time)
        val endTimePicker = dialogView.findViewById<TimePicker>(R.id.end_time)

        val daysAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = daysAdapter

        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("스케줄 추가")
            .setView(dialogView)
            .setPositiveButton("추가") { dialog, _ ->
                val name = nameInput.text.toString()
                val roomInfo = roomInfoInput.text.toString()
                val selectedDay = daySpinner.selectedItem.toString()
                val startHour = startTimePicker.hour
                val startMinute = startTimePicker.minute
                val endHour = endTimePicker.hour
                val endMinute = endTimePicker.minute

                if (isValidTime(startHour, startMinute, endHour, endMinute)) {
                    val randomColor = getRandomColor()
                    val newSchedule = Schedule(
                        id = scheduleList.size + 1,
                        name = name,
                        room = roomInfo,
                        day = selectedDay,
                        startTime = String.format("%02d:%02d", startHour, startMinute),
                        endTime = String.format("%02d:%02d", endHour, endMinute),
                        backgroundColor = randomColor
                    )
                    scheduleList.add(newSchedule)
                    updateTableWithSchedules()
                    dialog.dismiss()
                } else {
                    showErrorDialog("종료 시간은 시작 시간보다 나중이어야 합니다.")
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun isValidTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
        return (endHour > startHour) || (endHour == startHour && endMinute > startMinute)
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("오류")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun getRandomColor(): String {
        val random = Random.Default
        val r = random.nextInt(0, 256)
        val g = random.nextInt(0, 256)
        val b = random.nextInt(0, 256)
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
data class Schedule(
    val id: Int,                    // 고유 ID
    var name: String,               // 일정 이름
    var room: String,               // 방 정보
    var day: String,                // 요일
    var startTime: String,          // 시작 시간
    var endTime: String,            // 종료 시간
    var backgroundColor: String = "#FFFFFF", // 배경 색상
    var textColor: String = "#000000"        // 글자 색상
)
