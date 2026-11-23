package com.example.curswork.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.curswork.data.local.entity.DeviceEntity
import com.example.curswork.data.local.entity.RoomEntity
import com.example.curswork.ui.viewmodel.LightUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    uiState: LightUiState,
    onSearchQueryChanged: (String) -> Unit,
    onToggleRoomPower: (Int, Boolean) -> Unit,
    onToggleDevicePower: (String, Boolean) -> Unit,
    onDeviceClick: (String) -> Unit,
    onAddRoom: (String) -> Unit,
    onAddDevice: (Int, String) -> Unit,
    onDeleteDevice: (DeviceEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredRooms = remember(uiState.rooms, uiState.devices, uiState.searchQuery) {
        val q = uiState.searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            uiState.rooms
        } else {
            uiState.rooms.filter { room ->
                room.name.lowercase().contains(q) ||
                        uiState.devices.any { it.roomId == room.id && it.name.lowercase().contains(q) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кімнати", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Додати кімнату або лампу")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Поле пошуку (відповідно до рис. 5)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Пошук") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
                    val roomDevices = uiState.devices.filter { it.roomId == room.id }
                    val isAnyLightOn = roomDevices.any { it.isPowered }

                    RoomCard(
                        room = room,
                        devices = roomDevices,
                        isRoomPowered = isAnyLightOn,
                        onToggleRoomPower = { onToggleRoomPower(room.id, it) },
                        onToggleDevicePower = onToggleDevicePower,
                        onDeviceClick = onDeviceClick,
                        onDeleteDevice = onDeleteDevice
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddRoomOrDeviceDialog(
            rooms = uiState.rooms,
            onDismiss = { showAddDialog = false },
            onConfirmRoom = { roomName ->
                onAddRoom(roomName)
                showAddDialog = false
            },
            onConfirmDevice = { roomId, deviceName ->
                onAddDevice(roomId, deviceName)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun RoomCard(
    room: RoomEntity,
    devices: List<DeviceEntity>,
    isRoomPowered: Boolean,
    onToggleRoomPower: (Boolean) -> Unit,
    onToggleDevicePower: (String, Boolean) -> Unit,
    onDeviceClick: (String) -> Unit,
    onDeleteDevice: (DeviceEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Пристроїв: ${devices.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRoomPowered,
                    onCheckedChange = onToggleRoomPower,
                    enabled = devices.isNotEmpty()
                )
            }

            if (devices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                devices.forEach { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onDeviceClick(device.id) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (device.isPowered) Color(device.colorRgb)
                                    else Color.Gray.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (device.isPowered) Color.Black else Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (device.isPowered) "Увімкнено • ${device.brightness}%" else "Вимкнено",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = device.isPowered,
                            onCheckedChange = { onToggleDevicePower(device.id, it) }
                        )

                        IconButton(onClick = { onDeleteDevice(device) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Видалити лампу",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRoomOrDeviceDialog(
    rooms: List<RoomEntity>,
    onDismiss: () -> Unit,
    onConfirmRoom: (String) -> Unit,
    onConfirmDevice: (Int, String) -> Unit
) {
    var addModeIsDevice by remember { mutableStateOf(rooms.isNotEmpty()) }
    var nameInput by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf(rooms.firstOrNull()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Додати до системи") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !addModeIsDevice,
                        onClick = { addModeIsDevice = false },
                        label = { Text("Кімнату") }
                    )
                    FilterChip(
                        selected = addModeIsDevice,
                        onClick = { addModeIsDevice = true },
                        enabled = rooms.isNotEmpty(),
                        label = { Text("Розумну лампу") }
                    )
                }

                if (addModeIsDevice && rooms.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRoom?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Кімната") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            rooms.forEach { room ->
                                DropdownMenuItem(
                                    text = { Text(room.name) },
                                    onClick = {
                                        selectedRoom = room
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = {
                        Text(if (addModeIsDevice) "Назва лампи (напр. Люстра)" else "Назва кімнати (напр. Кухня)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (addModeIsDevice) {
                        selectedRoom?.let { onConfirmDevice(it.id, nameInput) }
                    } else {
                        onConfirmRoom(nameInput)
                    }
                },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Зберегти")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    )
}