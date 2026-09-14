package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val badgeKey: String,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val isUnlocked: Boolean = false,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val unlockedAtMillis: Long = 0
)
