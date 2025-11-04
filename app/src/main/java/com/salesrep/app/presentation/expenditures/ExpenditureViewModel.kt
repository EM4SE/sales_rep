package com.salesrep.app.presentation.expenditures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.repository.ExpenditureRepository
import com.salesrep.app.domain.model.Expenditure
import com.salesrep.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenditureUiState(
    val isLoading: Boolean = false,
    val expenditures: List<Expenditure> = emptyList(),
    val selectedExpenditure: Expenditure? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ExpenditureViewModel @Inject constructor(
    private val expenditureRepository: ExpenditureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenditureUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadExpenditures()
    }

    fun loadExpenditures() {
        viewModelScope.launch {
            expenditureRepository.getExpenditures().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                expenditures = result.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadExpenditureById(id: Int) {
        viewModelScope.launch {
            expenditureRepository.getExpenditureById(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                selectedExpenditure = result.data
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun createExpenditure(
        title: String,
        description: String?,
        amount: Double,
        date: String,
        receiptImage: String?
    ) {
        viewModelScope.launch {
            expenditureRepository.createExpenditure(
                title, description, amount, date, receiptImage
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Expenditure created successfully"
                            )
                        }
                        loadExpenditures()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateExpenditure(
        id: Int,
        title: String?,
        description: String?,
        amount: Double?,
        date: String?,
        receiptImage: String?
    ) {
        viewModelScope.launch {
            expenditureRepository.updateExpenditure(
                id, title, description, amount, date, receiptImage
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Expenditure updated successfully"
                            )
                        }
                        loadExpenditures()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun deleteExpenditure(id: Int) {
        viewModelScope.launch {
            expenditureRepository.deleteExpenditure(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Expenditure deleted successfully"
                            )
                        }
                        loadExpenditures()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}