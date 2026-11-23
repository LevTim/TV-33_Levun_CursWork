package com.example.curswork.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.curswork.ui.viewmodel.LightUiState
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    uiState: LightUiState,
    onBackClick: () -> Unit,
    onSelectTargetDevice: (String) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onCancelTimer: () -> Unit
) {
    val totalSeconds = (uiState.timerDurationMinutes * 60).coerceAtLeast(1)
    val progress = (uiState.timerRemainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val minutesPart = uiState.timerRemainingSeconds / 60
    val secondsPart = uiState.timerRemainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutesPart, secondsPart)

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                title = { Text("Таймер", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Вибір цільової лампи для таймера
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Пристрій для автовимкнення:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.devices, key = { it.id }) { device ->
                        FilterChip(
                            selected = device.id == uiState.timerTargetDevice?.id,
                            onClick = { onSelectTargetDevice(device.id) },
                            enabled = !uiState.isTimerRunning,
                            label = { Text(device.name) }
                        )
                    }
                }
            }

            // Центральний циферблат зворотного відліку 15:00 (рис. 5)
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isTimerRunning) "Відлік триває..." else "Готово до запуску",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Слайдер вибору часу та швидкі інтервали (15, 30, 60 хв)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    listOf(1, 15, 30, 60).forEach { mins ->
                        FilterChip(
                            selected = uiState.timerDurationMinutes == mins,
                            onClick = { onDurationChanged(mins) },
                            enabled = !uiState.isTimerRunning,
                            label = { Text("$mins хв") }
                        )
                    }
                }

                Slider(
                    value = uiState.timerDurationMinutes.toFloat(),
                    onValueChange = { onDurationChanged(it.roundToInt()) },
                    valueRange = 1f..120f,
                    enabled = !uiState.isTimerRunning,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Кнопка «Запустити» / «Скасувати» (рис. 5)
            Button(
                onClick = {
                    if (uiState.isTimerRunning) onCancelTimer() else onStartTimer()
                },
                enabled = uiState.timerTargetDevice != null,
                colors = if (uiState.isTimerRunning) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (uiState.isTimerRunning) "Скасувати" else "Запустити",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}