package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityEntity
import com.example.data.model.SportType
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticGold
import com.example.ui.theme.AthleticNeonGreen
import com.example.ui.theme.AthleticOrange
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activities by viewModel.activities.collectAsState()
    val weeklyGoalKm by viewModel.weeklyGoalKm.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }

    val totalKm = activities.sumOf { it.distanceMeters } / 1000.0
    val totalSeconds = activities.sumOf { it.durationSeconds }
    val totalHours = totalSeconds / 3600
    val totalMins = (totalSeconds % 3600) / 60
    val totalCalories = activities.sumOf { it.calories }

    // Run PRs
    val runActivities = activities.filter { it.sportType == SportType.RUN.name }
    val fastest5k = runActivities.filter { it.distanceMeters >= 4800 }.minByOrNull { it.avgPaceSecondsPerKm }
    val fastest1k = runActivities.filter { it.distanceMeters >= 900 }.minByOrNull { it.avgPaceSecondsPerKm }

    // Bike PRs
    val bikeActivities = activities.filter { it.sportType == SportType.RIDE.name }
    val longestRide = bikeActivities.maxByOrNull { it.distanceMeters }
    val maxElevationRide = activities.maxByOrNull { it.elevationGainMeters }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AthleticOrange, AthleticGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AT",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Atlet Velorun",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Text(
                        text = "@athlete_saya • Pelari & Goweser Komunitas",
                        fontSize = 12.sp,
                        color = Color(0xFF8E95A5)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = AthleticNeonGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AthleticNeonGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Akun Gratis Permanen • Semua Fitur Terbuka",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AthleticNeonGreen
                            )
                        }
                    }
                }
            }
        }

        // 2. All-Time Career Statistics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Statistik Karir Sepanjang Masa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "Total Jarak",
                            value = String.format(Locale.US, "%.1f km", totalKm),
                            color = AthleticOrange
                        )
                        StatItem(
                            label = "Aktivitas",
                            value = "${activities.size}",
                            color = Color.White
                        )
                        StatItem(
                            label = "Waktu Latihan",
                            value = "${totalHours}j ${totalMins}m",
                            color = AthleticCyan
                        )
                        StatItem(
                            label = "Kalori",
                            value = "$totalCalories",
                            color = AthleticGold
                        )
                    }
                }
            }
        }

        // 3. Weekly Goal Config Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AthleticOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Target Mingguan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Saat ini: ${weeklyGoalKm.roundToInt()} km / minggu",
                                fontSize = 12.sp,
                                color = Color(0xFF8E95A5)
                            )
                        }
                    }

                    Button(
                        onClick = { showGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C38)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("edit_weekly_goal_button")
                    ) {
                        Text("Ubah", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Personal Bests (PRs)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = AthleticGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rekor Pribadi (PRs)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Surface(
                            color = AthleticGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GRATIS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = AthleticGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PRItem(
                        title = "1K Tercepat (Lari)",
                        value = if (fastest1k != null) "${fastest1k.avgPaceSecondsPerKm / 60}:${String.format("%02d", fastest1k.avgPaceSecondsPerKm % 60)} /km" else "Belum tercatat",
                        sport = "RUN"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PRItem(
                        title = "5K Tercepat (Lari)",
                        value = if (fastest5k != null) "${fastest5k.durationSeconds / 60}m ${fastest5k.durationSeconds % 60}s" else "Belum tercatat",
                        sport = "RUN"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PRItem(
                        title = "Gowes Terjauh",
                        value = if (longestRide != null) String.format(Locale.US, "%.1f km", longestRide.distanceMeters / 1000.0) else "Belum tercatat",
                        sport = "RIDE"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PRItem(
                        title = "Tanjakan Tertinggi",
                        value = if (maxElevationRide != null) "+${maxElevationRide.elevationGainMeters.roundToInt()}m" else "Belum tercatat",
                        sport = "ELEV"
                    )
                }
            }
        }

        // 5. Trophy Badges Collection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AthleticGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Koleksi Lencana & Trofi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(text = "4 Terbuka", fontSize = 11.sp, color = AthleticNeonGreen, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BadgeItem(icon = "🏃", name = "First 5K", unlocked = activities.any { it.distanceMeters >= 5000 })
                        BadgeItem(icon = "🚴", name = "Gowes 20K", unlocked = activities.any { it.sportType == SportType.RIDE.name && it.distanceMeters >= 20000 })
                        BadgeItem(icon = "⛰️", name = "Raja Nanjak", unlocked = activities.any { it.elevationGainMeters >= 100 })
                        BadgeItem(icon = "👏", name = "Kudos Master", unlocked = true)
                    }
                }
            }
        }
    }

    // Set Goal Dialog
    if (showGoalDialog) {
        var sliderValue by remember { mutableStateOf(weeklyGoalKm.toFloat()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Atur Target Mingguan") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${sliderValue.roundToInt()} km / minggu",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = AthleticOrange
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 10f..100f,
                        steps = 17, // every 5 km
                        colors = SliderDefaults.colors(
                            thumbColor = AthleticOrange,
                            activeTrackColor = AthleticOrange
                        )
                    )
                    Text(
                        text = "Geser untuk menyesuaikan target jarak lari dan bersepeda mingguan Anda.",
                        fontSize = 11.sp,
                        color = Color(0xFF8E95A5)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setWeeklyGoal(sliderValue.toDouble())
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF8E95A5))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun PRItem(title: String, value: String, sport: String) {
    Surface(
        color = Color(0xFF202530),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (sport) {
                        "RUN" -> "🏃"
                        "RIDE" -> "🚴"
                        else -> "⛰️"
                    },
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AthleticOrange)
        }
    }
}

@Composable
fun BadgeItem(icon: String, name: String, unlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (unlocked) Color(0xFF262E3C) else Color(0xFF1E222A))
                .border(
                    width = if (unlocked) 2.dp else 1.dp,
                    color = if (unlocked) AthleticGold else Color(0xFF2C323E),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (unlocked) Color.White else Color(0xFF6B7280)
        )
    }
}
