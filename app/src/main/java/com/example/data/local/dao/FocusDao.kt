package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.AppRuleEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.UserBadgeEntity
import com.example.data.local.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    // Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestampMillis DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()

    // App Rules
    @Query("SELECT * FROM app_rules ORDER BY isAllowed DESC, category ASC, appName ASC")
    fun getAllAppRules(): Flow<List<AppRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppRule(rule: AppRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppRules(rules: List<AppRuleEntity>)

    @Update
    suspend fun updateAppRule(rule: AppRuleEntity)

    @Query("DELETE FROM app_rules WHERE id = :id")
    suspend fun deleteAppRule(id: Long)

    @Query("DELETE FROM app_rules")
    suspend fun clearAppRules()

    // Badges
    @Query("SELECT * FROM user_badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadges(): Flow<List<UserBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<UserBadgeEntity>)

    @Update
    suspend fun updateBadge(badge: UserBadgeEntity)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
