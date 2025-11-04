package com.salesrep.app.presentation.visits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salesrep.app.presentation.components.*
import com.salesrep.app.presentation.customers.DetailRow
import com.salesrep.app.presentation.orders.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    visitId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: VisitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(visitId) {
        viewModel.loadVisitById(visitId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visit Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.selectedVisit != null -> {
                    val visit = uiState.selectedVisit!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Visit Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = visit.visitType,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusChip(status = visit.status)
                                }
                            }
                        }

                        // Customer Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Customer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow(
                                    icon = Icons.Default.Person,
                                    label = "Name",
                                    value = visit.customerName ?: "Customer #${visit.customerId}"
                                )
                                visit.customerAddress?.let {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    DetailRow(
                                        icon = Icons.Default.LocationOn,
                                        label = "Address",
                                        value = it
                                    )
                                }
                            }
                        }

                        // Schedule Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow(
                                    icon = Icons.Default.CalendarToday,
                                    label = "Date",
                                    value = visit.visitDate
                                )
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow(
                                    icon = Icons.Default.AccessTime,
                                    label = "Time",
                                    value = visit.visitTime
                                )
                            }
                        }

                        // Notes Card
                        visit.notes?.let { notes ->
                            if (notes.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Notes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = notes,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Location Card
                        if (visit.locationLat != null && visit.locationLng != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Location",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Lat: ${visit.locationLat}\nLng: ${visit.locationLng}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Action Buttons
                        if (visit.status == "scheduled") {
                            Button(
                                onClick = { showStatusDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Visit")
                            }
                        }
                    }
                }
                else -> {
                    EmptyState("Visit not found")
                }
            }

            if (showDeleteDialog) {
                ConfirmationDialog(
                    title = "Delete Visit",
                    message = "Are you sure you want to delete this visit?",
                    onConfirm = {
                        viewModel.deleteVisit(visitId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            if (showStatusDialog) {
                AlertDialog(
                    onDismissRequest = { showStatusDialog = false },
                    title = { Text("Complete Visit") },
                    text = { Text("Mark this visit as completed?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.updateVisit(
                                    id = visitId,
                                    customerId = null,
                                    visitDate = null,
                                    visitTime = null,
                                    visitType = null,
                                    notes = null,
                                    locationLat = null,
                                    locationLng = null,
                                    status = "completed"
                                )
                                showStatusDialog = false
                            }
                        ) {
                            Text("Complete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStatusDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}