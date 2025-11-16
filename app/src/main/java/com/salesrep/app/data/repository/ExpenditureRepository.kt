package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.ExpenditureDao
import com.salesrep.app.data.local.dao.SaleRepDao
import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateExpenditureRequest
import com.salesrep.app.data.remote.dto.UpdateExpenditureRequest
import com.salesrep.app.domain.model.Expenditure
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
class ExpenditureRepository @Inject constructor(
    private val apiService: ApiService,
    private val expenditureDao: ExpenditureDao,
    private val saleRepDao: SaleRepDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    fun getExpenditures(): Flow<Resource<List<Expenditure>>> = flow {
        emit(Resource.Loading())

        val userType = preferencesManager.getUserType().first()
        val userId = preferencesManager.getUserId().first()

        val localExpenditures = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
            expenditureDao.getExpendituresBySaleRep(userId ?: 0).first()
        } else {
            expenditureDao.getAllExpenditures().first()
        }

        // Enrich expenditures with sales rep info
        val enrichedExpenditures = localExpenditures.map { expenditureEntity ->
            val saleRep = saleRepDao.getSaleRepById(expenditureEntity.saleRepId).first()
            expenditureEntity.toDomain().copy(
                saleRepName = saleRep?.name,
                saleRepEmail = saleRep?.email,
                saleRepPhone = saleRep?.phone
            )
        }
        emit(Resource.Success(enrichedExpenditures))

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getExpenditures()
                if (response.isSuccessful) {
                    response.body()?.let { expenditures ->
                        expenditureDao.insertExpenditures(expenditures.map { it.toEntity() })

                        val updatedExpenditures = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
                            expenditureDao.getExpendituresBySaleRep(userId ?: 0).first()
                        } else {
                            expenditureDao.getAllExpenditures().first()
                        }

                        val updatedEnrichedExpenditures = updatedExpenditures.map { expenditureEntity ->
                            val saleRep = saleRepDao.getSaleRepById(expenditureEntity.saleRepId).first()
                            expenditureEntity.toDomain().copy(
                                saleRepName = saleRep?.name,
                                saleRepEmail = saleRep?.email,
                                saleRepPhone = saleRep?.phone
                            )
                        }
                        emit(Resource.Success(updatedEnrichedExpenditures))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun getExpenditureById(id: Int): Flow<Resource<Expenditure>> = flow {
        emit(Resource.Loading())

        val localExpenditure = expenditureDao.getExpenditureById(id).first()
        localExpenditure?.let { expenditureEntity ->
            val saleRep = saleRepDao.getSaleRepById(expenditureEntity.saleRepId).first()
            val enrichedExpenditure = expenditureEntity.toDomain().copy(
                saleRepName = saleRep?.name,
                saleRepEmail = saleRep?.email,
                saleRepPhone = saleRep?.phone
            )
            emit(Resource.Success(enrichedExpenditure))
        }

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getExpenditureById(id)
                if (response.isSuccessful) {
                    response.body()?.expenditure?.let { expenditureDto ->
                        expenditureDao.insertExpenditure(expenditureDto.toEntity())

                        val expenditureEntity = expenditureDao.getExpenditureById(id).first()
                        expenditureEntity?.let {
                            val saleRep = saleRepDao.getSaleRepById(it.saleRepId).first()
                            val enrichedExpenditure = it.toDomain().copy(
                                saleRepName = saleRep?.name,
                                saleRepEmail = saleRep?.email,
                                saleRepPhone = saleRep?.phone
                            )
                            emit(Resource.Success(enrichedExpenditure))
                        }
                    }
                }
            } catch (e: Exception) {
                if (localExpenditure == null) {
                    emit(Resource.Error(e.message ?: "Error loading expenditure"))
                }
            }
        }
    }

    suspend fun createExpenditure(
        title: String,
        description: String?,
        amount: Double,
        date: String,
        receiptImage: String?
    ): Flow<Resource<Expenditure>> = flow {
        emit(Resource.Loading())

        val request = CreateExpenditureRequest(title, description, amount, date, receiptImage)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.createExpenditure(request)
                if (response.isSuccessful) {
                    response.body()?.expenditure?.let { expenditureDto ->
                        expenditureDao.insertExpenditure(expenditureDto.toEntity())

                        val expenditureEntity = expenditureDao.getExpenditureById(expenditureDto.id).first()
                        expenditureEntity?.let {
                            val saleRep = saleRepDao.getSaleRepById(it.saleRepId).first()
                            val enrichedExpenditure = it.toDomain().copy(
                                saleRepName = saleRep?.name,
                                saleRepEmail = saleRep?.email,
                                saleRepPhone = saleRep?.phone
                            )
                            emit(Resource.Success(enrichedExpenditure))
                        }
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to create expenditure"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "expenditure",
                entityId = "temp_${System.currentTimeMillis()}",
                operation = "CREATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Expenditure will be created when online"))
        }
    }

    suspend fun updateExpenditure(
        id: Int,
        title: String?,
        description: String?,
        amount: Double?,
        date: String?,
        receiptImage: String?
    ): Flow<Resource<Expenditure>> = flow {
        emit(Resource.Loading())

        val request = UpdateExpenditureRequest(title, description, amount, date, receiptImage)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.updateExpenditure(id, request)
                if (response.isSuccessful) {
                    response.body()?.expenditure?.let { expenditureDto ->
                        expenditureDao.insertExpenditure(expenditureDto.toEntity())

                        val expenditureEntity = expenditureDao.getExpenditureById(expenditureDto.id).first()
                        expenditureEntity?.let {
                            val saleRep = saleRepDao.getSaleRepById(it.saleRepId).first()
                            val enrichedExpenditure = it.toDomain().copy(
                                saleRepName = saleRep?.name,
                                saleRepEmail = saleRep?.email,
                                saleRepPhone = saleRep?.phone
                            )
                            emit(Resource.Success(enrichedExpenditure))
                        }
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to update expenditure"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "expenditure",
                entityId = id.toString(),
                operation = "UPDATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Expenditure will be updated when online"))
        }
    }

    suspend fun deleteExpenditure(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.deleteExpenditure(id)
                if (response.isSuccessful) {
                    expenditureDao.deleteExpenditure(id)
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to delete expenditure"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "expenditure",
                entityId = id.toString(),
                operation = "DELETE",
                jsonData = "",
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Expenditure will be deleted when online"))
        }
    }
}