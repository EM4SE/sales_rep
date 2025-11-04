package com.salesrep.app.data.remote.interceptors

import android.content.Context
import com.salesrep.app.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class NetworkConnectionInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkUtils.isNetworkAvailable()) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }
}

class NoConnectivityException : IOException() {
    override val message: String
        get() = "No internet connection"
}