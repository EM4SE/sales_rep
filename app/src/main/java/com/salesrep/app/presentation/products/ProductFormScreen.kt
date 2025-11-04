package com.salesrep.app.presentation.products

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.salesrep.app.presentation.components.*
import com.salesrep.app.util.BarcodeScannerDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var upc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<com.salesrep.app.domain.model.Category?>(null) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var scannerType by remember { mutableStateOf("") } // "sku" or "upc"
    var showImageOptions by remember { mutableStateOf(false) }

    val isEditMode = productId != null

    // Load categories on screen start
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScannerDialog = true
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            imageUri = it
            imageUrl = it.toString()
        }
    }

    // Camera photo launcher
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let {
                imageUri = it
                imageUrl = it.toString()
            }
        }
    }

    LaunchedEffect(productId) {
        productId?.let {
            viewModel.loadProductById(it)
        }
    }

    LaunchedEffect(uiState.selectedProduct) {
        if (isEditMode && uiState.selectedProduct != null) {
            val product = uiState.selectedProduct!!
            name = product.name
            description = product.description ?: ""
            price = product.price.toString()
            stock = product.stock.toString()
            sku = product.sku ?: ""
            upc = product.upc ?: ""
            imageUrl = product.imageUrl ?: ""
            // Find and set the selected category
            selectedCategory = uiState.categories.find { it.id == product.categoryId }
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
                title = { Text(if (isEditMode) "Edit Product" else "Add Product") },
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
            // Product Image Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Product Image",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (imageUri != null || imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUri ?: imageUrl,
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            imageUri = null
                            imageUrl = ""
                        }) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Image")
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable { showImageOptions = true },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text("Tap to add image")
                            }
                        }
                    }

                    if (imageUri == null && imageUrl.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg").apply {
                                            createNewFile()
                                        }
                                    )
                                    tempImageUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera")
                            }

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery")
                            }
                        }
                    }
                }
            }

            // Product Details
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price *") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock *") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // SKU with Scanner
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU") },
                leadingIcon = { Icon(Icons.Default.QrCode, null) },
                trailingIcon = {
                    IconButton(onClick = {
                        scannerType = "sku"
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                showScannerDialog = true
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, "Scan SKU")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // UPC with Scanner
            OutlinedTextField(
                value = upc,
                onValueChange = { upc = it },
                label = { Text("UPC") },
                leadingIcon = { Icon(Icons.Default.QrCode, null) },
                trailingIcon = {
                    IconButton(onClick = {
                        scannerType = "upc"
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                showScannerDialog = true
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, "Scan UPC")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategoryDropdown,
                onExpandedChange = { expandedCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false }
                ) {
                    if (uiState.categories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No categories available") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        category.description?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateProduct(
                            id = productId!!,
                            name = name,
                            description = description.ifEmpty { null },
                            price = price.toDoubleOrNull() ?: 0.0,
                            stock = stock.toIntOrNull() ?: 0,
                            sku = sku.ifEmpty { null },
                            upc = upc.ifEmpty { null },
                            imageUrl = imageUrl.ifEmpty { null },
                            categoryId = selectedCategory?.id ?: 1
                        )
                    } else {
                        viewModel.createProduct(
                            name = name,
                            description = description.ifEmpty { null },
                            price = price.toDoubleOrNull() ?: 0.0,
                            stock = stock.toIntOrNull() ?: 0,
                            sku = sku.ifEmpty { null },
                            upc = upc.ifEmpty { null },
                            imageUrl = imageUrl.ifEmpty { null },
                            categoryId = selectedCategory?.id ?: 1
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank() &&
                        price.isNotBlank() &&
                        stock.isNotBlank() &&
                        selectedCategory != null &&
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
                    Text(if (isEditMode) "Update Product" else "Create Product")
                }
            }
        }

        // Barcode Scanner Dialog
        if (showScannerDialog) {
            BarcodeScannerDialog(
                onDismiss = { showScannerDialog = false },
                onBarcodeScanned = { value, format ->
                    when (scannerType) {
                        "sku" -> sku = value
                        "upc" -> upc = value
                    }
                    showScannerDialog = false
                }
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