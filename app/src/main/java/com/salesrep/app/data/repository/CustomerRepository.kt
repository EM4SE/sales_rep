package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.CustomerDao
import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateCustomerRequest
import com.salesrep.app.data.remote.dto.UpdateCustomerRequest
import com.salesrep.app.domain.model.Customer
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
class CustomerRepository @Inject constructor(
    private val apiService: ApiService,
    private val customerDao: CustomerDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    fun getCustomers(): Flow<Resource<List<Customer>>> = flow {
        emit(Resource.Loading())

        // Get user type to filter data
        val userType = preferencesManager.getUserType().first()
        val userId = preferencesManager.getUserId().first()

        // Emit local data first
        val localCustomers = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
            customerDao.getCustomersBySaleRep(userId ?: 0).first()
        } else {
            customerDao.getAllCustomers().first()
        }
        emit(Resource.Success(localCustomers.map { it.toDomain() }))

        // Sync with server if online
        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getCustomers()
                if (response.isSuccessful) {
                    response.body()?.let { customers ->
                        customerDao.insertCustomers(customers.map { it.toEntity() })

                        // Re-emit updated data
                        val updatedCustomers = if (userType == com.salesrep.app.util.Constants.USER_TYPE_SALES_REP) {
                            customerDao.getCustomersBySaleRep(userId ?: 0).first()
                        } else {
                            customerDao.getAllCustomers().first()
                        }
                        emit(Resource.Success(updatedCustomers.map { it.toDomain() }))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun getCustomerById(id: Int): Flow<Resource<Customer>> = flow {
        emit(Resource.Loading())

        val localCustomer = customerDao.getCustomerById(id).first()
        localCustomer?.let {
            emit(Resource.Success(it.toDomain()))
        }

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getCustomerById(id)
                if (response.isSuccessful) {
                    response.body()?.customer?.let { customerDto ->
                        customerDao.insertCustomer(customerDto.toEntity())
                        emit(Resource.Success(customerDto.toEntity().toDomain()))
                    }
                }
            } catch (e: Exception) {
                if (localCustomer == null) {
                    emit(Resource.Error(e.message ?: "Error loading customer"))
                }
            }
        }
    }

    suspend fun createCustomer(
        name: String,
        email: String,
        phone: String?,
        address: String?,
        city: String?,
        latitude: Double?,
        longitude: Double?
    ): Flow<Resource<Customer>> = flow {
        emit(Resource.Loading())

        val request = CreateCustomerRequest(name, email, phone, address, city, latitude, longitude)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.createCustomer(request)
                if (response.isSuccessful) {
                    response.body()?.customer?.let { customerDto ->
                        customerDao.insertCustomer(customerDto.toEntity())
                        emit(Resource.Success(customerDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to create customer"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            // Queue for later sync
            val syncItem = SyncQueueEntity(
                entityType = "customer",
                entityId = "temp_${System.currentTimeMillis()}",
                operation = "CREATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Customer will be created when online"))
        }
    }

    suspend fun updateCustomer(
        id: Int,
        name: String?,
        email: String?,
        phone: String?,
        address: String?,
        city: String?,
        latitude: Double?,
        longitude: Double?
    ): Flow<Resource<Customer>> = flow {
        emit(Resource.Loading())

        val request = UpdateCustomerRequest(name, email, phone, address, city, latitude, longitude)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.updateCustomer(id, request)
                if (response.isSuccessful) {
                    response.body()?.customer?.let { customerDto ->
                        customerDao.insertCustomer(customerDto.toEntity())
                        emit(Resource.Success(customerDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to update customer"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "customer",
                entityId = id.toString(),
                operation = "UPDATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Customer will be updated when online"))
        }
    }

    suspend fun deleteCustomer(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.deleteCustomer(id)
                if (response.isSuccessful) {
                    customerDao.deleteCustomer(id)
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to delete customer"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "customer",
                entityId = id.toString(),
                operation = "DELETE",
                jsonData = "",
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Customer will be deleted when online"))
        }
    }
}