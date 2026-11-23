package com.example.curswork.data.hal

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedLightDriverTest {

    @Test
    fun setPower_updatesDevicePowerState() = runBlocking {
        val driver = SimulatedLightDriver(mockDelayMs = 50L)

        val result = driver.setPower("bulb-01", true)
        val status = driver.getStatus("bulb-01")

        assertTrue(result)
        assertEquals(true, status.isPowered)
    }

    @Test
    fun setBrightness_clampsValuesToValidRange() = runBlocking {
        val driver = SimulatedLightDriver(mockDelayMs = 10L)

        driver.setBrightness("bulb-01", 150)
        assertEquals(100, driver.getStatus("bulb-01").brightness)

        driver.setBrightness("bulb-01", -25)
        assertEquals(0, driver.getStatus("bulb-01").brightness)

        driver.setBrightness("bulb-01", 65)
        assertEquals(65, driver.getStatus("bulb-01").brightness)
    }

    @Test
    fun setColor_updatesDeviceColorRgb() = runBlocking {
        val driver = SimulatedLightDriver(mockDelayMs = 10L)
        val targetColor = 0xFFFF9E40.toInt()

        val result = driver.setColor("bulb-01", targetColor)
        val status = driver.getStatus("bulb-01")

        assertTrue(result)
        assertEquals(targetColor, status.colorRgb)
    }

    @Test
    fun simulatedDriver_emulatesNetworkDelay() = runBlocking {
        val expectedDelay = 300L
        val driver = SimulatedLightDriver(mockDelayMs = expectedDelay)

        val startTime = System.currentTimeMillis()
        driver.setPower("bulb-01", true)
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue("Очікувалась затримка не менше 280 мс, фактично: $elapsed мс", elapsed >= 280L)
    }
}