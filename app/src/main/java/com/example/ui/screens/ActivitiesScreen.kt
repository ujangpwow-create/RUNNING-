package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityEntity
import com.example.data.model.SportType
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticOrange
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun ActivitiesScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (ActivityEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val activities by viewModel.activities.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(activities, selectedFilter) {
        when (selectedFilter) {
            "RUN" -> activities.filter { it.sportType == SportType.RUN.name }
            "RIDE" -> activities.filter { it.sportType == SportType.RIDE.name }
            else -> activities
        }
    }

    val totalDistanceKm = filteredList.sumOf { it.distanceMeters } / 1000.0
    val totalTimeSeconds = filteredList.sumOf { it.durationSeconds }
    val totalHours = totalTimeSeconds / 3600
    val totalMins = (totalTimeSeconds % 3600) / 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("activities_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "LOG LATIHAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AthleticOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Riwayat Aktivitas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 2. Summary stats strip
        item {
            Surface(
                color = Color(0xFF161922),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Jarak", fontSize = 11.sp, color = Color(0xFF8E95A5))
                        Text(
                            text = String.format(Locale.US, "%.1f km", totalDistanceKm),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Aktivitas", fontSize = 11.sp, color = Color(0xFF8E95A5))
                        Text(
                            text = "${filteredList.size}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = AthleticOrange
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Durasi", fontSize = 11.sp, color = Color(0xFF8E95A5))
                        Text(
                            text = "${totalHours}j ${totalMins}m",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = AthleticCyan
                        )
                    }
                }
            }
        }

        // 3. Filter chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("Semua (${activities.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF262B36),
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == "RUN",
                    onClick = { selectedFilter = "RUN" },
                    label = { Text("Lari") },
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AthleticOrange,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == "RIDE",
                    onClick = { selectedFilter = "RIDE" },
                    label = { Text("Sepeda") },
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AthleticCyan,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }

        // 4. Activities list
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada aktivitas ditemukan",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mulai rekam lari atau gowes pertama Anda di tab Rekam!",
                            fontSize = 12.sp,
                            color = Color(0xFF8E95A5),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredList) { activity ->
                ActivityCardItem(
                    activity = activity,
                    onClick = { onNavigateToDetail(activity) },
                    onGiveKudos = { viewModel.giveKudosToActivity(activity.id) }
                )
            }
        }
    }
}
