package com.example.sitamu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val passwordHash: String
)
