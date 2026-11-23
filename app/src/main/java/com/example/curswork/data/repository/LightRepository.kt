package com.example.curswork.data.repository

import com.example.curswork.data.hal.SmartLightDriver
import com.example.curswork.data.local.dao.DeviceDao
import com.example.curswork.data.local.dao.PresetDao
import com.example.curswork.data.local.dao.RoomDao
import com.example.curswork.data.local.entity.DeviceEntity
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class LightRepository(
    private val driver: SmartLightDriver,
    private val deviceDao: DeviceDao,
    private val roomDao: RoomDao,
    private val presetDao: PresetDao
) {
    fun getDevices(): Flow<List<DeviceEntity>> = deviceDao.getAllDevices()

    fun getRooms(): Flow<List<RoomEntity>> = roomDao.getAllRooms()

    fun getPresets(): Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun setDevicePower(deviceId: String, isOn: Boolean) {
        val success = driver.setPower(deviceId, isOn)
        if (success) {
            deviceDao.updateDevicePower(deviceId, isOn)
        }
    }

    suspend fun setRoomPower(roomId: Int, isOn: Boolean) {
        val devices = deviceDao.getDevicesByRoomId(roomId)
        devices.forEach { device ->
            driver.setPower(device.id, isOn)
        }
        deviceDao.updateRoomDevicesPower(roomId, isOn)
    }

    suspend fun setDeviceBrightness(deviceId: String, newLevel: Int) {
        val clamped = newLevel.coerceIn(0, 100)
        val success = driver.setBrightness(deviceId, clamped)
        if (success) {
            deviceDao.updateDeviceBrightness(deviceId, clamped)
        }
    }

    suspend fun setDeviceColor(deviceId: String, colorRgb: Int) {
        val success = driver.setColor(deviceId, colorRgb)
        if (success) {
            deviceDao.updateDeviceColor(deviceId, colorRgb)
        }
    }

    suspend fun applyPreset(deviceId: String, preset: PresetEntity) {
        val powerOk = driver.setPower(deviceId, true)
        val brightnessOk = driver.setBrightness(deviceId, preset.brightness)
        val colorOk = driver.setColor(deviceId, preset.colorRgb)
        if (powerOk && brightnessOk && colorOk) {
            deviceDao.updateDevicePower(deviceId, true)
            deviceDao.updateDeviceBrightness(deviceId, preset.brightness)
            deviceDao.updateDeviceColor(deviceId, preset.colorRgb)
        }
    }

    suspend fun addRoom(name: String) {
        if (name.isNotBlank()) {
            roomDao.insertRoom(RoomEntity(name = name.trim()))
        }
    }

    suspend fun deleteRoom(room: RoomEntity) {
        roomDao.deleteRoom(room)
    }

    suspend fun addDevice(roomId: Int, name: String) {
        if (name.isNotBlank()) {
            val newDevice = DeviceEntity(
                id = "bulb-${UUID.randomUUID().toString().take(8)}",
                roomId = roomId,
                name = name.trim(),
                isPowered = true,
                brightness = 80,
                colorRgb = -1,
                isOnline = true
            )
            deviceDao.insertDevice(newDevice)
        }
    }

    suspend fun deleteDevice(device: DeviceEntity) {
        deviceDao.deleteDevice(device)
    }

    suspend fun addPreset(title: String, brightness: Int, colorRgb: Int) {
        if (title.isNotBlank()) {
            presetDao.insertPreset(
                PresetEntity(
                    title = title.trim(),
                    brightness = brightness.coerceIn(0, 100),
                    colorRgb = colorRgb
                )
            )
        }
    }

    suspend fun deletePreset(preset: PresetEntity) {
        presetDao.deletePreset(preset)
    }
}