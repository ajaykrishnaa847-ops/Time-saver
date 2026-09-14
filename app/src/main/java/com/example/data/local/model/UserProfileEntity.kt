package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val displayName: String = "Student Scholar",
    val email: String = "user@timesaver.app",
    val dailyGoalMinutes: Int = 180, // 3 hours
    val weeklyGoalMinutes: Int = 1200, // 20 hours
    val currentStreakDays: Int = 6,
    val bestStreakDays: Int = 14,
    val focusPoints: Int = 1420,
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroShortBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isCloudSynced: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
