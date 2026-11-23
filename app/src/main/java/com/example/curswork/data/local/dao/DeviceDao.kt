package com.example.curswork.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.curswork.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY room_id ASC, name ASC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE room_id = :roomId")
    suspend fun getDevicesByRoomId(roomId: Int): List<DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceEntity)

    @Query("UPDATE devices SET is_powered = :isPowered WHERE id = :deviceId")
    suspend fun updateDevicePower(deviceId: String, isPowered: Boolean)

    @Query("UPDATE devices SET brightness = :newLevel WHERE id = :deviceId")
    suspend fun updateDeviceBrightness(deviceId: String, newLevel: Int)

    @Query("UPDATE devices SET color_rgb = :colorRgb WHERE id = :deviceId")
    suspend fun updateDeviceColor(deviceId: String, colorRgb: Int)

    @Query("UPDATE devices SET is_powered = :isPowered WHERE room_id = :roomId")
    suspend fun updateRoomDevicesPower(roomId: Int, isPowered: Boolean)
}