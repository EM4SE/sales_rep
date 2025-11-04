package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.dao.VisitDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateVisitRequest
import com.salesrep.app.data.remote.dto.UpdateVisitRequest
import com.salesrep.app.domain.model.Visit
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
class VisitRepository @Inject constructor(
    private val apiService: ApiService,
    private val visitDao: VisitDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    fun getVisits(): Flow<Resource<List<Visit>>> = flow {
        emit(Resource.Loading())

        val userType = preferencesManager.getUserType().first()
        val userId = preferencesManager.getUserId().first()

        val localVisits = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
            visitDao.getVisitsBySaleRep(userId ?: 0).first()
        } else {
            visitDao.getAllVisits().first()
        }
        emit(Resource.Success(localVisits.map { it.toDomain() }))

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getVisits()
                if (response.isSuccessful) {
                    response.body()?.let { visits ->
                        visitDao.insertVisits(visits.map { it.toEntity() })

                        val updatedVisits = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
                            visitDao.getVisitsBySaleRep(userId ?: 0).first()
                        } else {
                            visitDao.getAllVisits().first()
                        }
                        emit(Resource.Success(updatedVisits.map { it.toDomain() }))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun getVisitById(id: Int): Flow<Resource<Visit>> = flow {
        emit(Resource.Loading())

        val localVisit = visitDao.getVisitById(id).first()
        localVisit?.let {
            emit(Resource.Success(it.toDomain()))
        }

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getVisitById(id)
                if (response.isSuccessful) {
                    response.body()?.visit?.let { visitDto ->
                        visitDao.insertVisit(visitDto.toEntity())
                        emit(Resource.Success(visitDto.toEntity().toDomain()))
                    }
                }
            } catch (e: Exception) {
                if (localVisit == null) {
                    emit(Resource.Error(e.message ?: "Error loading visit"))
                }
            }
        }
    }

    fun getVisitsByDate(date: String, saleRepId: Int): Flow<Resource<List<Visit>>> = flow {
        emit(Resource.Loading())
        val visits = visitDao.getVisitsByDate(date, saleRepId).first()
        emit(Resource.Success(visits.map { it.toDomain() }))
    }

    suspend fun createVisit(
        customerId: Int,
        visitDate: String,
        visitTime: String,
        visitType: String,
        notes: String?,
        locationLat: Double?,
        locationLng: Double?,
        status: String
    ): Flow<Resource<Visit>> = flow {
        emit(Resource.Loading())

        val request = CreateVisitRequest(
            customerId, visitDate, visitTime, visitType, notes, locationLat, locationLng, status
        )

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.createVisit(request)
                if (response.isSuccessful) {
                    response.body()?.visit?.let { visitDto ->
                        visitDao.insertVisit(visitDto.toEntity())
                        emit(Resource.Success(visitDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to create visit"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "visit",
                entityId = "temp_${System.currentTimeMillis()}",
                operation = "CREATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Visit will be created when online"))
        }
    }

    suspend fun updateVisit(
        id: Int,
        customerId: Int?,
        visitDate: String?,
        visitTime: String?,
        visitType: String?,
        notes: String?,
        locationLat: Double?,
        locationLng: Double?,
        status: String?
    ): Flow<Resource<Visit>> = flow {
        emit(Resource.Loading())

        val request = UpdateVisitRequest(
            customerId, visitDate, visitTime, visitType, notes, locationLat, locationLng, status
        )

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.updateVisit(id, request)
                if (response.isSuccessful) {
                    response.body()?.visit?.let { visitDto ->
                        visitDao.insertVisit(visitDto.toEntity())
                        emit(Resource.Success(visitDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to update visit"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "visit",
                entityId = id.toString(),
                operation = "UPDATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Visit will be updated when online"))
        }
    }

    suspend fun deleteVisit(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.deleteVisit(id)
                if (response.isSuccessful) {
                    visitDao.deleteVisit(id)
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to delete visit"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "visit",
                entityId = id.toString(),
                operation = "DELETE",
                jsonData = "",
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Visit will be deleted when online"))
        }
    }
}
