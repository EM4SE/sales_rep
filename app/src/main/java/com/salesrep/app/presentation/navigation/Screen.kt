package com.salesrep.app.presentation.navigation

sealed class Screen(val route: String) {
    // Authentication
    object Login : Screen("login")
    object Register : Screen("register")
    object SelectUserType : Screen("select_user_type")

    // Dashboard
    object AdminDashboard : Screen("admin_dashboard")
    object SalesRepDashboard : Screen("salesrep_dashboard")

    // Customers
    object CustomerList : Screen("customers")
    object CustomerDetail : Screen("customers/{customerId}") {
        fun createRoute(customerId: Int) = "customers/$customerId"
    }
    object CustomerForm : Screen("customers/form?customerId={customerId}") {
        fun createRoute(customerId: Int? = null) =
            if (customerId != null) "customers/form?customerId=$customerId"
            else "customers/form"
    }

    // Products
    object ProductList : Screen("products")
    object ProductDetail : Screen("products/{productId}") {
        fun createRoute(productId: Int) = "products/$productId"
    }
    object ProductForm : Screen("products/form?productId={productId}") {
        fun createRoute(productId: Int? = null) =
            if (productId != null) "products/form?productId=$productId"
            else "products/form"
    }

    // Orders
    object OrderList : Screen("orders")
    object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(orderId: Int) = "orders/$orderId"
    }
    object OrderForm : Screen("orders/form?orderId={orderId}") {
        fun createRoute(orderId: Int? = null) =
            if (orderId != null) "orders/form?orderId=$orderId"
            else "orders/form"
    }

    // Visits
    object VisitList : Screen("visits")
    object VisitDetail : Screen("visits/{visitId}") {
        fun createRoute(visitId: Int) = "visits/$visitId"
    }
    object VisitForm : Screen("visits/form?visitId={visitId}") {
        fun createRoute(visitId: Int? = null) =
            if (visitId != null) "visits/form?visitId=$visitId"
            else "visits/form"
    }

    // Expenditures
    object ExpenditureList : Screen("expenditures")
    object ExpenditureDetail : Screen("expenditures/{expenditureId}") {
        fun createRoute(expenditureId: Int) = "expenditures/$expenditureId"
    }
    object ExpenditureForm : Screen("expenditures/form?expenditureId={expenditureId}") {
        fun createRoute(expenditureId: Int? = null) =
            if (expenditureId != null) "expenditures/form?expenditureId=$expenditureId"
            else "expenditures/form"
    }

    // Categories (Admin only)
    object CategoryList : Screen("categories")

    // Update your Screen sealed class to include:

    // Sales Reps (Admin only)
    object SaleRepList : Screen("salereps")
    object SaleRepDetail : Screen("salereps/{saleRepId}") {
        fun createRoute(saleRepId: Int) = "salereps/$saleRepId"
    }
    object SaleRepForm : Screen("salereps/form?saleRepId={saleRepId}") {
        fun createRoute(saleRepId: Int? = null) =
            if (saleRepId != null) "salereps/form?saleRepId=$saleRepId"
            else "salereps/form"
    }
}