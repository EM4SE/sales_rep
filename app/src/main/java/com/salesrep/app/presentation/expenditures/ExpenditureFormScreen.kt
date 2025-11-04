package com.salesrep.app.presentation.expenditures

import android.os.Build
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenditureFormScreen(
    expenditureId: Int?,
    onNavigateBack: () -> Unit,
    viewModel: ExpenditureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var receiptUrl by remember { mutableStateOf("") }

    val isEditMode = expenditureId != null

    LaunchedEffect(expenditureId) {
        expenditureId?.let {
            viewModel.loadExpenditureById(it)
        }
    }

    LaunchedEffect(uiState.selectedExpenditure) {
        if (isEditMode && uiState.selectedExpenditure != null) {
            val expenditure = uiState.selectedExpenditure!!
            title = expenditure.title
            description = expenditure.description ?: ""
            amount = expenditure.amount.toString()
            date = expenditure.date
            receiptUrl = expenditure.receiptImage ?: ""
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
                title = { Text(if (isEditMode) "Edit Expense" else "Add Expense") },
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Client lunch, Fuel, Hotel") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                placeholder = { Text("Additional details about this expense") }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount *") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("0.00") }
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD) *") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = receiptUrl,
                onValueChange = { receiptUrl = it },
                label = { Text("Receipt Image URL") },
                leadingIcon = { Icon(Icons.Default.Image, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://example.com/receipt.jpg") }
            )

            // Quick Amount Buttons
            Text(
                text = "Quick Amounts",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("10", "25", "50", "100").forEach { quickAmount ->
                    OutlinedButton(
                        onClick = { amount = quickAmount },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$$quickAmount")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateExpenditure(
                            id = expenditureId!!,
                            title = title,
                            description = description.ifEmpty { null },
                            amount = amount.toDoubleOrNull(),
                            date = date,
                            receiptImage = receiptUrl.ifEmpty { null }
                        )
                    } else {
                        viewModel.createExpenditure(
                            title = title,
                            description = description.ifEmpty { null },
                            amount = amount.toDouble(),
                            date = date,
                            receiptImage = receiptUrl.ifEmpty { null }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = title.isNotBlank() &&
                        amount.isNotBlank() &&
                        date.isNotBlank() &&
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
                    Text(if (isEditMode) "Update Expense" else "Add Expense")
                }
            }

            // Helper text
            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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