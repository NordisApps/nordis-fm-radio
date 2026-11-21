package com.nordisapps.fmradio.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.nordisapps.fmradio.RadioViewModel
import com.nordisapps.fmradio.BuildConfig
import com.nordisapps.fmradio.R
import com.nordisapps.fmradio.diag.DiagnosticScreen
import com.nordisapps.fmradio.system.SystemServiceLocator
import com.nordisapps.fmradio.system.FM_SERVICE_NAME

sealed class Screen(val route: String, @param:StringRes val labelRes: Int) {
    object Home : Screen("home", R.string.nav_home)
    object Favorites : Screen("favorites", R.string.nav_favorites)
    object Settings : Screen("settings", R.string.nav_settings)
}

private val bottomNavItems = listOf(Screen.Home, Screen.Favorites, Screen.Settings)
private val tabOrder = listOf(Screen.Home.route, Screen.Favorites.route, Screen.Settings.route)
private val allScreens = listOf(Screen.Home, Screen.Favorites, Screen.Settings)

private fun tabIndex(route: String?): Int {
    return tabOrder.indexOf(route).let { if (it == -1) 0 else it }
}

@StringRes
private fun labelResForRoute(route: String?): Int = when (route) {
    Screen.Home.route -> R.string.title_fm_radio
    "diag" -> R.string.title_diag
    else -> allScreens.find { it.route == route }?.labelRes ?: R.string.title_fm_radio
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    favoriteStations: List<Double>,
    onStationSelected: (Double) -> Unit,
    onFavoriteToggle: (Double) -> Unit,
    settingsViewModel: RadioViewModel,
    homeContent: @Composable (SnackbarHostState) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val isHomeRoute = currentDestination?.route == Screen.Home.route
    val homeUiState = settingsViewModel.uiState

    Scaffold(
        topBar = {
            if (isHomeRoute) {
                TopAppBar(
                    title = {
                        PlaybackControlsRow(
                            isPlaying = homeUiState.isPlaying,
                            isRecording = homeUiState.isRecording,
                            isRecordingPaused = homeUiState.isRecordingPaused,
                            onPowerClick = settingsViewModel::togglePower,
                            onRecordClick = settingsViewModel::onRecordClick,
                            onRecordPauseClick = settingsViewModel::onRecordPauseClick,
                            onRecordStopClick = settingsViewModel::onRecordStopClick,
                            onRecordCancelClick = settingsViewModel::onRecordCancelClick
                        )
                    },
                    actions = {
                        ScanSpeakerRow(
                            isPlaying = homeUiState.isPlaying,
                            isScanning = homeUiState.isScanning,
                            isSpeakerOn = homeUiState.isSpeakerOn,
                            onScanClick = settingsViewModel::startScan,
                            onSpeakerClick = settingsViewModel::toggleSpeaker
                        )
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(labelResForRoute(currentDestination?.route))) }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.Home -> Icons.Filled.Home
                                    Screen.Favorites -> Icons.Filled.Star
                                    Screen.Settings -> Icons.Filled.Settings
                                },
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(screen.labelRes)) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val towardsRight =
                    tabIndex(targetState.destination.route) > tabIndex(initialState.destination.route)
                slideIntoContainer(
                    towards = if (towardsRight) AnimatedContentTransitionScope.SlideDirection.Left
                    else AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                val towardsRight =
                    tabIndex(targetState.destination.route) > tabIndex(initialState.destination.route)
                slideOutOfContainer(
                    towards = if (towardsRight) AnimatedContentTransitionScope.SlideDirection.Left
                    else AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            }
        ) {
            composable(Screen.Home.route) {
                homeContent(snackbarHostState)
            }
            composable(Screen.Favorites.route) {
                FavouritesScreen(
                    favoriteStations = favoriteStations,
                    onFavoriteToggle = onFavoriteToggle,
                    onStationSelected = onStationSelected
                )
            }
            composable(Screen.Settings.route) {
                val uiState = settingsViewModel.uiState
                SettingsScreen(
                    tuningStep = uiState.tuningStep,
                    onTuningStepChange = settingsViewModel::setTuningStep,
                    frequencyBand = uiState.frequencyBand,
                    onFrequencyBandChange = settingsViewModel::setFrequencyBand,
                    isRdsEnabled = uiState.isRdsEnabled,
                    onRdsToggle = settingsViewModel::setRdsEnabled,
                    isMonoMode = uiState.isMonoMode,
                    onMonoModeToggle = settingsViewModel::setMonoMode,
                    isSoftMuteEnabled = uiState.isSoftMuteEnabled,
                    onSoftMuteToggle = settingsViewModel::setSoftMuteEnabled,
                    onOpenDiagnostics = if (BuildConfig.IS_DIAG) {
                        { navController.navigate("diag") }
                    } else null
                )
            }
            if (BuildConfig.IS_DIAG) {
                composable("diag") {
                    DiagnosticScreen(binderProvider = {
                        SystemServiceLocator.getServiceOrNull(FM_SERVICE_NAME)
                    })
                }
            }
        }
    }
}

@Composable
fun FavouritesScreen(
    favoriteStations: List<Double>,
    onStationSelected: (Double) -> Unit,
    onFavoriteToggle: (Double) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (favoriteStations.isEmpty()) {
            Text(stringResource(R.string.favorites_empty))
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(favoriteStations) { freq ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onStationSelected(freq) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.freq_mhz, freq.toString()),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                        IconButton(onClick = { onFavoriteToggle(freq) }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(R.string.favorites_remove),
                                tint = Color(0xFFFFC107)
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}