package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.RouteSerializer
import com.example.data.model.FriendUser
import com.example.data.model.SportType
import com.example.ui.components.RouteCanvas
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticGold
import com.example.ui.theme.AthleticNeonGreen
import com.example.ui.theme.AthleticOrange
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToRecord: (SportType) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToActivityDetail: (ActivityEntity) -> Unit
) {
    val activities by viewModel.activities.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val weeklyGoalKm by viewModel.weeklyGoalKm.collectAsState()

    // Calculate this week's user total distance
    val currentUser = friends.find { it.isCurrentUser }
    val thisWeekDistanceKm = currentUser?.totalWeeklyKm ?: 0.0
    val progressRatio = (thisWeekDistanceKm / weeklyGoalKm).coerceIn(0.0, 1.0).toFloat()

    // Top runner in friends
    val topFriend = friends.maxByOrNull { it.totalWeeklyKm }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header with greeting and Free status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selamat Datang di",
                        color = Color(0xFF8E95A5),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "VELORUN",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = AthleticOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AthleticOrange, AthleticGold)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = AthleticOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "100% GRATIS & BEBAS",
                            color = AthleticOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Weekly Target Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_target_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AthleticOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = AthleticOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Target Mingguan Anda",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pekan ini sisa 3 hari",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E95A5)
                                )
                            }
                        }

                        Text(
                            text = "${(progressRatio * 100).roundToInt()}%",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = AthleticOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AthleticOrange,
                        trackColor = Color(0xFF262B36)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tercapai",
                                fontSize = 11.sp,
                                color = Color(0xFF8E95A5)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f km", thisWeekDistanceKm),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lari / Sepeda",
                                fontSize = 11.sp,
                                color = Color(0xFF8E95A5)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", currentUser?.weeklyRunKm ?: 0.0)} / ${String.format(Locale.US, "%.1f", currentUser?.weeklyRideKm ?: 0.0)} km",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = AthleticCyan
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Target",
                                fontSize = 11.sp,
                                color = Color(0xFF8E95A5)
                            )
                            Text(
                                text = "${weeklyGoalKm.roundToInt()} km",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Start Recording Buttons
        item {
            Text(
                text = "Mulai Rekam Aktivitas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button Run
                Button(
                    onClick = { onNavigateToRecord(SportType.RUN) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("quick_start_run_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AthleticOrange
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mulai Lari",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Button Ride
                Button(
                    onClick = { onNavigateToRecord(SportType.RIDE) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("quick_start_ride_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E222A)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(AthleticCyan, Color(0xFF00796B)))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = AthleticCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mulai Gowes",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 4. Leaderboard Community Highlight
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLeaderboard() }
                    .testTag("home_leaderboard_preview_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = AthleticCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Leaderboard Mingguan Teman",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Lihat Semua >",
                            color = AthleticCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (topFriend != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF14171E))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🥇",
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(topFriend.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topFriend.initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topFriend.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Peringkat 1 Pekan Ini",
                                    fontSize = 11.sp,
                                    color = AthleticGold
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f km", topFriend.totalWeeklyKm),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = AthleticNeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ayo kejar ketertinggalan dan kirim Kudos untuk menyemangati teman!",
                        fontSize = 11.sp,
                        color = Color(0xFF8E95A5)
                    )
                }
            }
        }

        // 5. Weekly Community Challenges
        item {
            Text(
                text = "Tantangan Komunitas Minggu Ini",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            ChallengeCard(
                title = "Tantangan 25K Lari Komunitas",
                sport = "Lari",
                currentKm = currentUser?.weeklyRunKm ?: 0.0,
                targetKm = 25.0,
                color = AthleticOrange,
                badge = "🏃 25K Finisher"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ChallengeCard(
                title = "Gran Fondo Mini 50K Gowes",
                sport = "Sepeda",
                currentKm = currentUser?.weeklyRideKm ?: 0.0,
                targetKm = 50.0,
                color = AthleticCyan,
                badge = "🚴 50K Rider"
            )
        }

        // 6. Recent Activities Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terbaru Anda",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (activities.isNotEmpty()) {
                    Text(
                        text = "${activities.size} Tersimpan",
                        fontSize = 12.sp,
                        color = Color(0xFF8E95A5)
                    )
                }
            }
        }

        if (activities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum ada aktivitas tercatat",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tekan tombol Mulai di atas untuk merekam lari atau gowes pertama Anda!",
                            fontSize = 12.sp,
                            color = Color(0xFF8E95A5),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(activities.take(3)) { activity ->
                ActivityCardItem(
                    activity = activity,
                    onClick = { onNavigateToActivityDetail(activity) },
                    onGiveKudos = { viewModel.giveKudosToActivity(activity.id) }
                )
            }
        }
    }
}

@Composable
fun ChallengeCard(
    title: String,
    sport: String,
    currentKm: Double,
    targetKm: Double,
    color: Color,
    badge: String
) {
    val progress = (currentKm / targetKm).coerceIn(0.0, 1.0).toFloat()
    val isCompleted = currentKm >= targetKm

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        color = if (isCompleted) AthleticGold else Color(0xFF8E95A5)
                    )
                }

                Surface(
                    color = if (isCompleted) AthleticNeonGreen.copy(alpha = 0.2f) else Color(0xFF262B36),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isCompleted) "SELESAI ✓" else "${String.format(Locale.US, "%.1f", currentKm)} / ${targetKm.roundToInt()} km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) AthleticNeonGreen else color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color(0xFF262B36)
            )
        }
    }
}

@Composable
fun ActivityCardItem(
    activity: ActivityEntity,
    onClick: () -> Unit,
    onGiveKudos: () -> Unit
) {
    val isRun = activity.sportType == SportType.RUN.name
    val sportColor = if (isRun) AthleticOrange else AthleticCyan
    val distanceKm = activity.distanceMeters / 1000.0
    val durationMin = activity.durationSeconds / 60
    val durationSec = activity.durationSeconds % 60
    val formattedDuration = String.format("%d:%02d", durationMin, durationSec)

    val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
    val dateString = dateFormat.format(Date(activity.timestamp))

    val points = RouteSerializer.deserializeGpsPoints(activity.routeJson)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("activity_card_${activity.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(sportColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRun) Icons.Default.DirectionsRun else Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = sportColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = activity.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateString,
                            fontSize = 11.sp,
                            color = Color(0xFF8E95A5)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF1E222A),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isRun) "LARI" else "GOWES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = sportColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini route preview canvas
            if (points.size >= 2) {
                RouteCanvas(
                    routePoints = points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    primaryColor = sportColor,
                    showGrid = false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Jarak", fontSize = 11.sp, color = Color(0xFF8E95A5))
                    Text(
                        text = String.format(Locale.US, "%.2f km", distanceKm),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(text = "Waktu", fontSize = 11.sp, color = Color(0xFF8E95A5))
                    Text(
                        text = formattedDuration,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = if (isRun) "Pace Rata-rata" else "Kecepatan",
                        fontSize = 11.sp,
                        color = Color(0xFF8E95A5)
                    )
                    val paceText = if (isRun) {
                        val mins = activity.avgPaceSecondsPerKm / 60
                        val secs = activity.avgPaceSecondsPerKm % 60
                        String.format("%d:%02d /km", mins, secs)
                    } else {
                        String.format(Locale.US, "%.1f km/j", activity.avgSpeedKmh)
                    }
                    Text(
                        text = paceText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Elevasi", fontSize = 11.sp, color = Color(0xFF8E95A5))
                    Text(
                        text = "+${activity.elevationGainMeters.roundToInt()}m",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kudos button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onGiveKudos() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Kudos",
                        tint = AthleticOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activity.kudosCount} Kudos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AthleticOrange
                    )
                }

                Text(
                    text = "Detail Lengkap >",
                    fontSize = 12.sp,
                    color = AthleticCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
