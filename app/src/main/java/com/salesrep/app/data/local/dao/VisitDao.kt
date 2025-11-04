package com.salesrep.app.data.local.dao

import androidx.room.*
import com.salesrep.app.data.local.entities.VisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits ORDER BY visitDate DESC, visitTime DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE saleRepId = :saleRepId ORDER BY visitDate DESC, visitTime DESC")
    fun getVisitsBySaleRep(saleRepId: Int): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE customerId = :customerId ORDER BY visitDate DESC, visitTime DESC")
    fun getVisitsByCustomer(customerId: Int): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE id = :id")
    fun getVisitById(id: Int): Flow<VisitEntity?>

    @Query("SELECT * FROM visits WHERE status = :status ORDER BY visitDate DESC, visitTime DESC")
    fun getVisitsByStatus(status: String): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE visitDate = :date AND saleRepId = :saleRepId ORDER BY visitTime ASC")
    fun getVisitsByDate(date: String, saleRepId: Int): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE isSynced = 0")
    suspend fun getUnsyncedVisits(): List<VisitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<VisitEntity>)

    @Update
    suspend fun updateVisit(visit: VisitEntity)

    @Query("DELETE FROM visits WHERE id = :id")
    suspend fun deleteVisit(id: Int)

    @Query("DELETE FROM visits")
    suspend fun clearAll()
}