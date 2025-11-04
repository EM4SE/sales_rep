package com.salesrep.app.presentation.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.repository.VisitRepository
import com.salesrep.app.domain.model.Visit
import com.salesrep.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisitUiState(
    val isLoading: Boolean = false,
    val visits: List<Visit> = emptyList(),
    val selectedVisit: Visit? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class VisitViewModel @Inject constructor(
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadVisits()
    }

    fun loadVisits() {
        viewModelScope.launch {
            visitRepository.getVisits().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                visits = result.data ?: emptyList(),
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

    fun loadVisitById(id: Int) {
        viewModelScope.launch {
            visitRepository.getVisitById(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                selectedVisit = result.data
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

    fun loadVisitsByDate(date: String, saleRepId: Int) {
        viewModelScope.launch {
            visitRepository.getVisitsByDate(date, saleRepId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                visits = result.data ?: emptyList()
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

    fun createVisit(
        customerId: Int,
        visitDate: String,
        visitTime: String,
        visitType: String,
        notes: String?,
        locationLat: Double?,
        locationLng: Double?,
        status: String
    ) {
        viewModelScope.launch {
            visitRepository.createVisit(
                customerId, visitDate, visitTime, visitType, notes, locationLat, locationLng, status
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Visit created successfully"
                            )
                        }
                        loadVisits()
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

    fun updateVisit(
        id: Int,
        customerId: Int?,
        visitDate: String?,
        visitTime: String?,
        visitType: String?,
        notes: String?,
        locationLat: Double?,
        locationLng: Double?,
        status: String?
    ) {
        viewModelScope.launch {
            visitRepository.updateVisit(
                id, customerId, visitDate, visitTime, visitType, notes, locationLat, locationLng, status
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Visit updated successfully"
                            )
                        }
                        loadVisits()
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

    fun deleteVisit(id: Int) {
        viewModelScope.launch {
            visitRepository.deleteVisit(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Visit deleted successfully"
                            )
                        }
                        loadVisits()
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