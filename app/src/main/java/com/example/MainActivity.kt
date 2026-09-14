package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EmergencyAccessDialog
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsAndWebScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TimeSaverTheme
import com.example.ui.viewmodel.FocusViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeSaverTheme {
                TimeSaverApp()
            }
        }
    }
}

sealed class NavigationTab(val index: Int, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val testTag: String) {
    object Home : NavigationTab(0, "Home", Icons.Default.Home, "nav_home")
    object Focus : NavigationTab(1, "Focus", Icons.Default.Lock, "nav_focus")
    object Apps : NavigationTab(2, "App Control", Icons.Default.Shield, "nav_apps")
    object Stats : NavigationTab(3, "Stats", Icons.Default.BarChart, "nav_stats")
    object Goals : NavigationTab(4, "Goals", Icons.Default.Flag, "nav_goals")
    object Settings : NavigationTab(5, "Web & Sync", Icons.Default.Language, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSaverApp(viewModel: FocusViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()

    if (showEmergencyDialog) {
        EmergencyAccessDialog(
            onDismiss = { showEmergencyDialog = false },
            onLaunchAllowedTool = { tool ->
                showEmergencyDialog = false
                Toast.makeText(context, "$tool opened in approved study mode", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald500),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TimeSaver",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Less Scrolling. More Doing.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showEmergencyDialog = true },
                        modifier = Modifier.testTag("emergency_button")
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = "Emergency Safety",
                            tint = CrimsonRed
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.syncCloudData()
                            Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { currentTab = 5 },
                        modifier = Modifier.testTag("settings_top_button")
                    ) {
                        Icon(
                            if (currentTab == 5) Icons.Default.Language else Icons.Default.Settings,
                            contentDescription = "Website & Settings",
                            tint = if (currentTab == 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val tabs = listOf(
                    NavigationTab.Home,
                    NavigationTab.Focus,
                    NavigationTab.Apps,
                    NavigationTab.Stats,
                    NavigationTab.Goals
                )

                tabs.forEach { tab ->
                    val isSelected = currentTab == tab.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab.index },
                        icon = {
                            if (tab == NavigationTab.Focus && activeSession.isActive) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Amber500) {
                                            Text("🔥", fontSize = 10.sp)
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToFocus = { currentTab = 1 },
                    onNavigateToGoals = { currentTab = 4 },
                    onNavigateToStats = { currentTab = 3 }
                )
                1 -> FocusScreen(
                    viewModel = viewModel,
                    onNavigateToApps = { currentTab = 2 }
                )
                2 -> AppsScreen(
                    viewModel = viewModel
                )
                3 -> StatsScreen(
                    viewModel = viewModel
                )
                4 -> GoalsScreen(
                    viewModel = viewModel
                )
                5 -> SettingsAndWebScreen(
                    viewModel = viewModel,
                    onNavigateToFocus = { currentTab = 1 }
                )
            }
        }
    }
}
