package com.example.sitamu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest_visits")
data class GuestVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val institutionCategory: String,
    val institutionName: String?,
    val address: String,
    val purpose: String,
    val personToMeet: String,
    val photoUri: String?,
    val visitDate: String, // Format: DD-MM-YYYY
    val visitTime: String, // Format: HH:mm
    val createdAt: Long,
    val updatedAt: Long?
)
