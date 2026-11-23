package com.example.curswork.ui.viewmodel

import com.example.curswork.data.local.entity.DeviceEntity
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.data.local.entity.RoomEntity

data class LightUiState(
    val rooms: List<RoomEntity> = emptyList(),
    val devices: List<DeviceEntity> = emptyList(),
    val presets: List<PresetEntity> = emptyList(),
    val selectedDeviceId: String? = null,
    val searchQuery: String = "",
    // Стан таймера
    val timerDurationMinutes: Int = 15,
    val timerRemainingSeconds: Int = 15 * 60,
    val isTimerRunning: Boolean = false,
    val timerTargetDeviceId: String? = null,
    // Індикатор синхронізації HAL
    val isSyncingHardware: Boolean = false
) {
    val selectedDevice: DeviceEntity?
        get() = devices.find { it.id == selectedDeviceId } ?: devices.firstOrNull()

    val timerTargetDevice: DeviceEntity?
        get() = devices.find { it.id == timerTargetDeviceId } ?: selectedDevice
}