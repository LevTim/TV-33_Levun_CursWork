package com.example.curswork.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val brightness: Int,
    @ColumnInfo(name = "color_rgb")
    val colorRgb: Int
)