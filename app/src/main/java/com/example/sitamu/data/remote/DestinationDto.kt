package com.example.sitamu.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DestinationDto(
    val id: Long,
    val name: String,
    val division: String,
    val active: Boolean,

    @SerialName("created_at")
    val createdAt: String? = null
)