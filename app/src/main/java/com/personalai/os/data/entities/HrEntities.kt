package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val department: String?,
    val jobTitle: String?,
    val joiningDate: Long?,
    val status: String   // active | on_leave | resigned | terminated
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val date: Long,
    val status: String   // present | absent | leave | late
)

@Entity(tableName = "leave_requests")
data class LeaveEntity(
    @PrimaryKey val id: String,
    val employeeId: String,
    val startDate: Long,
    val endDate: Long,
    val reason: String?,
    val status: String   // pending | approved | rejected
)

@Entity(tableName = "overtime")
data class OvertimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val date: Long,
    val hours: Double
)
