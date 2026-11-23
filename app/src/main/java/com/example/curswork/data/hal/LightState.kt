package com.example.curswork.data.hal

data class LightState(
    val isPowered: Boolean = false,
    val brightness: Int = 100,
    val colorRgb: Int = -1
)