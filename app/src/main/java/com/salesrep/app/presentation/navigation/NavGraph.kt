package com.salesrep.app.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.salesrep.app.presentation.auth.LoginScreen
import com.salesrep.app.presentation.auth.RegisterScreen
import com.salesrep.app.presentation.categories.CategoryListScreen
import com.salesrep.app.presentation.customers.CustomerDetailScreen
import com.salesrep.app.presentation.customers.CustomerFormScreen
import com.salesrep.app.presentation.customers.CustomerListScreen
import com.salesrep.app.presentation.dashboard.AdminDashboardScreen
import com.salesrep.app.presentation.dashboard.SalesRepDashboardScreen
import com.salesrep.app.presentation.expenditures.ExpenditureDetailScreen
import com.salesrep.app.presentation.expenditures.ExpenditureFormScreen
import com.salesrep.app.presentation.expenditures.ExpenditureListScreen
import com.salesrep.app.presentation.orders.OrderDetailScreen
import com.salesrep.app.presentation.orders.OrderFormScreen
import com.salesrep.app.presentation.orders.OrderListScreen
import com.salesrep.app.presentation.products.ProductFormScreen
import com.salesrep.app.presentation.products.ProductDetailScreen
import com.salesrep.app.presentation.products.ProductListScreen
import com.salesrep.app.presentation.salereps.SaleRepDetailScreen
import com.salesrep.app.presentation.salereps.SaleRepFormScreen
import com.salesrep.app.presentation.salereps.SaleRepListScreen
import com.salesrep.app.presentation.visits.VisitDetailScreen
import com.salesrep.app.presentation.visits.VisitFormScreen
import com.salesrep.app.presentation.visits.VisitListScreen
import com.salesrep.app.util.Constants
import com.salesrep.app.util.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    preferencesManager: PreferencesManager
) {
    val isLoggedIn = runBlocking {
        preferencesManager.getAuthToken().first() != null
    }
    val userType = runBlocking {
        preferencesManager.getUserType().first()
    }

    val startDestination = when {
        !isLoggedIn -> Screen.SelectUserType.route
        userType == Constants.USER_TYPE_ADMIN -> Screen.AdminDashboard.route
        userType == Constants.USER_TYPE_SALES_REP -> Screen.SalesRepDashboard.route
        else -> Screen.SelectUserType.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication
        composable(Screen.SelectUserType.route) {
            SelectUserTypeScreen(
                onAdminLogin = { navController.navigate(Screen.Login.route + "/admin") },
                onSalesRepLogin = { navController.navigate(Screen.Login.route + "/salesrep") }
            )
        }

        composable(
            route = Screen.Login.route + "/{userType}",
            arguments = listOf(navArgument("userType") { type = NavType.StringType })
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "admin"
            LoginScreen(
                isAdmin = userType == "admin",
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { isAdmin ->
                    val destination = if (isAdmin)
                        Screen.AdminDashboard.route
                    else
                        Screen.SalesRepDashboard.route
                    navController.navigate(destination) {
                        popUpTo(Screen.SelectUserType.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.SelectUserType.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboards
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToCustomers = { navController.navigate(Screen.CustomerList.route) },
                onNavigateToProducts = { navController.navigate(Screen.ProductList.route) },
                onNavigateToOrders = { navController.navigate(Screen.OrderList.route) },
                onNavigateToVisits = { navController.navigate(Screen.VisitList.route) },
                onNavigateToExpenditures = { navController.navigate(Screen.ExpenditureList.route) },
                onNavigateToCategories = { navController.navigate(Screen.CategoryList.route) },
                onNavigateToSaleReps = { navController.navigate(Screen.SaleRepList.route) },
                onLogout = {
                    navController.navigate(Screen.SelectUserType.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SalesRepDashboard.route) {
            SalesRepDashboardScreen(
                onNavigateToCustomers = { navController.navigate(Screen.CustomerList.route) },
                onNavigateToProducts = { navController.navigate(Screen.ProductList.route) },
                onNavigateToOrders = { navController.navigate(Screen.OrderList.route) },
                onNavigateToVisits = { navController.navigate(Screen.VisitList.route) },
                onNavigateToExpenditures = { navController.navigate(Screen.ExpenditureList.route) },
                onLogout = {
                    navController.navigate(Screen.SelectUserType.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Customers
        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.CustomerForm.createRoute())
                }
            )
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getInt("customerId") ?: 0
            CustomerDetailScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.CustomerForm.createRoute(customerId))
                }
            )
        }

        composable(
            route = Screen.CustomerForm.route,
            arguments = listOf(navArgument("customerId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getInt("customerId")?.takeIf { it != -1 }
            CustomerFormScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Products
        composable(Screen.ProductList.route) {
            ProductListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.ProductForm.createRoute())
                }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.ProductForm.createRoute(productId))
                }
            )
        }

        composable(
            route = Screen.ProductForm.route,
            arguments = listOf(navArgument("productId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")?.takeIf { it != -1 }
            ProductFormScreen( // Using enhanced version with camera
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Orders
        composable(Screen.OrderList.route) {
            OrderListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.OrderForm.createRoute())
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderDetailScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.OrderForm.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.OrderForm.route,
            arguments = listOf(navArgument("orderId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId")?.takeIf { it != -1 }
            OrderFormScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Visits
        composable(Screen.VisitList.route) {
            VisitListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { visitId ->
                    navController.navigate(Screen.VisitDetail.createRoute(visitId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.VisitForm.createRoute())
                }
            )
        }

        composable(
            route = Screen.VisitDetail.route,
            arguments = listOf(navArgument("visitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getInt("visitId") ?: 0
            VisitDetailScreen(
                visitId = visitId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.VisitForm.createRoute(visitId))
                }
            )
        }

        composable(
            route = Screen.VisitForm.route,
            arguments = listOf(navArgument("visitId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getInt("visitId")?.takeIf { it != -1 }
            VisitFormScreen(
                visitId = visitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Expenditures
        composable(Screen.ExpenditureList.route) {
            ExpenditureListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { expenditureId ->
                    navController.navigate(Screen.ExpenditureDetail.createRoute(expenditureId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.ExpenditureForm.createRoute())
                }
            )
        }

        composable(
            route = Screen.ExpenditureDetail.route,
            arguments = listOf(navArgument("expenditureId") { type = NavType.IntType })
        ) { backStackEntry ->
            val expenditureId = backStackEntry.arguments?.getInt("expenditureId") ?: 0
            ExpenditureDetailScreen(
                expenditureId = expenditureId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.ExpenditureForm.createRoute(expenditureId))
                }
            )
        }

        composable(
            route = Screen.ExpenditureForm.route,
            arguments = listOf(navArgument("expenditureId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val expenditureId = backStackEntry.arguments?.getInt("expenditureId")?.takeIf { it != -1 }
            ExpenditureFormScreen(
                expenditureId = expenditureId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Categories
        composable(Screen.CategoryList.route) {
            CategoryListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Sale Reps
        composable(Screen.SaleRepList.route) {
            SaleRepListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { saleRepId ->
                    navController.navigate(Screen.SaleRepDetail.createRoute(saleRepId))
                },
                onNavigateToAdd = {
                    navController.navigate("salereps/form")
                }
            )
        }

        composable(
            route = Screen.SaleRepDetail.route,
            arguments = listOf(navArgument("saleRepId") { type = NavType.IntType })
        ) { backStackEntry ->
            val saleRepId = backStackEntry.arguments?.getInt("saleRepId") ?: 0
            SaleRepDetailScreen(
                saleRepId = saleRepId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("salereps/form") {
            SaleRepFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}