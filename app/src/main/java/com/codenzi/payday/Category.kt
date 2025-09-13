package com.codenzi.payday

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val icon: String, // Name of a drawable resource
    val color: String // Hex color code, e.g., "#FF5733"
)
