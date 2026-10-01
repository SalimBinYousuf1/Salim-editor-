package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.files.FileManagerScreen
import com.example.ui.files.FileManagerViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.LocalEditorColors
import com.example.ui.trash.RecycleBinScreen
import com.example.ui.trash.RecycleBinViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    FILES("Files", Icons.Filled.Folder, Icons.Outlined.Folder),
    NOTES("Notes", Icons.Filled.Description, Icons.Outlined.Description),
    TRASH("Trash", Icons.Filled.RestoreFromTrash, Icons.Outlined.Delete),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainTabsScreen(
    fileManagerViewModel: FileManagerViewModel,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    recycleBinViewModel: RecycleBinViewModel,
    onOpenDocument: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.FILES) }
    val colors = LocalEditorColors.current

    // If on a secondary tab and back is pressed, return to Files tab first
    BackHandler(enabled = selectedTab != MainTab.FILES) {
        selectedTab = MainTab.FILES
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = colors.surface,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = 0.dp
                ) {
                    MainTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.accent,
                                selectedTextColor = colors.accent,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.accent.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MainTabsTransition"
            ) { tab ->
                when (tab) {
                    MainTab.FILES -> {
                        FileManagerScreen(
                            viewModel = fileManagerViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MainTab.NOTES -> {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onOpenDocument = onOpenDocument,
                            onNavigateToSettings = { selectedTab = MainTab.SETTINGS },
                            onNavigateToTrash = { selectedTab = MainTab.TRASH }
                        )
                    }
                    MainTab.TRASH -> {
                        RecycleBinScreen(
                            viewModel = recycleBinViewModel,
                            onBack = { selectedTab = MainTab.FILES }
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { selectedTab = MainTab.FILES }
                        )
                    }
                }
            }
        }
    }
}
