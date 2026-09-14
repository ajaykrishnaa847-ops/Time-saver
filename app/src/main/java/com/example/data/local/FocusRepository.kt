package com.example.data.local

import com.example.data.local.dao.FocusDao
import com.example.data.local.model.AppRuleEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.UserBadgeEntity
import com.example.data.local.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class FocusRepository(private val dao: FocusDao) {

    val allSessions: Flow<List<FocusSessionEntity>> = dao.getAllSessions()

    fun getRecentSessions(limit: Int = 10): Flow<List<FocusSessionEntity>> = dao.getRecentSessions(limit)

    val allAppRules: Flow<List<AppRuleEntity>> = dao.getAllAppRules()

    val allBadges: Flow<List<UserBadgeEntity>> = dao.getAllBadges()

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun insertSession(session: FocusSessionEntity): Long = dao.insertSession(session)

    suspend fun deleteSession(id: Long) = dao.deleteSession(id)

    suspend fun clearAllSessions() = dao.clearAllSessions()

    suspend fun insertAppRule(rule: AppRuleEntity): Long = dao.insertAppRule(rule)

    suspend fun updateAppRule(rule: AppRuleEntity) = dao.updateAppRule(rule)

    suspend fun deleteAppRule(id: Long) = dao.deleteAppRule(id)

    suspend fun clearAppRules() = dao.clearAppRules()

    suspend fun updateBadge(badge: UserBadgeEntity) = dao.updateBadge(badge)

    suspend fun updateProfile(profile: UserProfileEntity) = dao.insertOrUpdateProfile(profile)

    suspend fun getProfileSync(): UserProfileEntity? = dao.getUserProfileSync()
}
