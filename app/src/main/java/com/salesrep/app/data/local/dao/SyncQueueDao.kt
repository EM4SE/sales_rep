package com.salesrep.app.data.local.dao

import androidx.room.*
import com.salesrep.app.data.local.entities.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getAllSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType ORDER BY timestamp ASC")
    suspend fun getSyncItemsByType(entityType: String): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(syncItem: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItems(syncItems: List<SyncQueueEntity>)

    @Update
    suspend fun updateSyncItem(syncItem: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncItem(id: Int)

    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteSyncItemByEntity(entityType: String, entityId: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}