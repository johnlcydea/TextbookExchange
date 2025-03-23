package com.example.textbookexchange.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.textbookexchange.Book
import com.example.textbookexchange.data.local.BookViewModel

@Composable
fun AddBookScreen(
    currentUserId: String,
    viewModel: BookViewModel,
    onBookAdded: () -> Unit
) {
    val title = remember { mutableStateOf("") }
    val author = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val imageUrl = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .padding(16.dp), // Add padding around the edges
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add spacing below the TextField
        )
        TextField(
            value = author.value,
            onValueChange = { author.value = it },
            label = { Text("Author") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add spacing below the TextField
        )
        TextField(
            value = price.value,
            onValueChange = { price.value = it },
            label = { Text("Price") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add spacing below the TextField
        )
        TextField(
            value = category.value,
            onValueChange = { category.value = it },
            label = { Text("Category") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add spacing below the TextField
        )
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add spacing below the TextField
        )
        TextField(
            value = imageUrl.value,
            onValueChange = { imageUrl.value = it },
            label = { Text("Image URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add spacing below the TextField
        )
        Button(
            onClick = {
                val book = Book(
                    firebaseId = "", // Will be set by BookViewModel
                    title = title.value,
                    author = author.value,
                    price = price.value.toDoubleOrNull() ?: 0.0,
                    category = category.value,
                    description = description.value,
                    imageUrl = imageUrl.value,
                    userId = currentUserId
                )
                Log.d("AddBookScreen", "Adding book with userId: $currentUserId, book: $book")
                viewModel.addBook(book)
                Log.d("AddBookScreen", "Book added, navigating back")
                onBookAdded()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp) // Add spacing above the Button
        ) {
            Text("Add Book")
        }
    }
}