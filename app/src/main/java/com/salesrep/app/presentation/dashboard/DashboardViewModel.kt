package com.salesrep.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.repository.*
import com.salesrep.app.domain.model.*
import com.salesrep.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalCustomers: Int = 0,
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val totalVisits: Int = 0,
    val totalExpenditures: Int = 0,
    val totalSaleReps: Int = 0,
    val totalCategories: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val scheduledVisits: Int = 0,
    val completedVisits: Int = 0,
    val lowStockProducts: Int = 0
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val recentOrders: List<Order> = emptyList(),
    val recentVisits: List<Visit> = emptyList(),
    val recentExpenditures: List<Expenditure> = emptyList(),
    val topProducts: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val visitRepository: VisitRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load all data concurrently
            launch { loadCustomers() }
            launch { loadProducts() }
            launch { loadOrders() }
            launch { loadVisits() }
            launch { loadExpenditures() }
            launch { loadCategories() }
        }
    }

    private suspend fun loadCustomers() {
        customerRepository.getCustomers().collect { result ->
            if (result is Resource.Success) {
                val customers = result.data ?: emptyList()
                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(totalCustomers = customers.size)
                    )
                }
            }
        }
    }

    private suspend fun loadProducts() {
        productRepository.getProducts().collect { result ->
            if (result is Resource.Success) {
                val products = result.data ?: emptyList()
                val lowStockProducts = products.count { it.stock < 10 }
                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(
                            totalProducts = products.size,
                            lowStockProducts = lowStockProducts
                        ),
                        topProducts = products.take(5)
                    )
                }
            }
        }
    }

    private suspend fun loadOrders() {
        orderRepository.getOrders().collect { result ->
            when (result) {
                is Resource.Success -> {
                    val orders = result.data ?: emptyList()
                    val pendingOrders = orders.count { it.status == "pending" }
                    val completedOrders = orders.count { it.status == "delivered" || it.status == "completed" }
                    val totalRevenue = orders
                        .filter { it.status == "delivered" || it.status == "completed" }
                        .sumOf { it.totalAmount }

                    _uiState.update { state ->
                        val netProfit = totalRevenue - state.stats.totalExpenses
                        state.copy(
                            stats = state.stats.copy(
                                totalOrders = orders.size,
                                pendingOrders = pendingOrders,
                                completedOrders = completedOrders,
                                totalRevenue = totalRevenue,
                                netProfit = netProfit
                            ),
                            recentOrders = orders.sortedByDescending { it.createdAt }.take(5),
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private suspend fun loadVisits() {
        visitRepository.getVisits().collect { result ->
            if (result is Resource.Success) {
                val visits = result.data ?: emptyList()
                val scheduledVisits = visits.count { it.status == "scheduled" }
                val completedVisits = visits.count { it.status == "completed" }

                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(
                            totalVisits = visits.size,
                            scheduledVisits = scheduledVisits,
                            completedVisits = completedVisits
                        ),
                        recentVisits = visits.sortedByDescending { it.visitDate }.take(5)
                    )
                }
            }
        }
    }

    private suspend fun loadExpenditures() {
        expenditureRepository.getExpenditures().collect { result ->
            if (result is Resource.Success) {
                val expenditures = result.data ?: emptyList()
                val totalExpenses = expenditures.sumOf { it.amount }

                _uiState.update { state ->
                    val netProfit = state.stats.totalRevenue - totalExpenses
                    state.copy(
                        stats = state.stats.copy(
                            totalExpenditures = expenditures.size,
                            totalExpenses = totalExpenses,
                            netProfit = netProfit
                        ),
                        recentExpenditures = expenditures.sortedByDescending { it.date }.take(5)
                    )
                }
            }
        }
    }

    private suspend fun loadCategories() {
        categoryRepository.getCategories().collect { result ->
            if (result is Resource.Success) {
                val categories = result.data ?: emptyList()
                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(totalCategories = categories.size)
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}