package com.salesrep.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.repository.*
import com.salesrep.app.domain.model.*
import com.salesrep.app.util.PreferencesManager
import com.salesrep.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesRepDashboardStats(
    val myCustomers: Int = 0,
    val myOrders: Int = 0,
    val myVisits: Int = 0,
    val myExpenditures: Int = 0,
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val todayVisits: Int = 0,
    val completedVisits: Int = 0,
    val monthlyTarget: Double = 50000.0,
    val achievement: Double = 0.0
)

data class SalesRepDashboardUiState(
    val isLoading: Boolean = false,
    val stats: SalesRepDashboardStats = SalesRepDashboardStats(),
    val recentOrders: List<Order> = emptyList(),
    val todayVisits: List<Visit> = emptyList(),
    val recentExpenditures: List<Expenditure> = emptyList(),
    val topCustomers: List<Customer> = emptyList(),
    val userName: String = "",
    val error: String? = null
)

@HiltViewModel
class SalesRepDashboardViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val visitRepository: VisitRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesRepDashboardUiState())
    val uiState = _uiState.asStateFlow()

    private var currentSaleRepId: Int = 0

    init {
        loadUserData()
        loadDashboardData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            preferencesManager.getUserId().collect { userId ->
                currentSaleRepId = userId ?: 0
            }
        }

        viewModelScope.launch {
            preferencesManager.getUserName().collect { name ->
                _uiState.update { it.copy(userName = name ?: "") }
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            launch { loadCustomers() }
            launch { loadOrders() }
            launch { loadVisits() }
            launch { loadExpenditures() }
        }
    }

    private suspend fun loadCustomers() {
        customerRepository.getCustomers().collect { result ->
            if (result is Resource.Success) {
                val customers = result.data ?: emptyList()
                // In real app, filter by sale rep ID
                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(myCustomers = customers.size),
                        topCustomers = customers.take(5)
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
                    // In real app, filter by sale rep ID
                    val pendingOrders = orders.count { it.status == "pending" }
                    val completedOrders = orders.count {
                        it.status == "delivered" || it.status == "completed"
                    }
                    val totalSales = orders
                        .filter { it.status == "delivered" || it.status == "completed" }
                        .sumOf { it.totalAmount }

                    val achievement = if (totalSales > 0) {
                        (totalSales / 50000.0) * 100
                    } else 0.0

                    _uiState.update { state ->
                        state.copy(
                            stats = state.stats.copy(
                                myOrders = orders.size,
                                pendingOrders = pendingOrders,
                                completedOrders = completedOrders,
                                totalSales = totalSales,
                                achievement = achievement
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
                // In real app, filter by sale rep ID and today's date
                val todayVisits = visits.filter { it.status == "scheduled" }
                val completedVisits = visits.count { it.status == "completed" }

                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(
                            myVisits = visits.size,
                            todayVisits = todayVisits.size,
                            completedVisits = completedVisits
                        ),
                        todayVisits = todayVisits.take(5)
                    )
                }
            }
        }
    }

    private suspend fun loadExpenditures() {
        expenditureRepository.getExpenditures().collect { result ->
            if (result is Resource.Success) {
                val expenditures = result.data ?: emptyList()
                // In real app, filter by sale rep ID
                val totalExpenses = expenditures.sumOf { it.amount }

                _uiState.update { state ->
                    state.copy(
                        stats = state.stats.copy(
                            myExpenditures = expenditures.size,
                            totalExpenses = totalExpenses
                        ),
                        recentExpenditures = expenditures.sortedByDescending { it.date }.take(5)
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}