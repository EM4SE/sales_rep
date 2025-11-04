package com.salesrep.app.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.repository.OrderRepository
import com.salesrep.app.domain.model.Order
import com.salesrep.app.domain.model.OrderItem
import com.salesrep.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getOrders().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                orders = result.data ?: emptyList(),
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

    fun loadOrderById(id: Int) {
        viewModelScope.launch {
            orderRepository.getOrderById(id).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                selectedOrder = result.data
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

    fun createOrder(
        customerId: Int,
        totalAmount: Double,
        discount: Double?,
        tax: Double?,
        status: String,
        signatureImage: String?,
        deliveredAt: String?,
        items: List<OrderItem>
    ) {
        viewModelScope.launch {
            orderRepository.createOrder(
                customerId, totalAmount, discount, tax, status, signatureImage, deliveredAt, items
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Order created successfully"
                            )
                        }
                        loadOrders()
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

    fun updateOrderStatus(id: Int, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrder(
                id = id,
                customerId = null,
                totalAmount = null,
                discount = null,
                tax = null,
                status = status,
                signatureImage = null,
                deliveredAt = if (status == "delivered")
                    java.time.LocalDateTime.now().toString() else null
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Order status updated"
                            )
                        }
                        loadOrders()
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