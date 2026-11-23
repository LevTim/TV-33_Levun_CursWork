package com.example.curswork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.curswork.data.hal.SimulatedLightDriver
import com.example.curswork.data.local.AppDatabase
import com.example.curswork.data.repository.LightRepository
import com.example.curswork.ui.CursWorkApp
import com.example.curswork.ui.theme.CursWorkTheme
import com.example.curswork.ui.viewmodel.LightControlViewModel
import com.example.curswork.ui.viewmodel.LightControlViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ініціалізація Room DB, HAL-симулятора та репозиторію
        val database = AppDatabase.getDatabase(applicationContext)
        val simulatedDriver = SimulatedLightDriver(mockDelayMs = 300L)
        val repository = LightRepository(
            driver = simulatedDriver,
            deviceDao = database.deviceDao(),
            roomDao = database.roomDao(),
            presetDao = database.presetDao()
        )

        setContent {
            CursWorkTheme {
                val lightViewModel: LightControlViewModel = viewModel(
                    factory = LightControlViewModelFactory(repository)
                )
                CursWorkApp(viewModel = lightViewModel)
            }
        }
    }
}