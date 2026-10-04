package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ActivityEntity
import com.example.data.local.AppDatabase
import com.example.data.local.RouteSerializer
import com.example.data.model.FriendUser
import com.example.data.model.SportType
import com.example.data.repository.ActivityRepository
import com.example.data.tracking.TrackingEngine
import com.example.data.tracking.TrackingState
import com.example.data.tracking.TrackingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME,
    LEADERBOARD,
    RECORD,
    ACTIVITIES,
    PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ActivityRepository(database.activityDao())
    val trackingEngine = TrackingEngine(application, viewModelScope)

    val activities: StateFlow<List<ActivityEntity>> = repository.allActivities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val friends: StateFlow<List<FriendUser>> = repository.friends

    val trackingState: StateFlow<TrackingState> = trackingEngine.state

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedActivity = MutableStateFlow<ActivityEntity?>(null)
    val selectedActivity: StateFlow<ActivityEntity?> = _selectedActivity.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _weeklyGoalKm = MutableStateFlow(30.0)
    val weeklyGoalKm: StateFlow<Double> = _weeklyGoalKm.asStateFlow()

    init {
        viewModelScope.launch {
            activities.collect { list ->
                repository.syncUserActivitiesToLeaderboard(list)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun viewActivityDetail(activity: ActivityEntity) {
        _selectedActivity.value = activity
    }

    fun clearActivityDetail() {
        _selectedActivity.value = null
    }

    fun setSportType(sportType: SportType) {
        trackingEngine.setSportType(sportType)
    }

    fun toggleSimulation(enabled: Boolean) {
        trackingEngine.toggleSimulationMode(enabled)
    }

    fun startWorkout() {
        trackingEngine.startTracking()
    }

    fun pauseWorkout() {
        trackingEngine.pauseTracking()
    }

    fun resumeWorkout() {
        trackingEngine.resumeTracking()
    }

    fun requestFinishWorkout() {
        trackingEngine.pauseTracking()
        _showSaveDialog.value = true
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
    }

    fun confirmSaveWorkout(title: String, notes: String) {
        val state = trackingEngine.state.value
        val defaultTitle = if (title.isNotBlank()) title else {
            val sportName = if (state.sportType == SportType.RUN) "Lari" else "Gowes"
            "$sportName ${state.formatDuration()}"
        }

        viewModelScope.launch {
            val entity = ActivityEntity(
                title = defaultTitle,
                sportType = state.sportType.name,
                timestamp = System.currentTimeMillis(),
                durationSeconds = state.elapsedSeconds,
                distanceMeters = state.distanceMeters,
                elevationGainMeters = state.elevationGainMeters,
                avgSpeedKmh = state.avgSpeedKmh,
                maxSpeedKmh = state.maxSpeedKmh,
                calories = state.caloriesBurned,
                avgPaceSecondsPerKm = state.avgPaceSecondsPerKm,
                routeJson = RouteSerializer.serializeGpsPoints(state.routePoints),
                splitsJson = RouteSerializer.serializeSplits(state.splits),
                kudosCount = 0,
                userNotes = notes
            )
            repository.saveActivity(entity)
            trackingEngine.reset()
            _showSaveDialog.value = false
            _currentTab.value = AppTab.ACTIVITIES
        }
    }

    fun discardWorkout() {
        trackingEngine.reset()
        _showSaveDialog.value = false
    }

    fun giveKudosToFriend(friendId: String) {
        repository.giveKudosToFriend(friendId)
    }

    fun addFriend(name: String, motto: String) {
        repository.addFriend(name, motto)
    }

    fun giveKudosToActivity(activityId: Long) {
        viewModelScope.launch {
            repository.giveKudosToActivity(activityId)
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
            if (_selectedActivity.value?.id == activity.id) {
                _selectedActivity.value = null
            }
        }
    }

    fun setWeeklyGoal(goalKm: Double) {
        _weeklyGoalKm.value = goalKm
    }
}
