package com.salesrep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.salesrep.app.data.local.dao.*
import com.salesrep.app.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        SaleRepEntity::class,
        CustomerEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        VisitEntity::class,
        ExpenditureEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun saleRepDao(): SaleRepDao
    abstract fun customerDao(): CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun visitDao(): VisitDao
    abstract fun expenditureDao(): ExpenditureDao
    abstract fun syncQueueDao(): SyncQueueDao
}