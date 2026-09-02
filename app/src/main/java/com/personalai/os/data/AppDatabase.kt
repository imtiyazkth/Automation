package com.personalai.os.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.personalai.os.data.dao.AttendanceDao
import com.personalai.os.data.dao.AuditDao
import com.personalai.os.data.dao.CrmDao
import com.personalai.os.data.dao.PermissionDao
import com.personalai.os.data.entities.AgentPermissionEntity
import com.personalai.os.data.entities.ApprovalEntity
import com.personalai.os.data.entities.AttendanceEntity
import com.personalai.os.data.entities.AuditLogEntity
import com.personalai.os.data.entities.ContactEntity
import com.personalai.os.data.entities.CrmLeadEntity
import com.personalai.os.data.entities.EmployeeEntity
import com.personalai.os.data.entities.LeaveEntity
import com.personalai.os.data.entities.MemoryEntity
import com.personalai.os.data.entities.MessageEntity
import com.personalai.os.data.entities.OrderEntity
import com.personalai.os.data.entities.OvertimeEntity
import com.personalai.os.data.entities.RuleEntity
import com.personalai.os.data.entities.SettingEntity
import com.personalai.os.data.entities.UserEntity
import com.personalai.os.data.entities.WorkflowEntity

/**
 * NOTE ON ENCRYPTION: this declares plain Room for scaffold clarity. For
 * production, wrap the underlying SQLite connection with SQLCipher
 * (net.zetetic:android-database-sqlcipher) and open the database with a key
 * derived from the Android Keystore - never a hardcoded passphrase. See
 * blueprint Part 19 (Security Architecture) / Part 24 (Data Architecture).
 */
@Database(
    entities = [
        UserEntity::class, AgentPermissionEntity::class, AuditLogEntity::class,
        SettingEntity::class, MemoryEntity::class, ApprovalEntity::class,
        ContactEntity::class, MessageEntity::class,
        CrmLeadEntity::class, OrderEntity::class,
        EmployeeEntity::class, AttendanceEntity::class, LeaveEntity::class, OvertimeEntity::class,
        WorkflowEntity::class, RuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
    abstract fun crmDao(): CrmDao
    abstract fun auditDao(): AuditDao
    abstract fun permissionDao(): PermissionDao
}
