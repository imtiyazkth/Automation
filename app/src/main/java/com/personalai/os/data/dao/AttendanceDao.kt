package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.personalai.os.data.entities.AttendanceEntity
import com.personalai.os.data.entities.LeaveEntity
import com.personalai.os.data.entities.OvertimeEntity

data class AttendanceSummary(val present: Int, val absent: Int, val onLeave: Int, val late: Int)

@Dao
interface AttendanceDao {
    @Insert
    suspend fun insert(entity: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun forDate(date: Long): List<AttendanceEntity>

    @Query(
        """
        SELECT
          SUM(CASE WHEN status = 'present' THEN 1 ELSE 0 END) as present,
          SUM(CASE WHEN status = 'absent' THEN 1 ELSE 0 END) as absent,
          SUM(CASE WHEN status = 'leave' THEN 1 ELSE 0 END) as onLeave,
          SUM(CASE WHEN status = 'late' THEN 1 ELSE 0 END) as late
        FROM attendance WHERE date = :date
        """
    )
    suspend fun summaryForDate(date: Long): AttendanceSummary

    // Convenience used by HrAgent in this scaffold - swap `date` for
    // "today, in the device's local calendar day" in the real implementation.
    suspend fun todaySummary(): AttendanceSummary = summaryForDate(startOfTodayEpochMillis())

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId")
    suspend fun leavesFor(employeeId: String): List<LeaveEntity>

    @Query("SELECT SUM(hours) FROM overtime WHERE employeeId = :employeeId")
    suspend fun totalOvertimeHours(employeeId: String): Double?
}

private fun startOfTodayEpochMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
