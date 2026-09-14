package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FocusDao
import com.example.data.local.model.AppRuleEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.UserBadgeEntity
import com.example.data.local.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FocusSessionEntity::class,
        AppRuleEntity::class,
        UserBadgeEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timesaver_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.focusDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: FocusDao) {
                // Seed default profile
                dao.insertOrUpdateProfile(
                    UserProfileEntity(
                        id = 1,
                        displayName = "Alex Student",
                        email = "alex.student@timesaver.app",
                        dailyGoalMinutes = 180, // 3h
                        weeklyGoalMinutes = 1200, // 20h
                        currentStreakDays = 6,
                        bestStreakDays = 12,
                        focusPoints = 1420
                    )
                )

                // Seed initial sample focus sessions matching prompt example (Today: 2h 15m = 135 mins, 4 sessions, 27 distractions prevented)
                val now = System.currentTimeMillis()
                val oneHour = 3600 * 1000L
                dao.insertSession(
                    FocusSessionEntity(
                        sessionType = "FOCUS_LOCK",
                        sessionTitle = "Exam Revision: Data Structures",
                        plannedDurationMinutes = 45,
                        actualDurationSeconds = 45 * 60,
                        distractionsPrevented = 8,
                        completed = true,
                        category = "Study",
                        timestampMillis = now - (oneHour * 5),
                        notes = "Very productive, completed binary trees section."
                    )
                )
                dao.insertSession(
                    FocusSessionEntity(
                        sessionType = "POMODORO",
                        sessionTitle = "Calculus Problem Set",
                        plannedDurationMinutes = 25,
                        actualDurationSeconds = 25 * 60,
                        distractionsPrevented = 6,
                        completed = true,
                        category = "Study",
                        timestampMillis = now - (oneHour * 3),
                        notes = "Finished questions 1 to 10."
                    )
                )
                dao.insertSession(
                    FocusSessionEntity(
                        sessionType = "FOCUS_LOCK",
                        sessionTitle = "Research Paper Writing",
                        plannedDurationMinutes = 40,
                        actualDurationSeconds = 40 * 60,
                        distractionsPrevented = 9,
                        completed = true,
                        category = "Deep Work",
                        timestampMillis = now - (oneHour * 2),
                        notes = "Literature review drafted."
                    )
                )
                dao.insertSession(
                    FocusSessionEntity(
                        sessionType = "POMODORO",
                        sessionTitle = "Vocabulary Flashcards",
                        plannedDurationMinutes = 25,
                        actualDurationSeconds = 25 * 60,
                        distractionsPrevented = 4,
                        completed = true,
                        category = "Reading",
                        timestampMillis = now - (oneHour / 2),
                        notes = "Quick review before evening."
                    )
                )

                // Seed default app rules
                val initialRules = listOf(
                    // Essential & Protected
                    AppRuleEntity(packageName = "com.android.dialer", appName = "Phone Calls & Emergency", category = "ESSENTIAL", isAllowed = true, description = "Direct phone calling & 911 emergency services", isSystemProtected = true),
                    AppRuleEntity(packageName = "com.android.contacts", appName = "Emergency Contacts", category = "ESSENTIAL", isAllowed = true, description = "Important family & emergency contacts", isSystemProtected = true),
                    AppRuleEntity(packageName = "com.android.mms", appName = "Important Notifications", category = "ESSENTIAL", isAllowed = true, description = "Critical SMS and system alerts", isSystemProtected = true),
                    // Learning
                    AppRuleEntity(packageName = "com.google.android.youtube", appName = "YouTube (Educational Content)", category = "LEARNING", isAllowed = true, description = "Curated video lectures & study tutorials"),
                    AppRuleEntity(packageName = "com.adobe.reader", appName = "PDF & Document Readers", category = "LEARNING", isAllowed = true, description = "Textbooks, research papers, lecture slides"),
                    AppRuleEntity(packageName = "com.android.calculator2", appName = "Scientific Calculator", category = "LEARNING", isAllowed = true, description = "Math & scientific problem solving"),
                    AppRuleEntity(packageName = "com.google.android.keep", appName = "Study Notes", category = "LEARNING", isAllowed = true, description = "Quick scratchpad & lecture notes"),
                    // Productivity & Music
                    AppRuleEntity(packageName = "com.spotify.music", appName = "Spotify / Focus Music", category = "MUSIC", isAllowed = true, description = "Binaural beats, lofi, ambient focus audio"),

                    // Blocked distracting apps
                    AppRuleEntity(packageName = "com.instagram.android", appName = "Instagram", category = "SOCIAL", isAllowed = false, description = "Stories, reels, feed distractions"),
                    AppRuleEntity(packageName = "com.snapchat.android", appName = "Snapchat", category = "SOCIAL", isAllowed = false, description = "Streaks, snaps, stories"),
                    AppRuleEntity(packageName = "com.facebook.katana", appName = "Facebook", category = "SOCIAL", isAllowed = false, description = "Social feed, groups, notifications"),
                    AppRuleEntity(packageName = "com.zhiliaoapp.musically", appName = "TikTok & Short Video", category = "SHORT_VIDEO", isAllowed = false, description = "Algorithmically addictive endless video loop"),
                    AppRuleEntity(packageName = "com.supercell.clashroyale", appName = "Mobile Gaming Apps", category = "GAMING", isAllowed = false, description = "Casual & competitive mobile games"),
                    AppRuleEntity(packageName = "com.reddit.frontpage", appName = "Reddit & Discussion Forums", category = "SOCIAL", isAllowed = false, description = "Endless scrolling feeds & threads"),
                    AppRuleEntity(packageName = "com.twitter.android", appName = "X / Twitter", category = "SOCIAL", isAllowed = false, description = "News feed & social notifications")
                )
                dao.insertAppRules(initialRules)

                // Seed initial badges
                val initialBadges = listOf(
                    UserBadgeEntity(badgeKey = "FIRST_SESSION", title = "First Focus Session", description = "Complete your first focus session", iconSymbol = "🏆", isUnlocked = true, currentProgress = 1, targetProgress = 1, unlockedAtMillis = now - (86400000L * 6)),
                    UserBadgeEntity(badgeKey = "STREAK_7", title = "7-Day Streak", description = "Maintain a 7-day focus streak", iconSymbol = "🔥", isUnlocked = false, currentProgress = 6, targetProgress = 7),
                    UserBadgeEntity(badgeKey = "HOURS_10", title = "10 Hours Focused", description = "Accumulate 10 hours of focused study", iconSymbol = "📚", isUnlocked = true, currentProgress = 14, targetProgress = 10, unlockedAtMillis = now - 86400000L),
                    UserBadgeEntity(badgeKey = "SESSIONS_50", title = "50 Focus Sessions", description = "Complete 50 focus sessions", iconSymbol = "🚀", isUnlocked = false, currentProgress = 18, targetProgress = 50),
                    UserBadgeEntity(badgeKey = "SHIELD_MASTER", title = "Distraction Shield Master", description = "Prevent 25+ digital distractions", iconSymbol = "🛡️", isUnlocked = true, currentProgress = 27, targetProgress = 25, unlockedAtMillis = now - 3600000L),
                    UserBadgeEntity(badgeKey = "GOAL_MASTER", title = "Goal Crusher", description = "Hit your daily study goal 5 times", iconSymbol = "🎯", isUnlocked = false, currentProgress = 4, targetProgress = 5)
                )
                dao.insertBadges(initialBadges)
            }
        }
    }
}
