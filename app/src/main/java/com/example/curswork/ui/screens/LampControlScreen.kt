package com.example.curswork.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.ui.components.ColorWheelPicker
import com.example.curswork.ui.viewmodel.LightUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampControlScreen(
    uiState: LightUiState,
    onBackClick: () -> Unit,
    onSelectDevice: (String) -> Unit,
    onTogglePower: (String, Boolean) -> Unit,
    onBrightnessChange: (String, Int) -> Unit,
    onColorChange: (String, Int) -> Unit,
    onApplyPreset: (PresetEntity) -> Unit
) {
    val activeDevice = uiState.selectedDevice

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                title = {
                    Column {
                        Text("Лампа", fontWeight = FontWeight.Bold)
                        if (activeDevice != null) {
                            Text(
                                text = activeDevice.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (activeDevice != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = if (activeDevice.isPowered) "Вкл" else "Викл",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Switch(
                                checked = activeDevice.isPowered,
                                onCheckedChange = { onTogglePower(activeDevice.id, it) }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (activeDevice == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Немає доданих ламп. Додайте пристрій на вкладці «Кімнати».")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Швидкий вибір іншої лампи
            if (uiState.devices.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.devices, key = { it.id }) { device ->
                        FilterChip(
                            selected = device.id == activeDevice.id,
                            onClick = { onSelectDevice(device.id) },
                            label = { Text(device.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Центральна кругова RGB/HSV палітра кольорів (рис. 5)
            ColorWheelPicker(
                selectedColorInt = activeDevice.colorRgb,
                isPowered = activeDevice.isPowered,
                wheelSize = 230.dp,
                onColorSelected = { newColorInt ->
                    onColorChange(activeDevice.id, newColorInt)
                }
            )

            // Швидкий вибір колірної температури (2700K / 4000K / 6500K)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { onColorChange(activeDevice.id, 0xFFFFD180.toInt()) },
                    label = { Text("2700K Тепле") }
                )
                FilterChip(
                    selected = false,
                    onClick = { onColorChange(activeDevice.id, 0xFFFFF8E1.toInt()) },
                    label = { Text("4000K Денне") }
                )
                FilterChip(
                    selected = false,
                    onClick = { onColorChange(activeDevice.id, 0xFFE1F5FE.toInt()) },
                    label = { Text("6500K Холодне") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Слайдер яскравості 0% - 100% (рис. 5)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Яскравість",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${activeDevice.brightness}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = activeDevice.brightness.toFloat(),
                    onValueChange = { onBrightnessChange(activeDevice.id, it.roundToInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Нижній ряд кнопок швидкого вибору пресетів (рис. 5)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Швидкі пресети",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.presets, key = { it.id }) { preset ->
                        OutlinedButton(onClick = { onApplyPreset(preset) }) {
                            Text(preset.title)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}