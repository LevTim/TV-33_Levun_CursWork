package com.example.curswork.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.curswork.data.local.entity.PresetEntity
import com.example.curswork.ui.viewmodel.LightUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    uiState: LightUiState,
    onBackClick: () -> Unit,
    onApplyPreset: (PresetEntity) -> Unit,
    onAddPreset: (String, Int, Int) -> Unit,
    onDeletePreset: (PresetEntity) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                title = { Text("Пресети", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Активна лампа: ${uiState.selectedDevice?.name ?: "не обрано"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.presets, key = { it.id }) { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onApplyPreset(preset) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.colorRgb))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Яскравість: ${preset.brightness}% • Натисніть для активації",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { onDeletePreset(preset) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Видалити пресет"
                                )
                            }
                        }
                    }
                }
            }

            // Кнопка «Новий пресет» у нижній частині екрану (відповідно до рис. 5)
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Новий пресет", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showCreateDialog) {
        CreatePresetDialog(
            initialBrightness = uiState.selectedDevice?.brightness ?: 75,
            initialColor = uiState.selectedDevice?.colorRgb ?: 0xFFFFB74D.toInt(),
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, brightness, colorRgb ->
                onAddPreset(title, brightness, colorRgb)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreatePresetDialog(
    initialBrightness: Int,
    initialColor: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var brightness by remember { mutableIntStateOf(initialBrightness) }
    var selectedColor by remember { mutableIntStateOf(initialColor) }

    val colorOptions = listOf(
        0xFFFF9E40.toInt(), // Теплий помаранчевий
        0xFFFFE082.toInt(), // М'який жовтий
        0xFFE1F5FE.toInt(), // Холодний білий
        0xFF7C4DFF.toInt(), // Фіолетовий (кіно)
        0xFF00E676.toInt(), // Смарагдовий
        0xFFFF5252.toInt()  // Червоний
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новий пресет освітлення") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Назва (напр. Вечірній релакс)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Яскравість: $brightness%")
                Slider(
                    value = brightness.toFloat(),
                    onValueChange = { brightness = it.roundToInt() },
                    valueRange = 5f..100f
                )

                Text("Колір свічення:")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEach { colorInt ->
                        val isSelected = selectedColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.Black, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = colorInt }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, brightness, selectedColor) },
                enabled = title.isNotBlank()
            ) {
                Text("Створити")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    )
}