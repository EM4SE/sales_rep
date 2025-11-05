package com.salesrep.app.presentation.visits

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.salesrep.app.presentation.components.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VisitFormScreen(
    visitId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: VisitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerName by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var visitTime by remember { mutableStateOf("09:00") }
    var visitType by remember { mutableStateOf("Sales Call") }
    var notes by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("scheduled") }
    var expandedCustomer by remember { mutableStateOf(false) }
    var expandedVisitType by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }

    val isEditMode = visitId != null
    val visitTypes = listOf("Sales Call", "Follow-up", "Delivery", "Service", "Collection", "Product Demo")

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(visitId) {
        visitId?.let {
            viewModel.loadVisitById(it)
        }
    }

    LaunchedEffect(uiState.selectedVisit) {
        if (isEditMode && uiState.selectedVisit != null) {
            val visit = uiState.selectedVisit!!
            selectedCustomerId = visit.customerId
            selectedCustomerName = visit.customerName ?: "Customer #${visit.customerId}"
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
            // Customer Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCustomer,
                onExpandedChange = { expandedCustomer = it }
            ) {
                OutlinedTextField(
                    value = selectedCustomerName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Customer *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomer)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    placeholder = { Text("Choose a customer") }
                )
                ExposedDropdownMenu(
                    expanded = expandedCustomer,
                    onDismissRequest = { expandedCustomer = false }
                ) {
                    if (uiState.customers.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No customers available") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        uiState.customers.forEach { customer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = customer.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        customer.email?.let { email ->
                                            Text(
                                                text = email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedCustomerId = customer.id
                                    selectedCustomerName = customer.name
                                    expandedCustomer = false
                                }
                            )
                        }
                    }
                }
            }

            // Display selected customer info
            if (selectedCustomerId != null) {
                val customer = uiState.customers.find { it.id == selectedCustomerId }
                customer?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Customer Details",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            customer.phone?.let { phone ->
                                Text(
                                    text = "Phone: $phone",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            customer.address?.let { address ->
                                Text(
                                    text = "Address: $address",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = visitDate,
                onValueChange = { visitDate = it },
                label = { Text("Visit Date (YYYY-MM-DD) *") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("2025-11-05") }
            )

            OutlinedTextField(
                value = visitTime,
                onValueChange = { visitTime = it },
                label = { Text("Visit Time (HH:MM) *") },
                leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("09:00") }
            )

            // Quick Time Buttons
            Text(
                text = "Quick Times",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("09:00", "12:00", "15:00", "17:00").forEach { quickTime ->
                    OutlinedButton(
                        onClick = { visitTime = quickTime },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(quickTime)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visit Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedVisitType,
                onExpandedChange = { expandedVisitType = it }
            ) {
                OutlinedTextField(
                    value = visitType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Visit Type *") },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVisitType)
                    },
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
                minLines = 3,
                placeholder = { Text("Additional details about the visit") }
            )

            // Location Section with Map Integration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Visit Location (Optional)",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Button(
                            onClick = {
                                if (locationPermissions.allPermissionsGranted) {
                                    showMapDialog = true
                                } else {
                                    locationPermissions.launchMultiplePermissionRequest()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick on Map")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitude") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("6.9271") }
                        )

                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitude") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("79.8612") }
                        )
                    }

                    // Show location preview if coordinates are available
                    if (latitude.isNotBlank() && longitude.isNotBlank()) {
                        val lat = latitude.toDoubleOrNull()
                        val lng = longitude.toDoubleOrNull()

                        if (lat != null && lng != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 2.dp
                            ) {
                                LocationPreviewMap(
                                    latitude = lat,
                                    longitude = lng
                                )
                            }
                        }
                    }

                    // Permission status
                    if (!locationPermissions.allPermissionsGranted) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Location permission required for map picker",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    selectedCustomerId?.let { custId ->
                        if (isEditMode) {
                            viewModel.updateVisit(
                                id = visitId!!,
                                customerId = custId,
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
                                customerId = custId,
                                visitDate = visitDate,
                                visitTime = visitTime,
                                visitType = visitType,
                                notes = notes.ifEmpty { null },
                                locationLat = latitude.toDoubleOrNull(),
                                locationLng = longitude.toDoubleOrNull(),
                                status = status
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedCustomerId != null &&
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
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditMode) "Update Visit" else "Schedule Visit")
                }
            }

            // Helper text
            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Map Picker Dialog
        if (showMapDialog) {
            MapPickerDialog(
                initialLatitude = latitude.toDoubleOrNull() ?: 1.3521, // Singapore default
                initialLongitude = longitude.toDoubleOrNull() ?: 103.8198,
                onLocationSelected = { lat, lng ->
                    latitude = lat.toString()
                    longitude = lng.toString()
                    showMapDialog = false
                },
                onDismiss = { showMapDialog = false }
            )
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

@Composable
fun LocationPreviewMap(
    latitude: Double,
    longitude: Double
) {
    val location = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false
        )
    ) {
        Marker(
            state = MarkerState(position = location),
            title = "Visit Location"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerDialog(
    initialLatitude: Double,
    initialLongitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf(LatLng(initialLatitude, initialLongitude)) }
    var searchQuery by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    // Update selected location when camera moves
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            selectedLocation = cameraPositionState.position.target
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Visit Location",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search location...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Map with crosshair overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false
                        )
                    )

                    // Crosshair overlay
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Location Picker",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                            .offset(y = (-20).dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Location Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Selected Coordinates",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Lat: ${String.format("%.6f", selectedLocation.latitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Lng: ${String.format("%.6f", selectedLocation.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onLocationSelected(
                                selectedLocation.latitude,
                                selectedLocation.longitude
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm")
                    }
                }
            }
        }
    }
}