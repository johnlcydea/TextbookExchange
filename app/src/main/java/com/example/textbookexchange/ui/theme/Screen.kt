package com.example.textbookexchange.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object AddBook : Screen("addBook")
    object EditBook : Screen("editBook/{bookId}") {
        fun createRoute(bookId: Int) = "editBook/$bookId"
    }
}