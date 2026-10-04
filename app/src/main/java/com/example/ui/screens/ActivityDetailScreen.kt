package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityEntity
import com.example.data.local.RouteSerializer
import com.example.data.model.SportType
import com.example.ui.components.ElevationChart
import com.example.ui.components.RouteCanvas
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticGold
import com.example.ui.theme.AthleticNeonGreen
import com.example.ui.theme.AthleticOrange
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activity: ActivityEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isRun = activity.sportType == SportType.RUN.name
    val sportColor = if (isRun) AthleticOrange else AthleticCyan

    val points = remember(activity.routeJson) {
        RouteSerializer.deserializeGpsPoints(activity.routeJson)
    }
    val splits = remember(activity.splitsJson) {
        RouteSerializer.deserializeSplits(activity.splitsJson)
    }

    val distanceKm = activity.distanceMeters / 1000.0
    val durationHours = activity.durationSeconds / 3600
    val durationMins = (activity.durationSeconds % 3600) / 60
    val durationSecs = activity.durationSeconds % 60
    val formattedDuration = if (durationHours > 0) {
        String.format("%d:%02d:%02d", durationHours, durationMins, durationSecs)
    } else {
        String.format("%02d:%02d", durationMins, durationSecs)
    }

    val avgPaceFormatted = if (isRun && activity.avgPaceSecondsPerKm > 0) {
        val mins = activity.avgPaceSecondsPerKm / 60
        val secs = activity.avgPaceSecondsPerKm % 60
        String.format("%d:%02d /km", mins, secs)
    } else {
        String.format(Locale.US, "%.1f km/j", activity.avgSpeedKmh)
    }

    // Fastest split
    val fastestSplit = splits.minByOrNull { it.durationSeconds }

    // Relative effort calculation (unlocked free)
    val relativeEffortScore = ((activity.durationSeconds / 60.0) * (if (isRun) 1.2 else 0.8)).roundToInt()
    val cadenceEstimate = if (isRun) "168 spm" else "84 rpm"

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm", Locale("id", "ID"))
    val dateString = dateFormat.format(Date(activity.timestamp))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRun) "Analisis Lari" else "Analisis Bersepeda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF5252))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF101216),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF101216)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("activity_detail_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Title, Sport Pill & Free Premium Banner
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = sportColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isRun) Icons.Default.DirectionsRun else Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = sportColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isRun) "LARI OUTDOOR" else "GOWES OUTDOOR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = sportColor
                                )
                            }
                        }

                        Surface(
                            color = AthleticGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = AthleticGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "FITUR PRO 100% GRATIS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AthleticGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activity.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color.White
                    )

                    Text(
                        text = dateString,
                        fontSize = 12.sp,
                        color = Color(0xFF8E95A5)
                    )

                    if (activity.userNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${activity.userNotes}\"",
                            fontSize = 13.sp,
                            color = Color(0xFFCED4DA),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // 2. High-Res Route Canvas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Peta & Jalur GPS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${points.size} Titik GPS",
                                fontSize = 11.sp,
                                color = Color(0xFF8E95A5)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        RouteCanvas(
                            routePoints = points,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            primaryColor = sportColor,
                            showGrid = true
                        )
                    }
                }
            }

            // 3. Core Telemetry Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Primary row: Distance & Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Jarak Total", fontSize = 11.sp, color = Color(0xFF8E95A5))
                                Text(
                                    text = String.format(Locale.US, "%.2f km", distanceKm),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Total Durasi", fontSize = 11.sp, color = Color(0xFF8E95A5))
                                Text(
                                    text = formattedDuration,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF262C38))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary 4-column metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = if (isRun) "Pace Rata-rata" else "Kecepatan Rata-rata", fontSize = 10.sp, color = Color(0xFF8E95A5))
                                Text(text = avgPaceFormatted, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = sportColor)
                            }
                            Column {
                                Text(text = "Elevasi Gain", fontSize = 10.sp, color = Color(0xFF8E95A5))
                                Text(text = "+${activity.elevationGainMeters.roundToInt()}m", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AthleticNeonGreen)
                            }
                            Column {
                                Text(text = "Kalori", fontSize = 10.sp, color = Color(0xFF8E95A5))
                                Text(text = "${activity.calories} kcal", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFFFB300))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Kecepatan Max", fontSize = 10.sp, color = Color(0xFF8E95A5))
                                Text(text = String.format(Locale.US, "%.1f km/j", activity.maxSpeedKmh), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 4. Elevation Profile Area Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Landscape, contentDescription = null, tint = AthleticCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Profil Ketinggian (Elevasi)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                            Text(text = "Total +${activity.elevationGainMeters.roundToInt()}m", fontSize = 11.sp, color = AthleticCyan, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ElevationChart(
                            points = points,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }

            // 5. Splits Breakdown Table (Kilometer Splits)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Waktu Per Kilometer (Splits)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                            Text(text = "${splits.size} KM", fontSize = 11.sp, color = Color(0xFF8E95A5))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (splits.isEmpty()) {
                            Text(
                                text = "Latihan kurang dari 1 km untuk menghasilkan tabel split.",
                                fontSize = 12.sp,
                                color = Color(0xFF8E95A5),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "KM", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF8E95A5), modifier = Modifier.width(36.dp))
                                Text(text = "PACE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF8E95A5), modifier = Modifier.weight(1f))
                                Text(text = "WAKTU", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF8E95A5), modifier = Modifier.weight(1f))
                                Text(text = "ELEV", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF8E95A5), modifier = Modifier.width(50.dp))
                            }

                            HorizontalDivider(color = Color(0xFF262C38))

                            splits.forEach { split ->
                                val isFastest = fastestSplit?.kilometer == split.kilometer
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(36.dp)) {
                                        Text(
                                            text = "${split.kilometer}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        if (isFastest) {
                                            Text(text = " ⭐", fontSize = 10.sp)
                                        }
                                    }

                                    Text(
                                        text = "${split.formatPace()} /km",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isFastest) AthleticGold else Color.White,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = split.formatDuration(),
                                        fontSize = 13.sp,
                                        color = Color(0xFFCED4DA),
                                        modifier = Modifier.weight(1f)
                                    )

                                    val elevPrefix = if (split.elevationChangeMeters >= 0) "+" else ""
                                    Text(
                                        text = "$elevPrefix${split.elevationChangeMeters.roundToInt()}m",
                                        fontSize = 12.sp,
                                        color = if (split.elevationChangeMeters > 0) AthleticNeonGreen else Color(0xFF8E95A5),
                                        modifier = Modifier.width(50.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Advanced Performance Insights (100% Free)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Metrik Performa Lanjutan (Free Strava Summit)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Relative Effort
                            Surface(
                                color = Color(0xFF202530),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Relative Effort", fontSize = 10.sp, color = Color(0xFF8E95A5))
                                    Text(text = "$relativeEffortScore", fontSize = 20.sp, fontWeight = FontWeight.Black, color = AthleticOrange)
                                    Text(text = "Beban latihan seimbang", fontSize = 10.sp, color = Color(0xFFCED4DA))
                                }
                            }

                            // Cadence
                            Surface(
                                color = Color(0xFF202530),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Estimasi Cadence", fontSize = 10.sp, color = Color(0xFF8E95A5))
                                    Text(text = cadenceEstimate, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AthleticCyan)
                                    Text(text = "Ritme konsisten optimal", fontSize = 10.sp, color = Color(0xFFCED4DA))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Aktivitas Ini?") },
            text = { Text("Aktivitas dan seluruh rute yang tercatat akan dihapus secara permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteActivity(activity)
                        showDeleteConfirm = false
                        onBack()
                    }
                ) {
                    Text("Hapus", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
