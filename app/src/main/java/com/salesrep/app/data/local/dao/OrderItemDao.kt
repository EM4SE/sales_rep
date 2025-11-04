package com.salesrep.app.data.local.dao

import androidx.room.*
import com.salesrep.app.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsByOrder(orderId: Int): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE id = :id")
    fun getOrderItemById(id: Int): Flow<OrderItemEntity?>

    @Query("SELECT * FROM order_items WHERE isSynced = 0")
    suspend fun getUnsyncedOrderItems(): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(orderItems: List<OrderItemEntity>)

    @Update
    suspend fun updateOrderItem(orderItem: OrderItemEntity)

    @Query("DELETE FROM order_items WHERE id = :id")
    suspend fun deleteOrderItem(id: Int)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsByOrder(orderId: Int)

    @Query("DELETE FROM order_items")
    suspend fun clearAll()
}