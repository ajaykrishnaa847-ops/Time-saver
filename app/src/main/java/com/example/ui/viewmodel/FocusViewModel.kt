package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FocusRepository
import com.example.data.local.model.AppRuleEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.UserBadgeEntity
import com.example.data.local.model.UserProfileEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ActiveSessionUiState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val sessionType: String = "FOCUS_LOCK", // "FOCUS_LOCK" or "POMODORO"
    val sessionTitle: String = "Focus Lock",
    val category: String = "Study",
    val totalSeconds: Int = 1800, // 30m default
    val remainingSeconds: Int = 1800,
    val distractionsPrevented: Int = 0,
    val pomodoroCycle: Int = 1,
    val pomodoroPhase: String = "FOCUS" // "FOCUS", "SHORT_BREAK", "LONG_BREAK"
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
        } else 0f

    val remainingFormatted: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FocusRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = FocusRepository(db.focusDao())
    }

    // Active session state
    private val _activeSession = MutableStateFlow(ActiveSessionUiState())
    val activeSession: StateFlow<ActiveSessionUiState> = _activeSession.asStateFlow()

    private var timerJob: Job? = null

    // Room flows
    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appRules: StateFlow<List<AppRuleEntity>> = repository.allAppRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<UserBadgeEntity>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Derived Statistics
    val todayStats = combine(allSessions, _activeSession) { sessions, active ->
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        val todaySessions = sessions.filter { it.timestampMillis >= startOfToday }
        val pastFocusMinutes = todaySessions.sumOf { it.actualDurationSeconds } / 60
        val activeMinutes = if (active.isActive && active.sessionType == "FOCUS_LOCK") {
            (active.totalSeconds - active.remainingSeconds) / 60
        } else 0

        val totalMinutes = pastFocusMinutes + activeMinutes
        val sessionsCount = todaySessions.size + if (active.isActive) 1 else 0
        val distractions = todaySessions.sumOf { it.distractionsPrevented } + active.distractionsPrevented

        Triple(totalMinutes, sessionsCount, distractions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(135, 4, 27))

    fun startFocusLock(durationMinutes: Int, category: String, title: String) {
        val totalSec = durationMinutes * 60
        _activeSession.value = ActiveSessionUiState(
            isActive = true,
            isPaused = false,
            sessionType = "FOCUS_LOCK",
            sessionTitle = title.ifBlank { "$category Focus Lock" },
            category = category,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            distractionsPrevented = 0
        )
        startTimerLoop()
        vibratePhone(100)
    }

    fun startPomodoro(focusMinutes: Int = 25, category: String = "Study") {
        val totalSec = focusMinutes * 60
        _activeSession.value = ActiveSessionUiState(
            isActive = true,
            isPaused = false,
            sessionType = "POMODORO",
            sessionTitle = "Pomodoro #1: $category",
            category = category,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            distractionsPrevented = 0,
            pomodoroCycle = 1,
            pomodoroPhase = "FOCUS"
        )
        startTimerLoop()
        vibratePhone(100)
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_activeSession.value.isActive) {
                delay(1000L)
                if (!_activeSession.value.isPaused) {
                    val currentRemaining = _activeSession.value.remainingSeconds
                    if (currentRemaining > 1) {
                        _activeSession.value = _activeSession.value.copy(
                            remainingSeconds = currentRemaining - 1
                        )
                    } else {
                        // Timer completed!
                        onTimerFinished()
                        break
                    }
                }
            }
        }
    }

    private fun onTimerFinished() {
        val current = _activeSession.value
        vibratePhone(500)

        if (current.sessionType == "POMODORO") {
            handlePomodoroPhaseTransition(current)
        } else {
            // Focus lock completed
            val actualSec = current.totalSeconds
            saveCompletedSession(current, actualSec, completed = true)
            _activeSession.value = current.copy(isActive = false, remainingSeconds = 0)
            updateXpAndStreak(current.totalSeconds / 60)
        }
    }

    private fun handlePomodoroPhaseTransition(current: ActiveSessionUiState) {
        if (current.pomodoroPhase == "FOCUS") {
            // save the 25 min focus chunk
            saveCompletedSession(current, current.totalSeconds, completed = true)
            updateXpAndStreak(current.totalSeconds / 60)

            val nextPhase = if (current.pomodoroCycle % 4 == 0) "LONG_BREAK" else "SHORT_BREAK"
            val breakDuration = if (nextPhase == "LONG_BREAK") 15 * 60 else 5 * 60
            _activeSession.value = current.copy(
                pomodoroPhase = nextPhase,
                totalSeconds = breakDuration,
                remainingSeconds = breakDuration,
                sessionTitle = if (nextPhase == "LONG_BREAK") "Long Break (Great Job!)" else "Short 5m Break"
            )
            startTimerLoop()
        } else {
            // Break finished, start next focus cycle
            val nextCycle = current.pomodoroCycle + 1
            val focusSec = 25 * 60
            _activeSession.value = current.copy(
                pomodoroPhase = "FOCUS",
                pomodoroCycle = nextCycle,
                totalSeconds = focusSec,
                remainingSeconds = focusSec,
                sessionTitle = "Pomodoro #$nextCycle: ${current.category}"
            )
            startTimerLoop()
        }
    }

    fun pauseSession() {
        _activeSession.value = _activeSession.value.copy(isPaused = true)
    }

    fun resumeSession() {
        _activeSession.value = _activeSession.value.copy(isPaused = false)
    }

    fun stopSession(completed: Boolean) {
        timerJob?.cancel()
        val current = _activeSession.value
        if (current.isActive) {
            val elapsedSec = current.totalSeconds - current.remainingSeconds
            if (elapsedSec > 10) {
                saveCompletedSession(current, elapsedSec, completed)
                updateXpAndStreak(elapsedSec / 60)
            }
            _activeSession.value = ActiveSessionUiState(isActive = false)
        }
    }

    fun recordDistractionPrevented() {
        val current = _activeSession.value
        if (current.isActive) {
            _activeSession.value = current.copy(
                distractionsPrevented = current.distractionsPrevented + 1
            )
        }
    }

    private fun saveCompletedSession(state: ActiveSessionUiState, actualSec: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.insertSession(
                FocusSessionEntity(
                    sessionType = state.sessionType,
                    sessionTitle = state.sessionTitle,
                    plannedDurationMinutes = state.totalSeconds / 60,
                    actualDurationSeconds = actualSec,
                    distractionsPrevented = state.distractionsPrevented,
                    completed = completed,
                    category = state.category,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }

    private fun updateXpAndStreak(minutesFocused: Int) {
        viewModelScope.launch {
            val profile = repository.getProfileSync() ?: UserProfileEntity()
            val newXp = profile.focusPoints + (minutesFocused * 10)
            repository.updateProfile(
                profile.copy(
                    focusPoints = newXp,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleAppRule(rule: AppRuleEntity) {
        if (rule.isSystemProtected) return
        viewModelScope.launch {
            repository.updateAppRule(rule.copy(isAllowed = !rule.isAllowed))
        }
    }

    fun addCustomAppRule(name: String, category: String, isAllowed: Boolean) {
        viewModelScope.launch {
            val pkg = "com.user.${name.lowercase().replace(" ", "")}"
            repository.insertAppRule(
                AppRuleEntity(
                    packageName = pkg,
                    appName = name,
                    category = category,
                    isAllowed = isAllowed,
                    description = "Custom configured app rule"
                )
            )
        }
    }

    fun deleteAppRule(id: Long) {
        viewModelScope.launch {
            repository.deleteAppRule(id)
        }
    }

    fun updateGoals(dailyHours: Float, weeklyHours: Float) {
        viewModelScope.launch {
            val profile = repository.getProfileSync() ?: UserProfileEntity()
            repository.updateProfile(
                profile.copy(
                    dailyGoalMinutes = (dailyHours * 60).toInt(),
                    weeklyGoalMinutes = (weeklyHours * 60).toInt(),
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun syncCloudData() {
        viewModelScope.launch {
            val profile = repository.getProfileSync() ?: UserProfileEntity()
            repository.updateProfile(
                profile.copy(
                    isCloudSynced = true,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun loginUser(email: String, name: String) {
        viewModelScope.launch {
            val profile = repository.getProfileSync() ?: UserProfileEntity()
            repository.updateProfile(
                profile.copy(
                    displayName = name.ifBlank { "Alex Student" },
                    email = email.ifBlank { "alex.student@timesaver.app" },
                    isCloudSynced = true,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllSessions()
            repository.clearAppRules()
            AppDatabase.getDatabase(getApplication(), viewModelScope).run {
                // re-seed fresh baseline
                AppDatabase.Companion::class.java // trigger callback logic or insert fresh
            }
        }
    }

    private fun vibratePhone(durationMillis: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMillis)
            }
        } catch (_: Exception) {}
    }
}
