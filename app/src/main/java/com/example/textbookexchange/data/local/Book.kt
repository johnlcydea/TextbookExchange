package com.example.textbookexchange

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    val firebaseId: String = "",
    val title: String = "",
    val author: String? = null,
    val price: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val syncStatus: Int = SYNC_STATUS_SYNCED,
    val userId: String = ""
) {
    constructor() : this("", "", null, 0.0, "", "", "", SYNC_STATUS_SYNCED, "")

    companion object {
        const val SYNC_STATUS_SYNCED = 0
        const val SYNC_STATUS_PENDING_INSERT = 1
        const val SYNC_STATUS_PENDING_UPDATE = 2
        const val SYNC_STATUS_PENDING_DELETE = 3
    }
}