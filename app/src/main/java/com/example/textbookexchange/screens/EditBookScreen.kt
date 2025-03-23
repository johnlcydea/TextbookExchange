package com.example.textbookexchange.screens

                    import androidx.compose.foundation.layout.Arrangement
                    import androidx.compose.foundation.layout.Column
                    import androidx.compose.foundation.layout.Spacer
                    import androidx.compose.foundation.layout.fillMaxSize
                    import androidx.compose.foundation.layout.fillMaxWidth
                    import androidx.compose.foundation.layout.height
                    import androidx.compose.foundation.layout.padding
                    import androidx.compose.foundation.rememberScrollState
                    import androidx.compose.foundation.text.KeyboardOptions
                    import androidx.compose.foundation.verticalScroll
                    import androidx.compose.material3.Button
                    import androidx.compose.material3.ExperimentalMaterial3Api
                    import androidx.compose.material3.MaterialTheme
                    import androidx.compose.material3.OutlinedTextField
                    import androidx.compose.material3.Scaffold
                    import androidx.compose.material3.Text
                    import androidx.compose.material3.TopAppBar
                    import androidx.compose.runtime.Composable
                    import androidx.compose.runtime.LaunchedEffect
                    import androidx.compose.runtime.getValue
                    import androidx.compose.runtime.mutableStateOf
                    import androidx.compose.runtime.remember
                    import androidx.compose.runtime.rememberCoroutineScope
                    import androidx.compose.runtime.setValue
                    import androidx.compose.ui.Alignment
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.text.input.KeyboardType
                    import androidx.compose.ui.unit.dp
                    import com.example.textbookexchange.data.local.BookViewModel
                    import kotlinx.coroutines.launch

                    @OptIn(ExperimentalMaterial3Api::class)
                    @Composable
                    fun EditBookScreen(
                        bookId: String,
                        viewModel: BookViewModel,
                        onBookUpdated: () -> Unit
                    ) {
                        var title by remember { mutableStateOf("") }
                        var author by remember { mutableStateOf("") }
                        var price by remember { mutableStateOf("") }
                        var description by remember { mutableStateOf("") }
                        var category by remember { mutableStateOf("") }
                        val coroutineScope = rememberCoroutineScope()

                        // Load book data
                        LaunchedEffect(key1 = bookId) {
                            val book = viewModel.getBookById(bookId)
                            book?.let {
                                title = it.title ?: ""
                                author = it.author ?: ""
                                price = it.price.toString()
                                description = it.description ?: ""
                                category = it.category ?: ""
                            }
                        }

                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("Edit Book") }
                                )
                            }
                        ) { paddingValues ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Title section with spacing
                                Text(
                                    text = "Book Details",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = author,
                                    onValueChange = { author = it },
                                    label = { Text("Author") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = category,
                                    onValueChange = { category = it },
                                    label = { Text("Category") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = { Text("Price") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    maxLines = 5
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Action buttons at the bottom with proper spacing
                                Button(
                                    onClick = {
                                        val priceValue = price.toDoubleOrNull() ?: 0.0
                                        coroutineScope.launch {
                                            viewModel.getBookById(bookId)?.let {
                                                val updatedBook = it.copy(
                                                    title = title,
                                                    author = author,
                                                    price = priceValue,
                                                    description = description,
                                                    category = category
                                                )
                                                viewModel.updateBook(updatedBook)
                                                onBookUpdated()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Update Book")
                                }

                                Button(
                                    onClick = { onBookUpdated() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel")
                                }

                                // Add bottom spacing for better UX
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }