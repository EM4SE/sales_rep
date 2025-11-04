package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.OrderDao
import com.salesrep.app.data.local.dao.OrderItemDao
import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateOrderItemRequest
import com.salesrep.app.data.remote.dto.CreateOrderRequest
import com.salesrep.app.data.remote.dto.UpdateOrderRequest
import com.salesrep.app.domain.model.Order
import com.salesrep.app.domain.model.OrderItem
import com.salesrep.app.util.NetworkUtils
import com.salesrep.app.util.PreferencesManager
import com.salesrep.app.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    fun getOrders(): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())

        val userType = preferencesManager.getUserType().first()
        val userId = preferencesManager.getUserId().first()

        val localOrders = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
            orderDao.getOrdersBySaleRep(userId ?: 0).first()
        } else {
            orderDao.getAllOrders().first()
        }
        emit(Resource.Success(localOrders.map { it.toDomain() }))

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getOrders()
                if (response.isSuccessful) {
                    response.body()?.let { orders ->
                        // Insert orders
                        orderDao.insertOrders(orders.map { it.toEntity() })

                        // Insert order items
                        orders.forEach { orderDto ->
                            orderDto.orderItems?.let { items ->
                                orderItemDao.insertOrderItems(items.map { it.toEntity() })
                            }
                        }

                        val updatedOrders = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
                            orderDao.getOrdersBySaleRep(userId ?: 0).first()
                        } else {
                            orderDao.getAllOrders().first()
                        }
                        emit(Resource.Success(updatedOrders.map { it.toDomain() }))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun getOrderById(id: Int): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())

        val localOrder = orderDao.getOrderById(id).first()
        if (localOrder != null) {
            val order = localOrder.toDomain()
            val items = orderItemDao.getOrderItemsByOrder(id).first()
            emit(Resource.Success(order.copy(items = items.map { it.toDomain() })))
        }

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getOrderById(id)
                if (response.isSuccessful) {
                    response.body()?.order?.let { orderDto ->
                        orderDao.insertOrder(orderDto.toEntity())
                        orderDto.orderItems?.let { items ->
                            orderItemDao.insertOrderItems(items.map { it.toEntity() })
                        }

                        val order = orderDto.toEntity().toDomain()
                        val items = orderDto.orderItems?.map { it.toEntity().toDomain() } ?: emptyList()
                        emit(Resource.Success(order.copy(items = items)))
                    }
                }
            } catch (e: Exception) {
                if (localOrder == null) {
                    emit(Resource.Error(e.message ?: "Error loading order"))
                }
            }
        }
    }

    suspend fun createOrder(
        customerId: Int,
        totalAmount: Double,
        discount: Double?,
        tax: Double?,
        status: String,
        signatureImage: String?,
        deliveredAt: String?,
        items: List<OrderItem>
    ): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())

        val orderItems = items.map {
            CreateOrderItemRequest(
                productId = it.productId,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                discount = it.discount,
                taxAmount = it.taxAmount
            )
        }

        val request = CreateOrderRequest(
            customerId = customerId,
            totalAmount = totalAmount,
            discount = discount,
            tax = tax,
            status = status,
            signatureImage = signatureImage,
            deliveredAt = deliveredAt,
            items = orderItems
        )

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.createOrder(request)
                if (response.isSuccessful) {
                    response.body()?.order?.let { orderDto ->
                        orderDao.insertOrder(orderDto.toEntity())
                        orderDto.orderItems?.let { items ->
                            orderItemDao.insertOrderItems(items.map { it.toEntity() })
                        }
                        emit(Resource.Success(orderDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to create order"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "order",
                entityId = "temp_${System.currentTimeMillis()}",
                operation = "CREATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Order will be created when online"))
        }
    }

    suspend fun updateOrder(
        id: Int,
        customerId: Int?,
        totalAmount: Double?,
        discount: Double?,
        tax: Double?,
        status: String?,
        signatureImage: String?,
        deliveredAt: String?
    ): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())

        val request = UpdateOrderRequest(
            customerId, totalAmount, discount, tax, status, signatureImage, deliveredAt
        )

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.updateOrder(id, request)
                if (response.isSuccessful) {
                    response.body()?.order?.let { orderDto ->
                        orderDao.insertOrder(orderDto.toEntity())
                        emit(Resource.Success(orderDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to update order"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "order",
                entityId = id.toString(),
                operation = "UPDATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Order will be updated when online"))
        }
    }

    suspend fun deleteOrder(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.deleteOrder(id)
                if (response.isSuccessful) {
                    orderDao.deleteOrder(id)
                    orderItemDao.deleteOrderItemsByOrder(id)
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to delete order"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "order",
                entityId = id.toString(),
                operation = "DELETE",
                jsonData = "",
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Order will be deleted when online"))
        }
    }
}