package com.example.curswork.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["room_id"])]
)
data class DeviceEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "room_id")
    val roomId: Int,
    val name: String,
    @ColumnInfo(name = "is_powered")
    val isPowered: Boolean = false,
    val brightness: Int = 100,
    @ColumnInfo(name = "color_rgb")
    val colorRgb: Int = -1, // 0xFFFFFFFF (білий колір за замовчуванням)
    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = true
)