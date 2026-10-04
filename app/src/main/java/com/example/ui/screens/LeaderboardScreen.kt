package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FriendUser
import com.example.ui.theme.AthleticBronze
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticGold
import com.example.ui.theme.AthleticNeonGreen
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.AthleticSilver
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

enum class LeaderboardFilter {
    ALL,
    RUN,
    RIDE
}

@Composable
fun LeaderboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val friends by viewModel.friends.collectAsState()
    var selectedFilter by remember { mutableStateOf(LeaderboardFilter.ALL) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    // Filter and sort friends based on selected category
    val sortedFriends = remember(friends, selectedFilter) {
        friends.sortedByDescending { friend ->
            when (selectedFilter) {
                LeaderboardFilter.ALL -> friend.totalWeeklyKm
                LeaderboardFilter.RUN -> friend.weeklyRunKm
                LeaderboardFilter.RIDE -> friend.weeklyRideKm
            }
        }
    }

    val top1 = sortedFriends.getOrNull(0)
    val top2 = sortedFriends.getOrNull(1)
    val top3 = sortedFriends.getOrNull(2)
    val restFriends = if (sortedFriends.size > 3) sortedFriends.subList(3, sortedFriends.size) else emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("leaderboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with title & motivation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KOMUNITAS & TEMAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AthleticOrange,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Leaderboard Mingguan",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = { showAddFriendDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E222A)),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.testTag("add_friend_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Teman", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Weekly Competition Countdown Banner
            item {
                Surface(
                    color = Color(0xFF161922),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AthleticGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Periode Minggu Ini",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Reset setiap Senin 00:00 • 100% Gratis",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8E95A5)
                                )
                            }
                        }

                        Surface(
                            color = AthleticOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⏱️ Sisa 3 Hari",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AthleticOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. Tab Filter (Semua, Lari, Sepeda)
            item {
                TabRow(
                    selectedTabIndex = selectedFilter.ordinal,
                    containerColor = Color(0xFF14171E),
                    contentColor = AthleticOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedFilter.ordinal]),
                            color = AthleticOrange
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("leaderboard_filter_tabs")
                ) {
                    Tab(
                        selected = selectedFilter == LeaderboardFilter.ALL,
                        onClick = { selectedFilter = LeaderboardFilter.ALL },
                        text = {
                            Text(
                                "Gabungan",
                                fontWeight = if (selectedFilter == LeaderboardFilter.ALL) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFilter == LeaderboardFilter.ALL) AthleticOrange else Color(0xFF8E95A5)
                            )
                        }
                    )
                    Tab(
                        selected = selectedFilter == LeaderboardFilter.RUN,
                        onClick = { selectedFilter = LeaderboardFilter.RUN },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Lari",
                                    fontWeight = if (selectedFilter == LeaderboardFilter.RUN) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilter == LeaderboardFilter.RUN) AthleticOrange else Color(0xFF8E95A5)
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedFilter == LeaderboardFilter.RIDE,
                        onClick = { selectedFilter = LeaderboardFilter.RIDE },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Sepeda",
                                    fontWeight = if (selectedFilter == LeaderboardFilter.RIDE) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilter == LeaderboardFilter.RIDE) AthleticCyan else Color(0xFF8E95A5)
                                )
                            }
                        }
                    )
                }
            }

            // 4. Top 3 Podium
            item {
                PodiumSection(
                    top1 = top1,
                    top2 = top2,
                    top3 = top3,
                    filter = selectedFilter,
                    onGiveKudos = { friendId -> viewModel.giveKudosToFriend(friendId) }
                )
            }

            // 5. Rest of Friends Leaderboard List
            item {
                Text(
                    text = "Peringkat Teman Lainnya",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            itemsIndexed(restFriends) { index, friend ->
                val rank = index + 4
                FriendLeaderboardItem(
                    rank = rank,
                    friend = friend,
                    filter = selectedFilter,
                    onGiveKudos = { viewModel.giveKudosToFriend(friend.id) }
                )
            }
        }

        // Add Friend Dialog
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onAdd = { name, motto ->
                    viewModel.addFriend(name, motto)
                    showAddFriendDialog = false
                }
            )
        }
    }
}

@Composable
fun PodiumSection(
    top1: FriendUser?,
    top2: FriendUser?,
    top3: FriendUser?,
    filter: LeaderboardFilter,
    onGiveKudos: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podium_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 PODIUM MINGGU INI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AthleticGold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Silver)
                if (top2 != null) {
                    PodiumStep(
                        friend = top2,
                        rank = 2,
                        filter = filter,
                        accentColor = AthleticSilver,
                        podiumHeight = 85.dp,
                        crownEmoji = "🥈",
                        onGiveKudos = { onGiveKudos(top2.id) }
                    )
                }

                // Rank 1 (Gold)
                if (top1 != null) {
                    PodiumStep(
                        friend = top1,
                        rank = 1,
                        filter = filter,
                        accentColor = AthleticGold,
                        podiumHeight = 115.dp,
                        crownEmoji = "👑",
                        onGiveKudos = { onGiveKudos(top1.id) }
                    )
                }

                // Rank 3 (Bronze)
                if (top3 != null) {
                    PodiumStep(
                        friend = top3,
                        rank = 3,
                        filter = filter,
                        accentColor = AthleticBronze,
                        podiumHeight = 65.dp,
                        crownEmoji = "🥉",
                        onGiveKudos = { onGiveKudos(top3.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumStep(
    friend: FriendUser,
    rank: Int,
    filter: LeaderboardFilter,
    accentColor: Color,
    podiumHeight: androidx.compose.ui.unit.Dp,
    crownEmoji: String,
    onGiveKudos: () -> Unit
) {
    val displayKm = when (filter) {
        LeaderboardFilter.ALL -> friend.totalWeeklyKm
        LeaderboardFilter.RUN -> friend.weeklyRunKm
        LeaderboardFilter.RIDE -> friend.weeklyRideKm
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        // Crown / Medal
        Text(text = crownEmoji, fontSize = 22.sp)

        Spacer(modifier = Modifier.height(2.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(friend.avatarColorHex))
                .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (friend.isCurrentUser) "Anda" else friend.name,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (friend.isCurrentUser) AthleticOrange else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = String.format(Locale.US, "%.1f km", displayKm),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = accentColor
        )

        // Small Kudos Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onGiveKudos() }
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (friend.kudosGiven) "👏" else "🙌",
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${friend.kudosCount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (friend.kudosGiven) AthleticOrange else Color(0xFF8E95A5)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Pillar
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            Color(0xFF1E222A)
                        )
                    )
                )
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = accentColor
            )
        }
    }
}

@Composable
fun FriendLeaderboardItem(
    rank: Int,
    friend: FriendUser,
    filter: LeaderboardFilter,
    onGiveKudos: () -> Unit
) {
    val displayKm = when (filter) {
        LeaderboardFilter.ALL -> friend.totalWeeklyKm
        LeaderboardFilter.RUN -> friend.weeklyRunKm
        LeaderboardFilter.RIDE -> friend.weeklyRideKm
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_leaderboard_item_${friend.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (friend.isCurrentUser) Color(0xFF241C1A) else Color(0xFF161922)
        ),
        border = if (friend.isCurrentUser) {
            CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AthleticOrange, AthleticGold)))
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF202530)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(friend.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (friend.isCurrentUser) AthleticOrange else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (friend.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AthleticOrange,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ANDA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${friend.weeklyActivityCount} sesi • ${friend.motto}",
                    fontSize = 11.sp,
                    color = Color(0xFF8E95A5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Distance & Kudos
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%.1f km", displayKm),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (friend.isCurrentUser) AthleticOrange else AthleticNeonGreen
                )

                // Interactive Kudos Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (friend.kudosGiven) AthleticOrange.copy(alpha = 0.2f) else Color(0xFF202530))
                        .clickable { onGiveKudos() }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (friend.kudosGiven) "👏" else "🙌",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${friend.kudosCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (friend.kudosGiven) AthleticOrange else Color(0xFF8E95A5)
                    )
                }
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var motto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AthleticOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Teman Komunitas", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ajak teman lari atau gowes untuk bersaing di papan peringkat mingguan secara gratis!",
                    fontSize = 12.sp,
                    color = Color(0xFF8E95A5)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Teman / Atlet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_name_input")
                )

                OutlinedTextField(
                    value = motto,
                    onValueChange = { motto = it },
                    label = { Text("Motto Olahraga / Target") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_motto_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, motto)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange),
                modifier = Modifier.testTag("confirm_add_friend_button")
            ) {
                Text("Tambah", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF8E95A5))
            }
        }
    )
}
