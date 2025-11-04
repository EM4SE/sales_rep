package com.salesrep.app.presentation.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salesrep.app.domain.model.OrderItem
import com.salesrep.app.presentation.components.*
import com.salesrep.app.presentation.customers.CustomerViewModel
import com.salesrep.app.presentation.products.ProductViewModel
import java.text.NumberFormat
import java.util.*

data class OrderItemData(
    val productId: Int,
    val productName: String,
    val unitPrice: Double,
    var quantity: Int,
    var discount: Double = 0.0,
    var taxAmount: Double = 0.0
) {
    val lineTotal: Double
        get() = (quantity * unitPrice) - discount + taxAmount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormScreen(
    orderId: Int?,
    onNavigateBack: () -> Unit,
    orderViewModel: OrderViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val orderUiState by orderViewModel.uiState.collectAsState()
    val customerUiState by customerViewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()

    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    var orderItems by remember { mutableStateOf<List<OrderItemData>>(emptyList()) }
    var discount by remember { mutableStateOf("") }
    var tax by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("pending") }
    var notes by remember { mutableStateOf("") }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    // Calculate totals
    val subtotal = remember(orderItems) {
        orderItems.sumOf { it.quantity * it.unitPrice }
    }
    val totalDiscount = remember(orderItems, discount) {
        orderItems.sumOf { it.discount } + (discount.toDoubleOrNull() ?: 0.0)
    }
    val totalTax = remember(orderItems, tax) {
        orderItems.sumOf { it.taxAmount } + (tax.toDoubleOrNull() ?: 0.0)
    }
    val grandTotal = remember(subtotal, totalDiscount, totalTax) {
        subtotal - totalDiscount + totalTax
    }

    // Load data
    LaunchedEffect(Unit) {
        customerViewModel.loadCustomers()
        productViewModel.loadProducts()
    }

    LaunchedEffect(orderUiState.successMessage) {
        if (orderUiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Order") },
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Selection
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showCustomerDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Customer",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (selectedCustomerId != null) {
                                        customerUiState.customers.find { it.id == selectedCustomerId }?.name
                                            ?: "Select Customer"
                                    } else "Select Customer",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selectedCustomerId != null)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }

                // Order Items
                item {
                    Text(
                        "Order Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(orderItems) { item ->
                    OrderItemCard(
                        item = item,
                        onQuantityChange = { newQty ->
                            orderItems = orderItems.map {
                                if (it.productId == item.productId) it.copy(quantity = newQty) else it
                            }
                        },
                        onRemove = {
                            orderItems = orderItems.filter { it.productId != item.productId }
                        }
                    )
                }

                // Add Product Button
                item {
                    OutlinedButton(
                        onClick = { showProductDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Product")
                    }
                }

                // Order Summary
                if (orderItems.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Order Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                SummaryRow("Subtotal", currencyFormat.format(subtotal))

                                // Order Discount
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Order Discount")
                                    OutlinedTextField(
                                        value = discount,
                                        onValueChange = { discount = it },
                                        modifier = Modifier.width(120.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal
                                        ),
                                        prefix = { Text("$") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Tax
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tax")
                                    OutlinedTextField(
                                        value = tax,
                                        onValueChange = { tax = it },
                                        modifier = Modifier.width(120.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal
                                        ),
                                        prefix = { Text("$") }
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Total",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        currencyFormat.format(grandTotal),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Status Selection
                    item {
                        var expandedStatus by remember { mutableStateOf(false) }
                        val statuses = listOf("pending", "confirmed", "processing", "shipped", "delivered")

                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = it }
                        ) {
                            OutlinedTextField(
                                value = status.uppercase(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Order Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false }
                            ) {
                                statuses.forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption.uppercase()) },
                                        onClick = {
                                            status = statusOption
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Notes
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            }

            // Create Order Button
            if (orderItems.isNotEmpty() && selectedCustomerId != null) {
                Button(
                    onClick = {
                        val items = orderItems.map { item ->
                            OrderItem(
                                id = 0,
                                orderId = 0,
                                productId = item.productId,
                                quantity = item.quantity,
                                unitPrice = item.unitPrice,
                                discount = item.discount,
                                taxAmount = item.taxAmount
                            )
                        }

                        orderViewModel.createOrder(
                            customerId = selectedCustomerId!!,
                            totalAmount = grandTotal,
                            discount = discount.toDoubleOrNull(),
                            tax = tax.toDoubleOrNull(),
                            status = status,
                            signatureImage = null,
                            deliveredAt = null,
                            items = items
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    enabled = !orderUiState.isLoading
                ) {
                    if (orderUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.ShoppingCart, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Order - ${currencyFormat.format(grandTotal)}")
                    }
                }
            }
        }

        // Customer Selection Dialog
        if (showCustomerDialog) {
            CustomerSelectionDialog(
                customers = customerUiState.customers,
                onDismiss = { showCustomerDialog = false },
                onSelect = { customer ->
                    selectedCustomerId = customer.id
                    showCustomerDialog = false
                }
            )
        }

        // Product Selection Dialog
        if (showProductDialog) {
            ProductSelectionDialog(
                products = productUiState.products.filter { product ->
                    orderItems.none { it.productId == product.id }
                },
                onDismiss = { showProductDialog = false },
                onSelect = { product ->
                    orderItems = orderItems + OrderItemData(
                        productId = product.id,
                        productName = product.name,
                        unitPrice = product.price,
                        quantity = 1
                    )
                    showProductDialog = false
                }
            )
        }

        if (orderUiState.isLoading) {
            LoadingDialog()
        }

        orderUiState.error?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { orderViewModel.clearMessages() }
            )
        }
    }
}

@Composable
fun OrderItemCard(
    item: OrderItemData,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${currencyFormat.format(item.unitPrice)} × ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }

                    Text(
                        item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }

                // Line Total
                Text(
                    currencyFormat.format(item.lineTotal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionDialog(
    customers: List<com.salesrep.app.domain.model.Customer>,
    onDismiss: () -> Unit,
    onSelect: (com.salesrep.app.domain.model.Customer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isEmpty()) {
            customers
        } else {
            customers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    "Select Customer",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customers...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Customer List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(customer) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        customer.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        customer.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelectionDialog(
    products: List<com.salesrep.app.domain.model.Product>,
    onDismiss: () -> Unit,
    onSelect: (com.salesrep.app.domain.model.Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isEmpty()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.sku?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Select Product",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(product) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        product.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Stock: ${product.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (product.stock > 0)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                                Text(
                                    currencyFormat.format(product.price),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}