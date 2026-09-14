package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val category: String, // "ESSENTIAL", "LEARNING", "MUSIC", "SOCIAL", "GAMING", "SHORT_VIDEO", "CUSTOM"
    val isAllowed: Boolean, // true = in allowed list, false = in blocked list
    val description: String = "",
    val isSystemProtected: Boolean = false // e.g. Phone, Emergency are protected
)
