package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainTabsScreen
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.files.FileManagerViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.LocalEditorColors
import com.example.ui.theme.SalimAppTheme
import com.example.ui.trash.RecycleBinScreen
import com.example.ui.trash.RecycleBinViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val fileManagerViewModel: FileManagerViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val editorViewModel: EditorViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val recycleBinViewModel: RecycleBinViewModel by viewModels()

    private var pendingIntentUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()

            SalimAppTheme(settings = settingsState) {
                val colors = LocalEditorColors.current
                val navController = rememberNavController()

                // Check if an external file was opened with the app
                LaunchedEffect(pendingIntentUri) {
                    val uri = pendingIntentUri
                    if (uri != null) {
                        lifecycleScope.launch {
                            val app = applicationContext as SalimApplication
                            try {
                                val docId = app.documentRepository.importFromUri(uri)
                                navController.navigate("editor/$docId") {
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                pendingIntentUri = null
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { fadeIn(animationSpec = tween(220)) },
                        exitTransition = { fadeOut(animationSpec = tween(180)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
                        popExitTransition = { fadeOut(animationSpec = tween(180)) }
                    ) {
                        composable("home") {
                            MainTabsScreen(
                                fileManagerViewModel = fileManagerViewModel,
                                homeViewModel = homeViewModel,
                                settingsViewModel = settingsViewModel,
                                recycleBinViewModel = recycleBinViewModel,
                                onOpenDocument = { docId ->
                                    navController.navigate("editor/$docId")
                                }
                            )
                        }

                        composable(
                            route = "editor/{docId}",
                            arguments = listOf(navArgument("docId") { type = NavType.LongType }),
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(260)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(220)
                                )
                            }
                        ) { backStackEntry ->
                            val docId = backStackEntry.arguments?.getLong("docId") ?: 0L
                            EditorScreen(
                                viewModel = editorViewModel,
                                documentId = docId,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("trash") {
                            RecycleBinScreen(
                                viewModel = recycleBinViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_EDIT) {
            val uri = intent.data
            if (uri != null) {
                pendingIntentUri = uri
            }
        }
    }
}
