package com.example.textbookexchange.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.textbookexchange.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    val books: Flow<List<Book>> = repository.getAllBooks()

    fun addBook(book: Book) {
        viewModelScope.launch {
            repository.addBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    suspend fun getBookById(firebaseId: String): Book? {
        return repository.getBookById(firebaseId)
    }

    fun refreshBooks() {
        viewModelScope.launch {
            repository.getAllBooks() // This will trigger a fetch from Firebase if network is available
        }
    }
}