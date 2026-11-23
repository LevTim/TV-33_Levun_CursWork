package com.example.curswork.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.curswork.data.local.entity.DeviceEntity
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.data.local.entity.RoomEntity
import com.example.curswork.data.repository.LightRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LightControlViewModel(
    private val repository: LightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LightUiState())
    val uiState: StateFlow<LightUiState> = _uiState.asStateFlow()

    private var hardwareJob: Job? = null
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                repository.getRooms(),
                repository.getDevices(),
                repository.getPresets()
            ) { rooms, devices, presets ->
                Triple(rooms, devices, presets)
            }.collect { (rooms, devices, presets) ->
                _uiState.update { current ->
                    val validSelectedId = if (devices.any { it.id == current.selectedDeviceId }) {
                        current.selectedDeviceId
                    } else {
                        devices.firstOrNull()?.id
                    }
                    val validTimerTarget = if (devices.any { it.id == current.timerTargetDeviceId }) {
                        current.timerTargetDeviceId
                    } else {
                        validSelectedId
                    }
                    current.copy(
                        rooms = rooms,
                        devices = devices,
                        presets = presets,
                        selectedDeviceId = validSelectedId,
                        timerTargetDeviceId = validTimerTarget
                    )
                }
            }
        }
    }

    fun selectDevice(deviceId: String) {
        _uiState.update { it.copy(selectedDeviceId = deviceId) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleDevicePower(deviceId: String, isOn: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHardware = true) }
            repository.setDevicePower(deviceId, isOn)
            _uiState.update { it.copy(isSyncingHardware = false) }
        }
    }

    fun toggleRoomPower(roomId: Int, isOn: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHardware = true) }
            repository.setRoomPower(roomId, isOn)
            _uiState.update { it.copy(isSyncingHardware = false) }
        }
    }

    fun updateBrightness(deviceId: String, newLevel: Int) {
        val clamped = newLevel.coerceIn(0, 100)
        // Оптимістичне оновлення UI для миттєвої реакції слайдера
        _uiState.update { state ->
            state.copy(
                devices = state.devices.map {
                    if (it.id == deviceId) it.copy(brightness = clamped) else it
                }
            )
        }
        hardwareJob?.cancel()
        hardwareJob = viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHardware = true) }
            repository.setDeviceBrightness(deviceId, clamped)
            _uiState.update { it.copy(isSyncingHardware = false) }
        }
    }

    fun updateColor(deviceId: String, colorRgb: Int) {
        _uiState.update { state ->
            state.copy(
                devices = state.devices.map {
                    if (it.id == deviceId) it.copy(colorRgb = colorRgb, isPowered = true) else it
                }
            )
        }
        hardwareJob?.cancel()
        hardwareJob = viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHardware = true) }
            repository.setDevicePower(deviceId, true)
            repository.setDeviceColor(deviceId, colorRgb)
            _uiState.update { it.copy(isSyncingHardware = false) }
        }
    }

    fun applyPreset(preset: PresetEntity, targetDeviceId: String? = _uiState.value.selectedDevice?.id) {
        val id = targetDeviceId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingHardware = true) }
            repository.applyPreset(id, preset)
            _uiState.update { it.copy(isSyncingHardware = false) }
        }
    }

    fun addRoom(name: String) {
        viewModelScope.launch {
            repository.addRoom(name)
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            repository.deleteRoom(room)
        }
    }

    fun addDevice(roomId: Int, name: String) {
        viewModelScope.launch {
            repository.addDevice(roomId, name)
        }
    }

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch {
            repository.deleteDevice(device)
        }
    }

    fun addPreset(title: String, brightness: Int, colorRgb: Int) {
        viewModelScope.launch {
            repository.addPreset(title, brightness, colorRgb)
        }
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    // --- Керування таймером (UC-5: плавне зниження яскравості та вимкнення) ---

    fun selectTimerTarget(deviceId: String) {
        _uiState.update { it.copy(timerTargetDeviceId = deviceId) }
    }

    fun setTimerDurationMinutes(minutes: Int) {
        if (_uiState.value.isTimerRunning) return
        val clamped = minutes.coerceIn(1, 120)
        _uiState.update {
            it.copy(
                timerDurationMinutes = clamped,
                timerRemainingSeconds = clamped * 60
            )
        }
    }

    fun startTimer() {
        val target = _uiState.value.timerTargetDevice ?: return
        if (_uiState.value.isTimerRunning) return

        timerJob?.cancel()
        val initialSeconds = _uiState.value.timerRemainingSeconds.coerceAtLeast(10)
        val initialBrightness = target.brightness.coerceAtLeast(10)

        _uiState.update {
            it.copy(
                isTimerRunning = true,
                timerRemainingSeconds = initialSeconds
            )
        }

        timerJob = viewModelScope.launch {
            var remaining = initialSeconds
            // Починаємо плавне згасання за останні 10 секунд
            val fadeWindow = minOf(10, initialSeconds)

            while (remaining > 0) {
                delay(1000L)
                remaining -= 1
                _uiState.update { it.copy(timerRemainingSeconds = remaining) }

                if (remaining in 1..fadeWindow) {
                    val steppedBrightness = ((initialBrightness * remaining) / fadeWindow).coerceAtLeast(5)
                    repository.setDeviceBrightness(target.id, steppedBrightness)
                }
            }

            // Повне вимкнення після завершення відліку
            repository.setDevicePower(target.id, false)
            _uiState.update {
                it.copy(
                    isTimerRunning = false,
                    timerRemainingSeconds = it.timerDurationMinutes * 60
                )
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                timerRemainingSeconds = it.timerDurationMinutes * 60
            )
        }
    }
}

class LightControlViewModelFactory(
    private val repository: LightRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LightControlViewModel::class.java)) {
            return LightControlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}