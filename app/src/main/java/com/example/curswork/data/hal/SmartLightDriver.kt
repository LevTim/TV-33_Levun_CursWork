package com.example.curswork.data.hal

interface SmartLightDriver {
    suspend fun setPower(id: String, isOn: Boolean): Boolean
    suspend fun setBrightness(id: String, level: Int): Boolean
    suspend fun setColor(id: String, colorRgb: Int): Boolean
    suspend fun getStatus(id: String): LightState
}