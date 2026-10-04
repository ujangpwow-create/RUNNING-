package com.example.data.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.data.model.GpsPoint
import com.example.data.model.SplitData
import com.example.data.model.SportType
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class TrackingStatus {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

data class TrackingState(
    val status: TrackingStatus = TrackingStatus.IDLE,
    val sportType: SportType = SportType.RUN,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val currentPaceSecondsPerKm: Int = 0,
    val avgPaceSecondsPerKm: Int = 0,
    val elevationGainMeters: Double = 0.0,
    val currentAltitude: Double = 0.0,
    val caloriesBurned: Int = 0,
    val routePoints: List<GpsPoint> = emptyList(),
    val splits: List<SplitData> = emptyList(),
    val isSimulationMode: Boolean = false,
    val hasGpsFix: Boolean = false,
    val lastSplitNotification: String? = null
) {
    val distanceKm: Double
        get() = distanceMeters / 1000.0

    fun formatDuration(): String {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatCurrentPace(): String {
        if (currentPaceSecondsPerKm <= 0 || currentPaceSecondsPerKm > 1800) return "--:--"
        val mins = currentPaceSecondsPerKm / 60
        val secs = currentPaceSecondsPerKm % 60
        return String.format("%d:%02d", mins, secs)
    }

    fun formatAvgPace(): String {
        if (avgPaceSecondsPerKm <= 0 || avgPaceSecondsPerKm > 1800) return "--:--"
        val mins = avgPaceSecondsPerKm / 60
        val secs = avgPaceSecondsPerKm % 60
        return String.format("%d:%02d", mins, secs)
    }
}

class TrackingEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var simulationJob: Job? = null

    private var lastLocation: Location? = null
    private var splitStartDistance = 0.0
    private var splitStartTimeSeconds = 0L
    private var splitStartElevation = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (_state.value.status != TrackingStatus.RECORDING) return
            val location = result.lastLocation ?: return
            handleNewLocation(location)
        }
    }

    fun setSportType(sportType: SportType) {
        if (_state.value.status == TrackingStatus.IDLE) {
            _state.value = _state.value.copy(sportType = sportType)
        }
    }

    fun toggleSimulationMode(enabled: Boolean) {
        if (_state.value.status == TrackingStatus.IDLE) {
            _state.value = _state.value.copy(isSimulationMode = enabled)
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (_state.value.status == TrackingStatus.RECORDING) return

        _state.value = _state.value.copy(
            status = TrackingStatus.RECORDING,
            elapsedSeconds = 0L,
            distanceMeters = 0.0,
            currentSpeedKmh = 0.0,
            avgSpeedKmh = 0.0,
            maxSpeedKmh = 0.0,
            elevationGainMeters = 0.0,
            caloriesBurned = 0,
            routePoints = emptyList(),
            splits = emptyList(),
            lastSplitNotification = null
        )

        lastLocation = null
        splitStartDistance = 0.0
        splitStartTimeSeconds = 0L
        splitStartElevation = 0.0

        startTimer()

        if (_state.value.isSimulationMode) {
            startSimulation()
        } else {
            startRealGps()
        }
    }

    fun pauseTracking() {
        if (_state.value.status == TrackingStatus.RECORDING) {
            _state.value = _state.value.copy(
                status = TrackingStatus.PAUSED,
                currentSpeedKmh = 0.0,
                currentPaceSecondsPerKm = 0
            )
            stopRealGps()
            stopSimulation()
        }
    }

    fun resumeTracking() {
        if (_state.value.status == TrackingStatus.PAUSED) {
            _state.value = _state.value.copy(status = TrackingStatus.RECORDING)
            startTimer()
            if (_state.value.isSimulationMode) {
                startSimulation()
            } else {
                startRealGps()
            }
        }
    }

    fun stopTracking() {
        _state.value = _state.value.copy(status = TrackingStatus.STOPPED)
        timerJob?.cancel()
        timerJob = null
        stopRealGps()
        stopSimulation()
    }

    fun reset() {
        stopTracking()
        _state.value = TrackingState(sportType = _state.value.sportType, isSimulationMode = _state.value.isSimulationMode)
        lastLocation = null
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch(Dispatchers.Default) {
            while (_state.value.status == TrackingStatus.RECORDING) {
                delay(1000L)
                val newSeconds = _state.value.elapsedSeconds + 1
                val dist = _state.value.distanceMeters

                // Update averages
                val avgSpd = if (newSeconds > 0) (dist / newSeconds) * 3.6 else 0.0
                val avgPace = if (dist > 50) {
                    ((newSeconds.toDouble() / dist) * 1000.0).roundToInt()
                } else 0

                val calories = calculateCalories(dist, _state.value.sportType)

                _state.value = _state.value.copy(
                    elapsedSeconds = newSeconds,
                    avgSpeedKmh = avgSpd,
                    avgPaceSecondsPerKm = avgPace,
                    caloriesBurned = calories
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRealGps() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(2f)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _state.value = _state.value.copy(hasGpsFix = true)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = _state.value.copy(hasGpsFix = false)
        }
    }

    private fun stopRealGps() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleNewLocation(location: Location) {
        val prev = lastLocation
        var addedDist = 0.0

        if (prev != null) {
            val distArray = FloatArray(1)
            Location.distanceBetween(
                prev.latitude, prev.longitude,
                location.latitude, location.longitude,
                distArray
            )
            addedDist = distArray[0].toDouble()
            // Ignore GPS jitter when user is stationary
            if (addedDist < 0.5) addedDist = 0.0
        }

        val speedMps = if (location.hasSpeed()) location.speed else {
            if (prev != null && addedDist > 0) {
                val timeDiff = (location.time - prev.time) / 1000.0
                if (timeDiff > 0) (addedDist / timeDiff).toFloat() else 0f
            } else 0f
        }

        val speedKmh = speedMps * 3.6
        val currentPace = if (speedMps > 0.4f) {
            (1000.0 / speedMps).roundToInt()
        } else 0

        var elevGain = _state.value.elevationGainMeters
        if (prev != null && location.hasAltitude() && prev.hasAltitude()) {
            val altDiff = location.altitude - prev.altitude
            if (altDiff > 0.8) {
                elevGain += altDiff
            }
        }

        val newDist = _state.value.distanceMeters + addedDist
        val maxSpd = maxOf(_state.value.maxSpeedKmh, speedKmh)

        val newPoint = GpsPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            speedMps = speedMps,
            timestamp = location.time
        )

        lastLocation = location

        _state.value = _state.value.copy(
            distanceMeters = newDist,
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = maxSpd,
            currentPaceSecondsPerKm = currentPace,
            elevationGainMeters = elevGain,
            currentAltitude = location.altitude,
            routePoints = _state.value.routePoints + newPoint,
            hasGpsFix = true
        )

        checkSplits(newDist, elevGain)
    }

    private fun checkSplits(currentDistanceMeters: Double, currentElevation: Double) {
        val currentKmCount = (currentDistanceMeters / 1000.0).toInt()
        val recordedKmCount = _state.value.splits.size

        if (currentKmCount > recordedKmCount) {
            // New km completed!
            val kmIndex = recordedKmCount + 1
            val splitDuration = _state.value.elapsedSeconds - splitStartTimeSeconds
            val splitPace = splitDuration.toInt()
            val splitSpeed = if (splitDuration > 0) (1000.0 / splitDuration) * 3.6 else 0.0
            val splitElev = currentElevation - splitStartElevation

            val newSplit = SplitData(
                kilometer = kmIndex,
                durationSeconds = splitDuration,
                paceSecondsPerKm = splitPace,
                avgSpeedKmh = splitSpeed,
                elevationChangeMeters = splitElev
            )

            splitStartTimeSeconds = _state.value.elapsedSeconds
            splitStartDistance = currentDistanceMeters
            splitStartElevation = currentElevation

            _state.value = _state.value.copy(
                splits = _state.value.splits + newSplit,
                lastSplitNotification = "Kilometer $kmIndex: ${newSplit.formatPace()} /km"
            )
        }
    }

    private fun calculateCalories(distanceMeters: Double, sportType: SportType): Int {
        val km = distanceMeters / 1000.0
        return when (sportType) {
            SportType.RUN -> (km * 68.0).roundToInt() // Avg 70kg runner ~68 kcal/km
            SportType.RIDE -> (km * 32.0).roundToInt() // Avg 70kg cyclist ~32 kcal/km
        }
    }

    // Smooth testing simulation mode for indoor / emulator testing
    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = coroutineScope.launch(Dispatchers.Default) {
            var simStep = 0
            val baseLat = -6.2146
            val baseLng = 106.8451
            var baseAlt = 15.0

            while (_state.value.status == TrackingStatus.RECORDING) {
                delay(1200L)
                simStep++

                val isBike = _state.value.sportType == SportType.RIDE
                // Speed simulation: 11-13 km/h for run, 24-28 km/h for ride
                val speedKmh = if (isBike) (25.0 + Math.sin(simStep * 0.2) * 4.0) else (11.5 + Math.sin(simStep * 0.2) * 1.5)
                val speedMps = (speedKmh / 3.6).toFloat()
                val addedDist = speedMps * 1.2 // 1.2 sec step

                // Curve coordinates
                val angle = simStep * 0.04
                val lat = baseLat + Math.sin(angle) * 0.006 + Math.cos(angle * 2) * 0.002
                val lng = baseLng + Math.cos(angle) * 0.008 + Math.sin(angle * 3) * 0.002
                val alt = baseAlt + Math.sin(angle * 4) * 8.0

                val fakeLocation = Location("simulated").apply {
                    latitude = lat
                    longitude = lng
                    altitude = alt
                    speed = speedMps
                    time = System.currentTimeMillis()
                }

                launch(Dispatchers.Main) {
                    handleNewLocation(fakeLocation)
                }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }
}
