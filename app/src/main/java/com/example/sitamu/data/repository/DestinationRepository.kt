package com.example.sitamu.data.repository

import com.example.sitamu.data.remote.DestinationContact
import com.example.sitamu.data.remote.DestinationDto
import com.example.sitamu.data.remote.DestinationPayload
import com.example.sitamu.data.remote.RecipientContactDto
import com.example.sitamu.data.remote.RecipientContactPayload
import com.example.sitamu.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import java.time.Instant

class DestinationRepository {

    private val supabase = SupabaseProvider.client

    suspend fun getDestinations(): List<DestinationContact> {
        val destinations = supabase
            .from("destinations")
            .select()
            .decodeList<DestinationDto>()

        val contacts = supabase
            .from("recipient_contacts")
            .select()
            .decodeList<RecipientContactDto>()
            .associateBy { it.destinationId }

        return destinations
            .map { destination ->
                DestinationContact(
                    destination = destination,
                    whatsapp = contacts[destination.id]?.whatsapp
                )
            }
            .sortedBy { it.destination.name.lowercase() }
    }

    suspend fun createDestination(
        name: String,
        division: String,
        whatsapp: String,
        active: Boolean
    ) {
        val destination = supabase
            .from("destinations")
            .insert(DestinationPayload(name, division, active)) {
                select()
            }
            .decodeSingle<DestinationDto>()

        supabase
            .from("recipient_contacts")
            .insert(RecipientContactPayload(destination.id, whatsapp, Instant.now().toString()))
    }

    suspend fun updateDestination(
        id: Long,
        name: String,
        division: String,
        whatsapp: String,
        active: Boolean
    ) {
        supabase
            .from("destinations")
            .update(DestinationPayload(name, division, active)) {
                filter { eq("id", id) }
            }

        val existingContacts = supabase
            .from("recipient_contacts")
            .select {
                filter { eq("destination_id", id) }
            }
            .decodeList<RecipientContactDto>()

        if (existingContacts.isEmpty()) {
            supabase
                .from("recipient_contacts")
                .insert(RecipientContactPayload(id, whatsapp, Instant.now().toString()))
        } else {
            supabase
                .from("recipient_contacts")
                .update({
                    set("whatsapp", whatsapp)
                    set("updated_at", Instant.now().toString())
                }) {
                    filter { eq("destination_id", id) }
                }
        }
    }

    suspend fun setDestinationActive(id: Long, active: Boolean) {
        supabase
            .from("destinations")
            .update({
                set("active", active)
            }) {
                filter { eq("id", id) }
            }
    }
}
