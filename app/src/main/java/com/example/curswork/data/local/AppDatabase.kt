package com.example.curswork.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.curswork.data.local.dao.DeviceDao
import com.example.curswork.data.local.dao.PresetDao
import com.example.curswork.data.local.dao.RoomDao
import com.example.curswork.data.local.entity.DeviceEntity
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.data.local.entity.RoomEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RoomEntity::class, DeviceEntity::class, PresetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun roomDao(): RoomDao
    abstract fun deviceDao(): DeviceDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_lighting_database"
                )
                    .addCallback(DatabaseSeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(
                        database.roomDao(),
                        database.deviceDao(),
                        database.presetDao()
                    )
                }
            }
        }

        private suspend fun populateInitialData(
            roomDao: RoomDao,
            deviceDao: DeviceDao,
            presetDao: PresetDao
        ) {
            // Початкові кімнати відповідно до макету (Вітальня, Спальня, Офіс)
            val livingRoomId = roomDao.insertRoom(RoomEntity(name = "Вітальня", iconRes = "weekend")).toInt()
            val bedroomId = roomDao.insertRoom(RoomEntity(name = "Спальня", iconRes = "bed")).toInt()
            val officeId = roomDao.insertRoom(RoomEntity(name = "Офіс", iconRes = "work")).toInt()

            // Початкові розумні лампи
            deviceDao.insertDevice(
                DeviceEntity(
                    id = "bulb-living-01",
                    roomId = livingRoomId,
                    name = "Основне світло",
                    isPowered = true,
                    brightness = 85,
                    colorRgb = 0xFFFFE0B2.toInt(),
                    isOnline = true
                )
            )
            deviceDao.insertDevice(
                DeviceEntity(
                    id = "bulb-living-02",
                    roomId = livingRoomId,
                    name = "Торшер",
                    isPowered = false,
                    brightness = 60,
                    colorRgb = 0xFFFF8A65.toInt(),
                    isOnline = true
                )
            )
            deviceDao.insertDevice(
                DeviceEntity(
                    id = "bulb-bedroom-01",
                    roomId = bedroomId,
                    name = "Нічник біля ліжка",
                    isPowered = true,
                    brightness = 30,
                    colorRgb = 0xFFFFAB40.toInt(),
                    isOnline = true
                )
            )
            deviceDao.insertDevice(
                DeviceEntity(
                    id = "bulb-office-01",
                    roomId = officeId,
                    name = "Настільна лампа",
                    isPowered = true,
                    brightness = 100,
                    colorRgb = 0xFFE3F2FD.toInt(),
                    isOnline = true
                )
            )

            // Початкові пресети відповідно до тексту курсової
            presetDao.insertPreset(
                PresetEntity(
                    title = "Нічне читання",
                    brightness = 25,
                    colorRgb = 0xFFFF9E40.toInt()
                )
            )
            presetDao.insertPreset(
                PresetEntity(
                    title = "Режим кіно",
                    brightness = 15,
                    colorRgb = 0xFF7C4DFF.toInt()
                )
            )
            presetDao.insertPreset(
                PresetEntity(
                    title = "Робочий день",
                    brightness = 100,
                    colorRgb = 0xFFE1F5FE.toInt()
                )
            )
        }
    }
}