package com.salesrep.app.data.local.dao

import androidx.room.*
import com.salesrep.app.data.local.entities.ExpenditureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenditureDao {
    @Query("SELECT * FROM expenditures ORDER BY date DESC")
    fun getAllExpenditures(): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE saleRepId = :saleRepId ORDER BY date DESC")
    fun getExpendituresBySaleRep(saleRepId: Int): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE id = :id")
    fun getExpenditureById(id: Int): Flow<ExpenditureEntity?>

    @Query("SELECT * FROM expenditures WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpendituresByDateRange(startDate: String, endDate: String): Flow<List<ExpenditureEntity>>

    @Query("SELECT SUM(amount) FROM expenditures WHERE saleRepId = :saleRepId AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpenditureAmount(saleRepId: Int, startDate: String, endDate: String): Flow<Double?>

    @Query("SELECT * FROM expenditures WHERE isSynced = 0")
    suspend fun getUnsyncedExpenditures(): List<ExpenditureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenditure(expenditure: ExpenditureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenditures(expenditures: List<ExpenditureEntity>)

    @Update
    suspend fun updateExpenditure(expenditure: ExpenditureEntity)

    @Query("DELETE FROM expenditures WHERE id = :id")
    suspend fun deleteExpenditure(id: Int)

    @Query("DELETE FROM expenditures")
    suspend fun clearAll()
}