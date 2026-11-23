package com.example.curswork.data.hal

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class SimulatedLightDriver(
    private val mockDelayMs: Long = 300L
) : SmartLightDriver {

    private val simulatedDevices = ConcurrentHashMap<String, LightState>()

    override suspend fun setPower(id: String, isOn: Boolean): Boolean {
        delay(mockDelayMs)
        val current = simulatedDevices.getOrPut(id) { LightState() }
        simulatedDevices[id] = current.copy(isPowered = isOn)
        return true
    }

    override suspend fun setBrightness(id: String, level: Int): Boolean {
        delay(mockDelayMs)
        val clampedLevel = level.coerceIn(0, 100)
        val current = simulatedDevices.getOrPut(id) { LightState() }
        simulatedDevices[id] = current.copy(brightness = clampedLevel)
        return true
    }

    override suspend fun setColor(id: String, colorRgb: Int): Boolean {
        delay(mockDelayMs)
        val current = simulatedDevices.getOrPut(id) { LightState() }
        simulatedDevices[id] = current.copy(colorRgb = colorRgb)
        return true
    }

    override suspend fun getStatus(id: String): LightState {
        delay(mockDelayMs)
        return simulatedDevices.getOrPut(id) { LightState() }
    }
}