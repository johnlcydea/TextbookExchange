package com.example.textbookexchange.data.local

                            import android.util.Log
                            import com.example.textbookexchange.Book
                            import com.google.firebase.database.ktx.database
                            import com.google.firebase.ktx.Firebase
                            import kotlinx.coroutines.CoroutineScope
                            import kotlinx.coroutines.Dispatchers
                            import kotlinx.coroutines.flow.Flow
                            import kotlinx.coroutines.flow.distinctUntilChanged
                            import kotlinx.coroutines.flow.onEach
                            import kotlinx.coroutines.launch
                            import kotlinx.coroutines.tasks.await
                            import kotlinx.coroutines.withContext

                            class BookRepository(private val bookDao: BookDao) {
                                private val database = Firebase.database.reference.child("books")
                                private var isNetworkAvailable: Boolean = false
                                private val TAG = "BookRepository"

                                // Network status setter
                                fun setNetworkStatus(isAvailable: Boolean) {
                                    isNetworkAvailable = isAvailable
                                    Log.d(TAG, "Network status set to: $isAvailable")
                                }

                                // Get all books as Flow
                                fun getAllBooks(): Flow<List<Book>> {
                                    Log.d(TAG, "getAllBooks: Called")

                                    // If network is available, fetch from Firebase first
                                    if (isNetworkAvailable) {
                                        Log.d(TAG, "getAllBooks: Network available, fetching from Firebase")
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val snapshot = database.get().await()
                                                val firebaseBooks = mutableListOf<Book>()
                                                for (bookSnapshot in snapshot.children) {
                                                    val book = bookSnapshot.getValue(Book::class.java)?.copy(firebaseId = bookSnapshot.key ?: "")
                                                    if (book != null) {
                                                        firebaseBooks.add(book)
                                                        bookDao.insert(book)
                                                        Log.d(TAG, "getAllBooks: Cached book from Firebase: $book")
                                                    }
                                                }
                                                Log.d(TAG, "getAllBooks: Fetched ${firebaseBooks.size} books from Firebase")
                                            } catch (e: Exception) {
                                                Log.e(TAG, "getAllBooks: Failed to fetch from Firebase: ${e.message}", e)
                                            }
                                        }
                                    }

                                    return bookDao.getAllBooksAsFlow()
                                        .onEach { books ->
                                            Log.d(TAG, "Flow emission with ${books.size} books")
                                        }
                                        .distinctUntilChanged()
                                }

                                suspend fun getAllBooksDirectly(): List<Book> {
                                    return bookDao.getAllBooks()
                                }

                                suspend fun getBookById(firebaseId: String): Book? {
                                    Log.d(TAG, "getBookById: Fetching book with firebaseId: $firebaseId")
                                    var book = withContext(Dispatchers.IO) {
                                        bookDao.getBookById(firebaseId)
                                    }
                                    if (book != null) {
                                        Log.d(TAG, "getBookById: Found book in local DB: $book")
                                        return book
                                    }

                                    if (isNetworkAvailable) {
                                        Log.d(TAG, "getBookById: Fetching from Firebase")
                                        try {
                                            val snapshot = database.child(firebaseId).get().await()
                                            book = snapshot.getValue(Book::class.java)?.copy(firebaseId = snapshot.key ?: "")
                                            if (book != null) {
                                                withContext(Dispatchers.IO) {
                                                    bookDao.insert(book)
                                                }
                                                Log.d(TAG, "getBookById: Cached book from Firebase in local DB: $book")
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "getBookById: Failed to fetch from Firebase: ${e.message}", e)
                                        }
                                    }
                                    return book
                                }

                                suspend fun addBook(book: Book) {
                                    Log.d(TAG, "addBook: Adding book: $book")
                                    // Generate a key first if not already set
                                    val bookId = if (book.firebaseId.isNotEmpty()) book.firebaseId else database.push().key ?: ""
                                    val newBook = book.copy(firebaseId = bookId)

                                    // Always insert to local DB first to trigger UI update
                                    withContext(Dispatchers.IO) {
                                        val syncStatus = if (isNetworkAvailable) Book.SYNC_STATUS_SYNCED else Book.SYNC_STATUS_PENDING_INSERT
                                        val bookToInsert = newBook.copy(syncStatus = syncStatus)
                                        bookDao.insert(bookToInsert)
                                        Log.d(TAG, "addBook: Added to local DB: $bookToInsert")
                                    }

                                    // Then sync to Firebase if possible
                                    if (isNetworkAvailable) {
                                        try {
                                            database.child(bookId).setValue(newBook).await()
                                            Log.d(TAG, "addBook: Added to Firebase successfully")
                                            // Update sync status if needed
                                            withContext(Dispatchers.IO) {
                                                bookDao.update(newBook.copy(syncStatus = Book.SYNC_STATUS_SYNCED))
                                                Log.d(TAG, "addBook: Updated sync status to SYNCED")
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "addBook: Failed to add to Firebase: ${e.message}", e)
                                        }
                                    }
                                }

                                suspend fun updateBook(book: Book) {
                                    Log.d(TAG, "updateBook: Updating book: $book")
                                    // Always update local DB first
                                    withContext(Dispatchers.IO) {
                                        val syncStatus = if (isNetworkAvailable) Book.SYNC_STATUS_SYNCED else Book.SYNC_STATUS_PENDING_UPDATE
                                        bookDao.update(book.copy(syncStatus = syncStatus))
                                        Log.d(TAG, "updateBook: Updated in local DB with status $syncStatus")
                                    }

                                    // Then try to update Firebase
                                    if (isNetworkAvailable && book.firebaseId.isNotEmpty()) {
                                        try {
                                            database.child(book.firebaseId).setValue(book).await()
                                            Log.d(TAG, "updateBook: Updated in Firebase successfully")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "updateBook: Failed to update in Firebase: ${e.message}", e)
                                            // Mark as pending update if Firebase update fails
                                            withContext(Dispatchers.IO) {
                                                bookDao.update(book.copy(syncStatus = Book.SYNC_STATUS_PENDING_UPDATE))
                                            }
                                        }
                                    }
                                }

                                // Fixed deleteBook implementation
                                suspend fun deleteBook(book: Book) {
                                    Log.d(TAG, "deleteBook: Deleting book: $book")

                                    // Always delete locally first regardless of network status
                                    // This ensures UI updates immediately
                                    try {
                                        withContext(Dispatchers.IO) {
                                            bookDao.delete(book)
                                            Log.d(TAG, "deleteBook: Successfully deleted from local DB")
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "deleteBook: Failed to delete from local DB: ${e.message}", e)
                                        return // Exit if local delete fails
                                    }

                                    // Then try to delete from Firebase if network is available
                                    if (isNetworkAvailable && book.firebaseId.isNotEmpty()) {
                                        try {
                                            database.child(book.firebaseId).removeValue().await()
                                            Log.d(TAG, "deleteBook: Successfully deleted from Firebase")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "deleteBook: Failed to delete from Firebase: ${e.message}", e)
                                            // If Firebase delete fails, we could re-insert the book marked for deletion
                                            // But for now we'll just log the error since the UI has already updated
                                        }
                                    } else if (!isNetworkAvailable && book.syncStatus == Book.SYNC_STATUS_SYNCED) {
                                        // If network is unavailable and book was previously synced,
                                        // we could track this deletion for later sync, but for now we'll just log
                                        Log.d(TAG, "deleteBook: Network unavailable, Firebase deletion will be skipped")
                                    }
                                }

                                suspend fun syncPendingChanges() {
                                    if (!isNetworkAvailable) {
                                        Log.d(TAG, "syncPendingChanges: Network unavailable, skipping sync")
                                        return
                                    }

                                    Log.d(TAG, "syncPendingChanges: Starting sync")
                                    // Sync pending inserts
                                    try {
                                        val pendingInserts = withContext(Dispatchers.IO) {
                                            bookDao.getPendingBooks(Book.SYNC_STATUS_PENDING_INSERT)
                                        }
                                        Log.d(TAG, "syncPendingChanges: Found ${pendingInserts.size} pending inserts")

                                        for (book in pendingInserts) {
                                            try {
                                                val bookId = if (book.firebaseId.isEmpty()) database.push().key ?: "" else book.firebaseId
                                                val updatedBook = book.copy(firebaseId = bookId)
                                                database.child(bookId).setValue(updatedBook).await()

                                                withContext(Dispatchers.IO) {
                                                    bookDao.update(updatedBook.copy(syncStatus = Book.SYNC_STATUS_SYNCED))
                                                    Log.d(TAG, "syncPendingChanges: Synced insert for book: $updatedBook")
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG, "syncPendingChanges: Failed to sync insert for book ${book.firebaseId}: ${e.message}", e)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "syncPendingChanges: Error processing pending inserts: ${e.message}", e)
                                    }

                                    // Sync pending updates
                                    try {
                                        val pendingUpdates = withContext(Dispatchers.IO) {
                                            bookDao.getPendingBooks(Book.SYNC_STATUS_PENDING_UPDATE)
                                        }
                                        Log.d(TAG, "syncPendingChanges: Found ${pendingUpdates.size} pending updates")

                                        for (book in pendingUpdates) {
                                            if (book.firebaseId.isNotEmpty()) {
                                                try {
                                                    database.child(book.firebaseId).setValue(book).await()
                                                    withContext(Dispatchers.IO) {
                                                        bookDao.update(book.copy(syncStatus = Book.SYNC_STATUS_SYNCED))
                                                        Log.d(TAG, "syncPendingChanges: Synced update for book: $book")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "syncPendingChanges: Failed to sync update for book ${book.firebaseId}: ${e.message}", e)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "syncPendingChanges: Error processing pending updates: ${e.message}", e)
                                    }

                                    // Sync pending deletes
                                    try {
                                        val pendingDeletes = withContext(Dispatchers.IO) {
                                            bookDao.getPendingBooks(Book.SYNC_STATUS_PENDING_DELETE)
                                        }
                                        Log.d(TAG, "syncPendingChanges: Found ${pendingDeletes.size} pending deletes")

                                        for (book in pendingDeletes) {
                                            if (book.firebaseId.isNotEmpty()) {
                                                try {
                                                    database.child(book.firebaseId).removeValue().await()
                                                    withContext(Dispatchers.IO) {
                                                        bookDao.delete(book)
                                                        Log.d(TAG, "syncPendingChanges: Synced delete for book: $book")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "syncPendingChanges: Failed to sync delete for book ${book.firebaseId}: ${e.message}", e)
                                                }
                                            } else {
                                                // No Firebase ID, just delete locally
                                                withContext(Dispatchers.IO) {
                                                    bookDao.delete(book)
                                                    Log.d(TAG, "syncPendingChanges: Deleted local-only book: $book")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "syncPendingChanges: Error processing pending deletes: ${e.message}", e)
                                    }
                                }
                            }