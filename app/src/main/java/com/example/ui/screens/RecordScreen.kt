package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.SportType
import com.example.data.tracking.TrackingState
import com.example.data.tracking.TrackingStatus
import com.example.ui.components.RouteCanvas
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticGold
import com.example.ui.theme.AthleticNeonGreen
import com.example.ui.theme.AthleticOrange
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val trackingState by viewModel.trackingState.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            viewModel.startWorkout()
        }
    }

    val sportColor = if (trackingState.sportType == SportType.RUN) AthleticOrange else AthleticCyan

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("record_screen")
            .background(Color(0xFF0F1116))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Sport Selector and Simulation Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sport Switcher (Disabled while recording)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = trackingState.sportType == SportType.RUN,
                        onClick = {
                            if (trackingState.status == TrackingStatus.IDLE) {
                                viewModel.setSportType(SportType.RUN)
                            }
                        },
                        label = { Text("Lari", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AthleticOrange,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = trackingState.sportType == SportType.RIDE,
                        onClick = {
                            if (trackingState.status == TrackingStatus.IDLE) {
                                viewModel.setSportType(SportType.RIDE)
                            }
                        },
                        label = { Text("Sepeda", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DirectionsBike,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AthleticCyan,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }

                // Simulation mode switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Simulasi",
                        fontSize = 11.sp,
                        color = if (trackingState.isSimulationMode) AthleticNeonGreen else Color(0xFF6B7280),
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = trackingState.isSimulationMode,
                        onCheckedChange = { viewModel.toggleSimulation(it) },
                        enabled = trackingState.status == TrackingStatus.IDLE,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AthleticNeonGreen
                        ),
                        modifier = Modifier.testTag("simulation_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. GPS / Status Pill
            Surface(
                color = when {
                    trackingState.isSimulationMode -> Color(0xFF2A2715)
                    trackingState.hasGpsFix -> Color(0xFF142B1F)
                    else -> Color(0xFF222630)
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderDefaultsOrNull(trackingState)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (trackingState.hasGpsFix || trackingState.isSimulationMode) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                        contentDescription = null,
                        tint = if (trackingState.isSimulationMode) Color(0xFFFFD54F) else if (trackingState.hasGpsFix) AthleticNeonGreen else Color(0xFF8E95A5),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = when {
                            trackingState.isSimulationMode -> "Mode Uji Simulasi Aktif"
                            trackingState.hasGpsFix -> "GPS Terkunci • Siap Rekam"
                            else -> "Mencari GPS..."
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trackingState.isSimulationMode) Color(0xFFFFD54F) else if (trackingState.hasGpsFix) AthleticNeonGreen else Color(0xFF8E95A5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Giant HUD Metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tracking_hud_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Distance (Primary Metric)
                    Text(
                        text = "JARAK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E95A5),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", trackingState.distanceKm),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 60.sp
                    )
                    Text(
                        text = "KILOMETER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = sportColor,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF232733))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Time
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "WAKTU", fontSize = 10.sp, color = Color(0xFF8E95A5), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = trackingState.formatDuration(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Pace / Speed
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val label = if (trackingState.sportType == SportType.RUN) "PACE SAAT INI" else "KECEPATAN"
                            Text(text = label, fontSize = 10.sp, color = Color(0xFF8E95A5), fontWeight = FontWeight.SemiBold)
                            val value = if (trackingState.sportType == SportType.RUN) {
                                "${trackingState.formatCurrentPace()} /km"
                            } else {
                                String.format(Locale.US, "%.1f km/j", trackingState.currentSpeedKmh)
                            }
                            Text(
                                text = value,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = sportColor
                            )
                        }

                        // Elevation Gain
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ELEVASI", fontSize = 10.sp, color = Color(0xFF8E95A5), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "+${trackingState.elevationGainMeters.roundToInt()}m",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AthleticNeonGreen
                            )
                        }

                        // Calories
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "KALORI", fontSize = 10.sp, color = Color(0xFF8E95A5), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${trackingState.caloriesBurned}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Live Route Map Polyline Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                RouteCanvas(
                    routePoints = trackingState.routePoints,
                    modifier = Modifier.fillMaxSize(),
                    isLiveTracking = trackingState.status == TrackingStatus.RECORDING,
                    primaryColor = sportColor
                )

                // Last split popup toast inside map
                if (trackingState.lastSplitNotification != null && trackingState.status == TrackingStatus.RECORDING) {
                    Surface(
                        color = Color(0xDD000000),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = AthleticGold, modifier = Modifier.size(14.dp))
                            Text(
                                text = trackingState.lastSplitNotification ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Controls Section
            when (trackingState.status) {
                TrackingStatus.IDLE -> {
                    Button(
                        onClick = {
                            if (hasLocationPermission || trackingState.isSimulationMode) {
                                viewModel.startWorkout()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("start_workout_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sportColor
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "MULAI ${if (trackingState.sportType == SportType.RUN) "LARI" else "GOWES"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                TrackingStatus.RECORDING -> {
                    Button(
                        onClick = { viewModel.pauseWorkout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("pause_workout_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Black
                            )
                            Text(
                                text = "JEDA AKTIVITAS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                TrackingStatus.PAUSED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Resume button
                        Button(
                            onClick = { viewModel.resumeWorkout() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(60.dp)
                                .testTag("resume_workout_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AthleticNeonGreen)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LANJUTKAN",
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }

                        // Finish button
                        Button(
                            onClick = { viewModel.requestFinishWorkout() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(60.dp)
                                .testTag("finish_workout_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = sportColor)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELESAI",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        // Discard button
                        OutlinedButton(
                            onClick = { viewModel.discardWorkout() },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(60.dp)
                                .testTag("discard_workout_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF5252)
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Batalkan", tint = Color(0xFFFF5252))
                        }
                    }
                }

                TrackingStatus.STOPPED -> {
                    Button(
                        onClick = { viewModel.requestFinishWorkout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = sportColor)
                    ) {
                        Text("SIMPAN AKTIVITAS", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Finish Save Workout Dialog
        if (showSaveDialog) {
            FinishWorkoutDialog(
                state = trackingState,
                onDismiss = { viewModel.dismissSaveDialog() },
                onSave = { title, notes ->
                    viewModel.confirmSaveWorkout(title, notes)
                },
                onDiscard = {
                    viewModel.discardWorkout()
                }
            )
        }
    }
}

@Composable
fun BorderDefaultsOrNull(state: TrackingState): androidx.compose.foundation.BorderStroke? {
    return if (state.isSimulationMode) {
        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
    } else if (state.hasGpsFix) {
        androidx.compose.foundation.BorderStroke(1.dp, AthleticNeonGreen.copy(alpha = 0.5f))
    } else null
}

@Composable
fun FinishWorkoutDialog(
    state: TrackingState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDiscard: () -> Unit
) {
    val isRun = state.sportType == SportType.RUN
    val defaultTitle = if (isRun) "Lari Keren ${state.formatDuration()}" else "Gowes Sore ${state.formatDuration()}"

    var title by remember { mutableStateOf(defaultTitle) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRun) Icons.Default.DirectionsRun else Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = if (isRun) AthleticOrange else AthleticCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aktivitas Selesai!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Summary Recap Chip
                Surface(
                    color = Color(0xFF1E222A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Jarak", fontSize = 11.sp, color = Color(0xFF8E95A5))
                            Text(
                                text = String.format(Locale.US, "%.2f km", state.distanceKm),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(text = "Durasi", fontSize = 11.sp, color = Color(0xFF8E95A5))
                            Text(
                                text = state.formatDuration(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(text = "Kalori", fontSize = 11.sp, color = Color(0xFF8E95A5))
                            Text(
                                text = "${state.caloriesBurned} kcal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AthleticOrange
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Aktivitas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Perasaan saat latihan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_notes_input"),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, notes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRun) AthleticOrange else AthleticCyan
                ),
                modifier = Modifier.testTag("save_workout_confirm_button")
            ) {
                Text("Simpan ke Riwayat & Leaderboard", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                modifier = Modifier.testTag("discard_dialog_button")
            ) {
                Text("Hapus", color = Color(0xFFFF5252))
            }
        }
    )
}
