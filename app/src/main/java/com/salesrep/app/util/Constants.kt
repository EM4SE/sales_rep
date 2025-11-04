package com.salesrep.app.util

object Constants {
    // API
    const val BASE_URL = "https://your-api-url.com/api/"
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // DataStore Keys
    const val PREFERENCES_NAME = "sales_rep_preferences"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_TYPE = "user_type"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"

    // User Types
    const val USER_TYPE_ADMIN = "admin"
    const val USER_TYPE_SALES_REP = "sales_rep"

    // Sync
    const val SYNC_WORK_NAME = "sync_work"
    const val SYNC_INTERVAL_MINUTES = 15L

    // Database
    const val DATABASE_NAME = "sales_rep_db"
    const val DATABASE_VERSION = 1

    // Status Values (API expects 1/0, not true/false)
    const val STATUS_ACTIVE = 1
    const val STATUS_INACTIVE = 0

    // Order Status
    const val ORDER_STATUS_PENDING = "pending"
    const val ORDER_STATUS_CONFIRMED = "confirmed"
    const val ORDER_STATUS_PROCESSING = "processing"
    const val ORDER_STATUS_SHIPPED = "shipped"
    const val ORDER_STATUS_DELIVERED = "delivered"
    const val ORDER_STATUS_CANCELLED = "cancelled"

    // Visit Status
    const val VISIT_STATUS_SCHEDULED = "scheduled"
    const val VISIT_STATUS_COMPLETED = "completed"
    const val VISIT_STATUS_CANCELLED = "cancelled"
}