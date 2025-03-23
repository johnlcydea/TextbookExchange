package com.example.textbookexchange.data.local
import androidx.room.OnConflictStrategy
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.textbookexchange.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    @Query("SELECT * FROM books")
    fun getAllBooksAsFlow(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE firebaseId = :firebaseId")
    suspend fun getBookById(firebaseId: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM books WHERE syncStatus != :syncStatus")
    suspend fun getPendingBooks(syncStatus: Int): List<Book>


}