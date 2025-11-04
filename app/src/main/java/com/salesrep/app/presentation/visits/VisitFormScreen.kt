package com.salesrep.app.presentation.visits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salesrep.app.presentation.components.*
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitFormScreen(
    visitId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: VisitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var customerId by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var visitTime by remember { mutableStateOf("09:00") }
    var visitType by remember { mutableStateOf("Sales Call") }
    var notes by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("scheduled") }

    val isEditMode = visitId != null

    LaunchedEffect(visitId) {
        visitId?.let {
            viewModel.loadVisitById(it)
        }
    }

    LaunchedEffect(uiState.selectedVisit) {
        if (isEditMode && uiState.selectedVisit != null) {
            val visit = uiState.selectedVisit!!
            customerId = visit.customerId.toString()
            visitDate = visit.visitDate
            visitTime = visit.visitTime
            visitType = visit.visitType
            notes = visit.notes ?: ""
            latitude = visit.locationLat?.toString() ?: ""
            longitude = visit.locationLng?.toString() ?: ""
            status = visit.status
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Visit" else "Schedule Visit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = customerId,
                onValueChange = { customerId = it },
                label = { Text("Customer ID *") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = visitDate,
                onValueChange = { visitDate = it },
                label = { Text("Visit Date (YYYY-MM-DD) *") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = visitTime,
                onValueChange = { visitTime = it },
                label = { Text("Visit Time (HH:MM) *") },
                leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Visit Type Dropdown
            var expandedVisitType by remember { mutableStateOf(false) }
            val visitTypes = listOf("Sales Call", "Follow-up", "Delivery", "Service", "Collection", "Product Demo")

            ExposedDropdownMenuBox(
                expanded = expandedVisitType,
                onExpandedChange = { expandedVisitType = it }
            ) {
                OutlinedTextField(
                    value = visitType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Visit Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVisitType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedVisitType,
                    onDismissRequest = { expandedVisitType = false }
                ) {
                    visitTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                visitType = type
                                expandedVisitType = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                minLines = 3
            )

            Text(
                text = "Location (Optional)",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateVisit(
                            id = visitId!!,
                            customerId = customerId.toIntOrNull(),
                            visitDate = visitDate,
                            visitTime = visitTime,
                            visitType = visitType,
                            notes = notes.ifEmpty { null },
                            locationLat = latitude.toDoubleOrNull(),
                            locationLng = longitude.toDoubleOrNull(),
                            status = status
                        )
                    } else {
                        viewModel.createVisit(
                            customerId = customerId.toInt(),
                            visitDate = visitDate,
                            visitTime = visitTime,
                            visitType = visitType,
                            notes = notes.ifEmpty { null },
                            locationLat = latitude.toDoubleOrNull(),
                            locationLng = longitude.toDoubleOrNull(),
                            status = status
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = customerId.isNotBlank() &&
                        visitDate.isNotBlank() &&
                        visitTime.isNotBlank() &&
                        !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Visit" else "Schedule Visit")
                }
            }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        }

        uiState.error?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { viewModel.clearMessages() }
            )
        }
    }
}