package com.example.sitamu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.local.AuthPreferences
import com.example.sitamu.data.model.GuestVisitEntity
import com.example.sitamu.data.model.AdminEntity
import com.example.sitamu.data.repository.AuthRepository
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.utils.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GuestRepositoryTest {
    @Test
    fun existingEmptyDatabaseIsRecoveredWithoutLosingVisits() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val old = Room.databaseBuilder(context, AppDatabase::class.java, "sitamu_database").build()
        val id = old.guestVisitDao().insertGuestVisit(sampleVisit())
        old.close()

        val reopened = AppDatabase.getInstance(context)
        assertNotNull(reopened.adminDao().getAdminByUsername("admin"))
        assertEquals("Siti", reopened.guestVisitDao().getGuestVisitById(id)?.name)
        reopened.close()
    }

    @Test
    fun existingAdminPasswordIsPreserved() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val old = Room.databaseBuilder(context, AppDatabase::class.java, "sitamu_database").build()
        val hash = SecurityUtils.hashPassword("password-lama")
        old.adminDao().insertAdmin(AdminEntity(name = "Petugas", username = "admin", passwordHash = hash))
        old.close()

        val reopened = AppDatabase.getInstance(context)
        assertEquals(1, reopened.adminDao().getAdminCount())
        assertEquals(hash, reopened.adminDao().getAdminByUsername("admin")?.passwordHash)
        reopened.close()
    }

    @Test
    fun freshDatabaseSupportsImmediateLoginAndLogout() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val auth = AuthRepository(db.adminDao(), AuthPreferences(context))

        assertTrue(auth.login("admin", "wrong").isFailure)
        assertFalse(auth.isLoggedIn.first())
        assertTrue(auth.login("admin", "admin123").isSuccess)
        assertTrue(auth.isLoggedIn.first())
        assertEquals("Administrator", auth.adminName.first())
        assertEquals(1, db.adminDao().getAdminCount())
        auth.logout()
        assertFalse(auth.isLoggedIn.first())
        db.close()
    }

    @Test
    fun visitsCanBeCreatedFilteredUpdatedObservedAndDeleted() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val guests = GuestRepository(db.guestVisitDao())
        val id = guests.insertGuestVisit(sampleVisit())
        guests.insertGuestVisit(sampleVisit().copy(name = "Budi", createdAt = 2000L))

        assertEquals(2, guests.totalCount.first())
        assertEquals("Budi", guests.latest5GuestVisits.first().first().name)
        assertEquals(1, guests.getCountInPeriod(0L, 1500L).first())
        assertEquals(listOf(id), guests.searchAndFilterGuests("Siti", "Pemerintah", 0L, 1500L).first().map { it.id })
        assertTrue(guests.searchAndFilterGuests("Siti", "Swasta", 0L, Long.MAX_VALUE).first().isEmpty())

        val original = requireNotNull(guests.getGuestVisitById(id))
        assertEquals(1, guests.updateGuestVisit(original.copy(name = "Siti Diperbarui")))
        assertEquals("Siti Diperbarui", guests.observeGuestVisitById(id).first()?.name)
        assertEquals(1, guests.deleteGuestVisit(original))
        assertNull(guests.getGuestVisitById(id))
        assertEquals(1, guests.totalCount.first())
        db.close()
    }
}

internal fun sampleVisit() = GuestVisitEntity(
    name = "Siti", phone = "08123456789", institutionCategory = "Pemerintah",
    institutionName = null, address = "Makassar", purpose = "Konsultasi",
    personToMeet = "Petugas", photoUri = null, visitDate = "14-09-2026",
    visitTime = "10:00", createdAt = 1000L, updatedAt = null
)
