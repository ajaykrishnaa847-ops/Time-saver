package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionType: String = "FOCUS_LOCK", // "FOCUS_LOCK" or "POMODORO"
    val sessionTitle: String = "Study Session",
    val plannedDurationMinutes: Int = 30,
    val actualDurationSeconds: Int = 0,
    val distractionsPrevented: Int = 0,
    val completed: Boolean = true,
    val category: String = "Study", // "Study", "Work", "Reading", "Deep Work"
    val timestampMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
