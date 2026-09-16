package com.example.sitamu.data.remote

import io.github.jan.supabase.postgrest.from

class SupabaseRepository {

    private val supabase = SupabaseProvider.client

    suspend fun getDestinations(): List<DestinationDto> {
        return supabase
            .from("destinations")
            .select()
            .decodeList<DestinationDto>()
    }

    suspend fun getGuestVisits(): List<GuestVisitDto> {
        return supabase
            .from("guest_visits")
            .select()
            .decodeList<GuestVisitDto>()
    }

    suspend fun getPendingGuestVisits(): List<GuestVisitDto> {
        return supabase
            .from("guest_visits")
            .select {
                filter {
                    eq("status", "PENDING")
                }
            }
            .decodeList<GuestVisitDto>()
    }
}
