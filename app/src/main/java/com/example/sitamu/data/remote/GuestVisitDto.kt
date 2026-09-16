package com.example.sitamu.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuestVisitDto(
    val id: String,
    val name: String,
    val institution: String? = null,
    val phone: String,

    @SerialName("destination_id")
    val destinationId: Long,

    @SerialName("destination_name")
    val destinationName: String,

    val purpose: String,
    val status: String,

    @SerialName("whatsapp_status")
    val whatsappStatus: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("approved_at")
    val approvedAt: String? = null,

    @SerialName("checked_in_at")
    val checkedInAt: String? = null,

    @SerialName("checked_out_at")
    val checkedOutAt: String? = null
)
