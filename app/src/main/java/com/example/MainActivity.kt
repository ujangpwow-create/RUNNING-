package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.SportType
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.ActivityDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecordScreen
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VelorunMainApp()
            }
        }
    }
}

@Composable
fun VelorunMainApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()

    // If an activity detail is selected, show detail screen with full analysis
    if (selectedActivity != null) {
        ActivityDetailScreen(
            activity = selectedActivity!!,
            viewModel = viewModel,
            onBack = { viewModel.clearActivityDetail() }
        )
    } else {
        Scaffold(
            bottomBar = {
                VelorunBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            containerColor = Color(0xFF101216)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabTransition"
                ) { tab ->
                    when (tab) {
                        AppTab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToRecord = { sport ->
                                viewModel.setSportType(sport)
                                viewModel.selectTab(AppTab.RECORD)
                            },
                            onNavigateToLeaderboard = {
                                viewModel.selectTab(AppTab.LEADERBOARD)
                            },
                            onNavigateToActivityDetail = { activity ->
                                viewModel.viewActivityDetail(activity)
                            }
                        )

                        AppTab.LEADERBOARD -> LeaderboardScreen(
                            viewModel = viewModel
                        )

                        AppTab.RECORD -> RecordScreen(
                            viewModel = viewModel
                        )

                        AppTab.ACTIVITIES -> ActivitiesScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { activity ->
                                viewModel.viewActivityDetail(activity)
                            }
                        )

                        AppTab.PROFILE -> ProfileScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(
    val tab: AppTab,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isProminent: Boolean = false
)

@Composable
fun VelorunBottomNavigation(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    val items = listOf(
        NavItem(AppTab.HOME, "Beranda", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(AppTab.LEADERBOARD, "Leaderboard", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
        NavItem(AppTab.RECORD, "Rekam", Icons.Filled.PlayArrow, Icons.Filled.PlayArrow, isProminent = true),
        NavItem(AppTab.ACTIVITIES, "Riwayat", Icons.Filled.History, Icons.Outlined.History),
        NavItem(AppTab.PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar(
        containerColor = Color(0xFF14171E),
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
    ) {
        items.forEach { item ->
            val isSelected = currentTab == item.tab

            if (item.isProminent) {
                // Center prominent Record Action
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(item.tab) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AthleticOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.selectedIcon,
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) AthleticOrange else Color(0xFF8E95A5),
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_item_${item.tab.name.lowercase()}")
                )
            } else {
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(item.tab) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = if (isSelected) AthleticOrange else Color(0xFF8E95A5)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AthleticOrange else Color(0xFF8E95A5),
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = AthleticOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_${item.tab.name.lowercase()}")
                )
            }
        }
    }
}
