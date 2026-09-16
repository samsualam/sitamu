package com.example.sitamu.data.repository

import com.example.sitamu.data.remote.GuestVisitDto
import com.example.sitamu.data.remote.SupabaseProvider
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OnlineGuestRepository {

    private val supabase = SupabaseProvider.client

    suspend fun getAllGuestVisits(): List<GuestVisitDto> {
        return supabase
            .from("guest_visits")
            .select()
            .decodeList<GuestVisitDto>()
    }

    suspend fun getPendingGuestVisits(): List<GuestVisitDto> {
        return withTimeout(15_000L) {
            supabase
                .from("guest_visits")
                .select {
                    filter {
                        eq("status", "PENDING")
                    }
                }
                .decodeList<GuestVisitDto>()
        }
    }

    suspend fun approveGuestVisit(id: String) {
        supabase
            .from("guest_visits")
            .update({
                set("status", "APPROVED")
                set("approved_at", Instant.now().toString())
            }) {
                filter {
                    eq("id", id)
                }
            }
    }

    suspend fun rejectGuestVisit(id: String) {
        supabase
            .from("guest_visits")
            .update({
                set("status", "REJECTED")
            }) {
                filter {
                    eq("id", id)
                }
            }
    }

    suspend fun sendGuestWhatsapp(visitId: String) {
        supabase.functions.invoke(
            function = "send-guest-whatsapp",
            body = buildJsonObject {
                put("visitId", visitId)
            }
        )
    }
}
