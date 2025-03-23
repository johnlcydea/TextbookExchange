package com.example.textbookexchange.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.textbookexchange.Book
import com.example.textbookexchange.data.local.BookViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    currentUserId: String,
    viewModel: BookViewModel,
    onAddBookClick: () -> Unit,
    onEditBookClick: (String) -> Unit,
    onBookDetailsClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val booksState = viewModel.books.collectAsState(initial = emptyList())
    val books = booksState.value
    Log.d("DashboardScreen", "Current userId: $currentUserId")
    Log.d("DashboardScreen", "Books in dashboard: $books")

    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    // State for delete loading
    var isDeleting by remember { mutableStateOf(false) }
    // State for refresh
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    try {
                        viewModel.refreshBooks()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to refresh: ${e.message ?: "Unknown error"}")
                    } finally {
                        isRefreshing = false
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with user info and buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Logged in as: $currentUserId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onAddBookClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("Add New Book")
                        }
                        Button(
                            onClick = { showLogoutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Logout")
                        }
                    }
                }

                // Main content
                if (books.isEmpty() && !isRefreshing) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No books available.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "My Listings",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val userBooks = books.filter { it.userId == currentUserId }
                        Log.d("DashboardScreen", "User books (userId: $currentUserId): $userBooks")
                        if (userBooks.isEmpty()) {
                            item {
                                Text(
                                    text = "You have no listings.",
                                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            items(userBooks) { book ->
                                BookItem(
                                    book = book,
                                    onClick = {
                                        if (book.firebaseId.isNotEmpty()) {
                                            onBookDetailsClick(book.firebaseId)
                                        }
                                    },
                                    onEditClick = {
                                        if (book.firebaseId.isNotEmpty()) {
                                            onEditBookClick(book.firebaseId)
                                        } else {
                                            Log.e("DashboardScreen", "Cannot edit book: Invalid book ID")
                                        }
                                    },
                                    onDeleteClick = {
                                        bookToDelete = book
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                        item {
                            Text(
                                text = "Books for Sale",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val otherBooks = books.filter { it.userId != currentUserId }
                        Log.d("DashboardScreen", "Other books: $otherBooks")
                        if (otherBooks.isEmpty()) {
                            item {
                                Text(
                                    text = "No books for sale.",
                                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            items(otherBooks) { book ->
                                BookItem(
                                    book = book,
                                    onClick = {
                                        if (book.firebaseId.isNotEmpty()) {
                                            onBookDetailsClick(book.firebaseId)
                                        }
                                    },
                                    onEditClick = null,
                                    onDeleteClick = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && bookToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                bookToDelete = null
                isDeleting = false
            },
            title = { Text("Confirm Delete") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Are you sure you want to delete '${bookToDelete!!.title}'?")
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(24.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            try {
                                viewModel.deleteBook(bookToDelete!!)
                                snackbarHostState.showSnackbar("Book deleted successfully")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    "Failed to delete book: ${e.message ?: "Unknown error"}"
                                )
                            } finally {
                                isDeleting = false
                                showDeleteDialog = false
                                bookToDelete = null
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        bookToDelete = null
                        isDeleting = false
                    },
                    enabled = !isDeleting
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (book.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = "Book cover",
                        modifier = Modifier.size(60.dp),
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_delete)
                    )
                }
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Author: ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Category: ${book.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Price: $${DecimalFormat("#,##0.00").format(book.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (onEditClick != null && onDeleteClick != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.size(width = 80.dp, height = 40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Edit", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(width = 80.dp, height = 40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}