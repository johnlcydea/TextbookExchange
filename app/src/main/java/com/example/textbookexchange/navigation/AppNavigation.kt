package com.example.textbookexchange

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.textbookexchange.data.local.BookViewModel
import com.example.textbookexchange.screens.AddBookScreen
import com.example.textbookexchange.screens.BookDetailsScreen
import com.example.textbookexchange.screens.DashboardScreen
import com.example.textbookexchange.screens.EditBookScreen
import com.example.textbookexchange.screens.LoginScreen

@Composable
fun AppNavigation(viewModel: BookViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password, userId ->
                    navController.navigate("dashboard/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "dashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "User1"
            DashboardScreen(
                currentUserId = userId,
                viewModel = viewModel,
                onAddBookClick = { navController.navigate("addBook/$userId") },
                onEditBookClick = { bookId: String -> navController.navigate("editBook/$bookId") },
                onBookDetailsClick = { bookId: String -> navController.navigate("bookDetails/$bookId") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("dashboard/$userId") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "addBook/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "User1"
            AddBookScreen(
                currentUserId = userId,
                viewModel = viewModel,
                onBookAdded = { navController.popBackStack() }
            )
        }
        composable(
            route = "editBook/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            EditBookScreen(
                bookId = bookId,
                viewModel = viewModel,
                onBookUpdated = { navController.popBackStack() }
            )
        }
        composable(
            route = "bookDetails/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(
                bookId = bookId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}