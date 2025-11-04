package com.salesrep.app.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salesrep.app.presentation.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToCustomers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToVisits: () -> Unit,
    onNavigateToExpenditures: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSaleReps: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome, Admin!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Sales Reps",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToSaleReps
                    )
                }
                item {
                    DashboardCard(
                        title = "Customers",
                        icon = Icons.Default.People,
                        onClick = onNavigateToCustomers
                    )
                }
                item {
                    DashboardCard(
                        title = "Products",
                        icon = Icons.Default.ShoppingCart,
                        onClick = onNavigateToProducts
                    )
                }
                item {
                    DashboardCard(
                        title = "Categories",
                        icon = Icons.Default.Category,
                        onClick = onNavigateToCategories
                    )
                }
                item {
                    DashboardCard(
                        title = "Orders",
                        icon = Icons.Default.Receipt,
                        onClick = onNavigateToOrders
                    )
                }
                item {
                    DashboardCard(
                        title = "Visits",
                        icon = Icons.Default.CalendarToday,
                        onClick = onNavigateToVisits
                    )
                }
                item {
                    DashboardCard(
                        title = "Expenditures",
                        icon = Icons.Default.AttachMoney,
                        onClick = onNavigateToExpenditures
                    )
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        }
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}