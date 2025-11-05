package com.salesrep.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salesrep.app.domain.model.Expenditure
import com.salesrep.app.domain.model.Order
import com.salesrep.app.domain.model.Visit
import com.salesrep.app.presentation.auth.AuthViewModel
import java.text.NumberFormat
import java.util.Locale

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
    authViewModel: AuthViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val uiState by dashboardViewModel.uiState.collectAsState()

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Dashboard")
                        Text(
                            "Overview & Analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Welcome Section
//                item {
//                    Text(
//                        text = "Welcome back!",
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                }



                // Statistics Grid
                item {
                    Text(
                        text = "Business Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Customers",
                                value = uiState.stats.totalCustomers.toString(),
                                icon = Icons.Default.People,
                                color = Color(0xFF2196F3),
                                onClick = onNavigateToCustomers,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Products",
                                value = uiState.stats.totalProducts.toString(),
                                subtitle = if (uiState.stats.lowStockProducts > 0)
                                    "${uiState.stats.lowStockProducts} low stock" else null,
                                icon = Icons.Default.Inventory,
                                color = Color(0xFF9C27B0),
                                onClick = onNavigateToProducts,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Orders",
                                value = uiState.stats.totalOrders.toString(),
                                subtitle = "${uiState.stats.pendingOrders} pending",
                                icon = Icons.Default.ShoppingCart,
                                color = Color(0xFFFF9800),
                                onClick = onNavigateToOrders,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Visits",
                                value = uiState.stats.totalVisits.toString(),
                                subtitle = "${uiState.stats.scheduledVisits} scheduled",
                                icon = Icons.Default.Event,
                                color = Color(0xFF00BCD4),
                                onClick = onNavigateToVisits,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Sales Reps",
                                value = uiState.stats.totalSaleReps.toString(),
                                icon = Icons.Default.Badge,
                                color = Color(0xFF3F51B5),
                                onClick = onNavigateToSaleReps,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Categories",
                                value = uiState.stats.totalCategories.toString(),
                                icon = Icons.Default.Category,
                                color = Color(0xFF009688),
                                onClick = onNavigateToCategories,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Expenditures",
                                value = uiState.stats.totalExpenditures.toString(),
                                icon = Icons.Default.Receipt,
                                color = Color(0xFFE91E63),
                                onClick = onNavigateToExpenditures,
                                modifier = Modifier.weight(1f)
                            )
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "More insights",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                // Key Metrics Cards
                item {
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Total Revenue",
                            value = currencyFormatter.format(uiState.stats.totalRevenue),
                            icon = Icons.Default.TrendingUp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Expenses",
                                value = currencyFormatter.format(uiState.stats.totalExpenses),
                                icon = Icons.Default.TrendingDown,
                                color = Color(0xFFF44336),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Net Profit",
                                value = currencyFormatter.format(uiState.stats.netProfit),
                                icon = Icons.Default.AccountBalance,
                                color = if (uiState.stats.netProfit >= 0) Color(0xFF2196F3) else Color(0xFFF44336),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            QuickActionButton(
                                title = "Customers",
                                icon = Icons.Default.People,
                                onClick = onNavigateToCustomers
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "View Products",
                                icon = Icons.Default.Inventory,
                                onClick = onNavigateToProducts
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "View Orders",
                                icon = Icons.Default.ShoppingCart,
                                onClick = onNavigateToOrders
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "View Visits",
                                icon = Icons.Default.Event,
                                onClick = onNavigateToVisits
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "Categories",
                                icon = Icons.Default.Category,
                                onClick = onNavigateToCategories
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "Sales Reps",
                                icon = Icons.Default.Badge,
                                onClick = onNavigateToSaleReps
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "Expenditures",
                                icon = Icons.Default.AttachMoney,
                                onClick = onNavigateToExpenditures
                            )
                        }
                    }
                }

                // Recent Orders
                if (uiState.recentOrders.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Orders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                uiState.recentOrders.forEach { order ->
                                    RecentOrderItem(order, currencyFormatter)
                                    if (order != uiState.recentOrders.last()) {
                                        Divider()
                                    }
                                }

                                TextButton(
                                    onClick = onNavigateToOrders,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("View All Orders")
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Visits
                if (uiState.recentVisits.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Visits",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                uiState.recentVisits.forEach { visit ->
                                    RecentVisitItem(visit)
                                    if (visit != uiState.recentVisits.last()) {
                                        Divider()
                                    }
                                }

                                TextButton(
                                    onClick = onNavigateToVisits,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("View All Visits")
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Expenditures
                if (uiState.recentExpenditures.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Expenditures",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                uiState.recentExpenditures.forEach { expenditure ->
                                    RecentExpenditureItem(expenditure, currencyFormatter)
                                    if (expenditure != uiState.recentExpenditures.last()) {
                                        Divider()
                                    }
                                }

                                TextButton(
                                    onClick = onNavigateToExpenditures,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("View All Expenditures")
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
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

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = color
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RecentOrderItem(order: Order, formatter: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Order #${order.id}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = order.createdAt ?: "Unknown date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatter.format(order.totalAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            StatusBadge(order.status)
        }
    }
}

@Composable
fun RecentVisitItem(visit: Visit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Visit #${visit.id}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${visit.visitDate} at ${visit.visitTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = visit.visitType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        StatusBadge(visit.status)
    }
}

@Composable
fun RecentExpenditureItem(expenditure: Expenditure, formatter: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expenditure.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = expenditure.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = formatter.format(expenditure.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> Color(0xFFFF9800) to "Pending"
        "completed", "delivered" -> Color(0xFF4CAF50) to "Completed"
        "scheduled" -> Color(0xFF2196F3) to "Scheduled"
        "cancelled" -> Color(0xFFF44336) to "Cancelled"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}