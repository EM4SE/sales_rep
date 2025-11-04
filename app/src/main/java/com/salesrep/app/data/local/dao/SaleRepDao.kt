package com.salesrep.app.data.local.dao

import androidx.room.*
import com.salesrep.app.data.local.entities.SaleRepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleRepDao {
    @Query("SELECT * FROM sale_reps ORDER BY name ASC")
    fun getAllSaleReps(): Flow<List<SaleRepEntity>>

    @Query("SELECT * FROM sale_reps WHERE id = :id")
    fun getSaleRepById(id: Int): Flow<SaleRepEntity?>

    @Query("SELECT * FROM sale_reps WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveSaleReps(): Flow<List<SaleRepEntity>>

    @Query("SELECT * FROM sale_reps WHERE isSynced = 0")
    suspend fun getUnsyncedSaleReps(): List<SaleRepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleRep(saleRep: SaleRepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleReps(saleReps: List<SaleRepEntity>)

    @Update
    suspend fun updateSaleRep(saleRep: SaleRepEntity)

    @Query("DELETE FROM sale_reps WHERE id = :id")
    suspend fun deleteSaleRep(id: Int)

    @Query("DELETE FROM sale_reps")
    suspend fun clearAll()
}