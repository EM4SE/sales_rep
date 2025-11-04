package com.salesrep.app.presentation.salereps

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
import coil.compose.AsyncImage
import com.salesrep.app.presentation.components.*
import com.salesrep.app.presentation.customers.DetailRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleRepDetailScreen(
    saleRepId: Int,
    onNavigateBack: () -> Unit,
    viewModel: SaleRepViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saleRepId) {
        viewModel.loadSaleRepById(saleRepId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Rep Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
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
                uiState.selectedSaleRep != null -> {
                    val saleRep = uiState.selectedSaleRep!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Header
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (saleRep.profilePicture != null) {
                                    AsyncImage(
                                        model = saleRep.profilePicture,
                                        contentDescription = null,
                                        modifier = Modifier.size(120.dp)
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.size(120.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = saleRep.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = if (saleRep.isActive)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = if (saleRep.isActive) "Active" else "Inactive",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Contact Info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailRow(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = saleRep.email
                                )
                                saleRep.phone?.let {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    DetailRow(
                                        icon = Icons.Default.Phone,
                                        label = "Phone",
                                        value = it
                                    )
                                }
                                saleRep.region?.let {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    DetailRow(
                                        icon = Icons.Default.LocationOn,
                                        label = "Region",
                                        value = it
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    EmptyState("Sales Rep not found")
                }
            }

            if (showDeleteDialog) {
                ConfirmationDialog(
                    title = "Delete Sales Rep",
                    message = "Are you sure you want to delete this sales representative?",
                    onConfirm = {
                        viewModel.deleteSaleRep(saleRepId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
    }
}