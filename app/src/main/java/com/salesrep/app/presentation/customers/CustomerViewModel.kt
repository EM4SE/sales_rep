package com.salesrep.app.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.repository.CustomerRepository
import com.salesrep.app.data.repository.OrderRepository
import com.salesrep.app.data.repository.VisitRepository
import com.salesrep.app.domain.model.Customer
import com.salesrep.app.domain.model.Order
import com.salesrep.app.domain.model.Visit
import com.salesrep.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerUiState(
    val isLoading: Boolean = false,
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val customerOrders: List<Order> = emptyList(),
    val customerVisits: List<Visit> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getCustomers().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                customers = result.data ?: emptyList(),
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

    fun loadCustomerById(id: Int) {
        viewModelScope.launch {
            // Load customer details
            customerRepository.getCustomerById(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                selectedCustomer = result.data
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

            // Load customer orders
            loadCustomerOrders(id)

            // Load customer visits
            loadCustomerVisits(id)
        }
    }

    private fun loadCustomerOrders(customerId: Int) {
        viewModelScope.launch {
            orderRepository.getOrders().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val customerOrders = result.data?.filter { it.customerId == customerId } ?: emptyList()
                        _uiState.update {
                            it.copy(customerOrders = customerOrders)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadCustomerVisits(customerId: Int) {
        viewModelScope.launch {
            visitRepository.getVisits().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val customerVisits = result.data?.filter { it.customerId == customerId } ?: emptyList()
                        _uiState.update {
                            it.copy(customerVisits = customerVisits)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun createCustomer(
        name: String,
        email: String,
        phone: String?,
        address: String?,
        city: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            customerRepository.createCustomer(
                name, email, phone, address, city, latitude, longitude
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Customer created successfully"
                            )
                        }
                        loadCustomers()
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

    fun updateCustomer(
        id: Int,
        name: String?,
        email: String?,
        phone: String?,
        address: String?,
        city: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            customerRepository.updateCustomer(
                id, name, email, phone, address, city, latitude, longitude
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Customer updated successfully"
                            )
                        }
                        loadCustomers()
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

    fun deleteCustomer(id: Int) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Customer deleted successfully"
                            )
                        }
                        loadCustomers()
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