package com.codenzi.payday

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // E.g., "Checking", "Savings", "Cash"
    val balance: Double
)
