package com.example.sitamu.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipientContactDto(
    @SerialName("destination_id")
    val destinationId: Long,
    val whatsapp: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

data class DestinationContact(
    val destination: DestinationDto,
    val whatsapp: String?
)

@Serializable
data class DestinationPayload(
    val name: String,
    val division: String,
    val active: Boolean
)

@Serializable
data class RecipientContactPayload(
    @SerialName("destination_id")
    val destinationId: Long,
    val whatsapp: String,
    @SerialName("updated_at")
    val updatedAt: String
)
