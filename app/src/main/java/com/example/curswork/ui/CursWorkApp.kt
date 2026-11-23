package com.example.curswork.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.curswork.ui.components.AppScreen
import com.example.curswork.ui.components.BottomNavBar
import com.example.curswork.ui.screens.LampControlScreen
import com.example.curswork.ui.screens.PresetsScreen
import com.example.curswork.ui.screens.RoomsScreen
import com.example.curswork.ui.screens.TimerScreen
import com.example.curswork.ui.viewmodel.LightControlViewModel

@Composable
fun CursWorkApp(viewModel: LightControlViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTab(route) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.ROOMS.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.ROOMS.route) {
                RoomsScreen(
                    uiState = uiState,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onToggleRoomPower = viewModel::toggleRoomPower,
                    onToggleDevicePower = viewModel::toggleDevicePower,
                    onDeviceClick = { deviceId ->
                        viewModel.selectDevice(deviceId)
                        navigateToTab(AppScreen.LAMP.route)
                    },
                    onAddRoom = viewModel::addRoom,
                    onAddDevice = viewModel::addDevice,
                    onDeleteDevice = viewModel::deleteDevice
                )
            }

            composable(AppScreen.LAMP.route) {
                LampControlScreen(
                    uiState = uiState,
                    onBackClick = { navigateToTab(AppScreen.ROOMS.route) },
                    onSelectDevice = viewModel::selectDevice,
                    onTogglePower = viewModel::toggleDevicePower,
                    onBrightnessChange = viewModel::updateBrightness,
                    onColorChange = viewModel::updateColor,
                    onApplyPreset = { preset -> viewModel.applyPreset(preset) }
                )
            }

            composable(AppScreen.PRESETS.route) {
                PresetsScreen(
                    uiState = uiState,
                    onBackClick = { navigateToTab(AppScreen.ROOMS.route) },
                    onApplyPreset = { preset ->
                        viewModel.applyPreset(preset)
                        navigateToTab(AppScreen.LAMP.route)
                    },
                    onAddPreset = viewModel::addPreset,
                    onDeletePreset = viewModel::deletePreset
                )
            }

            composable(AppScreen.TIMER.route) {
                TimerScreen(
                    uiState = uiState,
                    onBackClick = { navigateToTab(AppScreen.ROOMS.route) },
                    onSelectTargetDevice = viewModel::selectTimerTarget,
                    onDurationChanged = viewModel::setTimerDurationMinutes,
                    onStartTimer = viewModel::startTimer,
                    onCancelTimer = viewModel::cancelTimer
                )
            }
        }
    }
}