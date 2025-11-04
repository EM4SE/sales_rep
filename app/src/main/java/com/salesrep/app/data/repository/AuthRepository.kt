package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.UserDao
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.LoginRequest
import com.salesrep.app.data.remote.dto.RegisterRequest
import com.salesrep.app.domain.model.SaleRep
import com.salesrep.app.domain.model.User
import com.salesrep.app.util.Constants
import com.salesrep.app.util.PreferencesManager
import com.salesrep.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) {
    suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    // Save token
                    preferencesManager.saveAuthToken(authResponse.token)

                    // Save user data
                    authResponse.user?.let { userDto ->
                        val user = userDto.toDomain()
                        userDao.insertUser(userDto.toEntity())
                        preferencesManager.saveUserData(
                            userId = user.id,
                            userType = Constants.USER_TYPE_ADMIN,
                            name = user.name,
                            email = user.email
                        )
                        emit(Resource.Success(user))
                    }
                } ?: emit(Resource.Error("Invalid response"))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    suspend fun saleRepLogin(email: String, password: String): Flow<Resource<SaleRep>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.saleRepLogin(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    // Save token
                    preferencesManager.saveAuthToken(authResponse.token)

                    // Save sale rep data
                    authResponse.saleRep?.let { saleRepDto ->
                        val saleRep = saleRepDto.toDomain()
                        preferencesManager.saveUserData(
                            userId = saleRep.id,
                            userType = Constants.USER_TYPE_SALES_REP,
                            name = saleRep.name,
                            email = saleRep.email
                        )
                        emit(Resource.Success(saleRep))
                    }
                } ?: emit(Resource.Error("Invalid response"))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.register(
                RegisterRequest(name, email, password, passwordConfirmation)
            )
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    preferencesManager.saveAuthToken(authResponse.token)
                    authResponse.user?.let { userDto ->
                        val user = userDto.toDomain()
                        userDao.insertUser(userDto.toEntity())
                        preferencesManager.saveUserData(
                            userId = user.id,
                            userType = Constants.USER_TYPE_ADMIN,
                            name = user.name,
                            email = user.email
                        )
                        emit(Resource.Success(user))
                    }
                } ?: emit(Resource.Error("Invalid response"))
            } else {
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    suspend fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                preferencesManager.clearAll()
                userDao.clearAll()
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Logout failed"))
            }
        } catch (e: Exception) {
            // Clear local data anyway
            preferencesManager.clearAll()
            emit(Resource.Success(Unit))
        }
    }
}