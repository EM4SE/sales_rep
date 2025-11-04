package com.salesrep.app.presentation.salereps

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salesrep.app.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleRepFormScreen(
    saleRepId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: SaleRepViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = saleRepId != null

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    // Load existing sale rep data if editing
    LaunchedEffect(saleRepId) {
        if (saleRepId != null) {
            viewModel.loadSaleRepById(saleRepId)
        }
    }

    // Initialize form with existing data
    LaunchedEffect(uiState.selectedSaleRep) {
        if (isEditMode && uiState.selectedSaleRep != null && !isInitialized) {
            val saleRep = uiState.selectedSaleRep!!
            name = saleRep.name
            email = saleRep.email
            phone = saleRep.phone ?: ""
            region = saleRep.region ?: ""
            profilePicture = saleRep.profilePicture ?: ""
            isActive = saleRep.isActive
            isInitialized = true
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
                title = { Text(if (isEditMode) "Edit Sales Representative" else "Add Sales Representative") },
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email *") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Password fields - optional for edit mode
            if (!isEditMode || password.isNotEmpty()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEditMode) "New Password (optional)" else "Password *") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(if (isEditMode) "Confirm New Password" else "Confirm Password *") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword
                )

                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("Region") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = profilePicture,
                onValueChange = { profilePicture = it },
                label = { Text("Profile Picture URL") },
                leadingIcon = { Icon(Icons.Default.Image, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Active")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isEditMode && saleRepId != null) {
                        viewModel.updateSaleRep(
                            id = saleRepId,
                            name = name,
                            email = email,
                            password = password.ifEmpty { null },
                            passwordConfirmation = confirmPassword.ifEmpty { null },
                            phone = phone.ifEmpty { null },
                            region = region.ifEmpty { null },
                            profilePicture = profilePicture.ifEmpty { null },
                            isActive = isActive
                        )
                    } else {
                        viewModel.createSaleRep(
                            name = name,
                            email = email,
                            password = password,
                            passwordConfirmation = confirmPassword,
                            phone = phone.ifEmpty { null },
                            region = region.ifEmpty { null },
                            profilePicture = profilePicture.ifEmpty { null },
                            isActive = isActive
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank() &&
                        email.isNotBlank() &&
                        (isEditMode || password.isNotBlank()) &&
                        (password.isEmpty() || password == confirmPassword) &&
                        !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Sales Rep" else "Create Sales Rep")
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