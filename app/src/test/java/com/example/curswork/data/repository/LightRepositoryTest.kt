package com.example.curswork.data.repository

import com.example.curswork.data.hal.SimulatedLightDriver
import com.example.curswork.data.local.dao.DeviceDao
import com.example.curswork.data.local.dao.PresetDao
import com.example.curswork.data.local.dao.RoomDao
import com.example.curswork.data.local.entity.DeviceEntity
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LightRepositoryTest {

    @Test
    fun applyPreset_turnsOnDeviceAndUpdatesHalAndDao() = runBlocking {
        val driver = SimulatedLightDriver(mockDelayMs = 10L)
        val fakeDeviceDao = FakeDeviceDao(
            initialDevices = listOf(
                DeviceEntity(
                    id = "bulb-1",
                    roomId = 1,
                    name = "Основне світло",
                    isPowered = false,
                    brightness = 100,
                    colorRgb = -1
                )
            )
        )
        val repository = LightRepository(driver, fakeDeviceDao, FakeRoomDao(), FakePresetDao())
        val preset = PresetEntity(
            id = 1,
            title = "Нічне читання",
            brightness = 25,
            colorRgb = 0xFFFF9E40.toInt()
        )

        repository.applyPreset("bulb-1", preset)

        val updatedDevice = fakeDeviceDao.getAllDevices().first().first()
        val halStatus = driver.getStatus("bulb-1")

        assertTrue(updatedDevice.isPowered)
        assertEquals(25, updatedDevice.brightness)
        assertEquals(0xFFFF9E40.toInt(), updatedDevice.colorRgb)
        assertTrue(halStatus.isPowered)
        assertEquals(25, halStatus.brightness)
    }

    @Test
    fun setRoomPower_togglesAllDevicesInSpecifiedRoom() = runBlocking {
        val driver = SimulatedLightDriver(mockDelayMs = 10L)
        val fakeDeviceDao = FakeDeviceDao(
            initialDevices = listOf(
                DeviceEntity(id = "bulb-1", roomId = 1, name = "Люстра", isPowered = false),
                DeviceEntity(id = "bulb-2", roomId = 1, name = "Торшер", isPowered = false),
                DeviceEntity(id = "bulb-3", roomId = 2, name = "Нічник", isPowered = false)
            )
        )
        val repository = LightRepository(driver, fakeDeviceDao, FakeRoomDao(), FakePresetDao())

        repository.setRoomPower(roomId = 1, isOn = true)

        val allDevices = fakeDeviceDao.getAllDevices().first()
        assertEquals(true, allDevices.first { it.id == "bulb-1" }.isPowered)
        assertEquals(true, allDevices.first { it.id == "bulb-2" }.isPowered)
        assertEquals(false, allDevices.first { it.id == "bulb-3" }.isPowered)
    }

    // Легковагові тестові заглушки DAO для ізольованої перевірки репозиторію без SQLite
    private class FakeDeviceDao(initialDevices: List<DeviceEntity>) : DeviceDao {
        private val state = MutableStateFlow(initialDevices)

        override fun getAllDevices(): Flow<List<DeviceEntity>> = state

        override suspend fun getDevicesByRoomId(roomId: Int): List<DeviceEntity> =
            state.value.filter { it.roomId == roomId }

        override suspend fun insertDevice(device: DeviceEntity) {
            state.value = state.value.filterNot { it.id == device.id } + device
        }

        override suspend fun deleteDevice(device: DeviceEntity) {
            state.value = state.value.filterNot { it.id == device.id }
        }

        override suspend fun updateDevicePower(deviceId: String, isPowered: Boolean) {
            state.value = state.value.map {
                if (it.id == deviceId) it.copy(isPowered = isPowered) else it
            }
        }

        override suspend fun updateDeviceBrightness(deviceId: String, newLevel: Int) {
            state.value = state.value.map {
                if (it.id == deviceId) it.copy(brightness = newLevel) else it
            }
        }

        override suspend fun updateDeviceColor(deviceId: String, colorRgb: Int) {
            state.value = state.value.map {
                if (it.id == deviceId) it.copy(colorRgb = colorRgb) else it
            }
        }

        override suspend fun updateRoomDevicesPower(roomId: Int, isPowered: Boolean) {
            state.value = state.value.map {
                if (it.roomId == roomId) it.copy(isPowered = isPowered) else it
            }
        }
    }

    private class FakeRoomDao : RoomDao {
        private val state = MutableStateFlow<List<RoomEntity>>(emptyList())
        override fun getAllRooms(): Flow<List<RoomEntity>> = state
        override suspend fun insertRoom(room: RoomEntity): Long = 1L
        override suspend fun deleteRoom(room: RoomEntity) {}
    }

    private class FakePresetDao : PresetDao {
        private val state = MutableStateFlow<List<PresetEntity>>(emptyList())
        override fun getAllPresets(): Flow<List<PresetEntity>> = state
        override suspend fun insertPreset(preset: PresetEntity) {}
        override suspend fun deletePreset(preset: PresetEntity) {}
    }
}