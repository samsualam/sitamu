package com.example.sitamu.data.repository

import com.example.sitamu.data.local.GuestVisitDao
import com.example.sitamu.data.model.GuestVisitEntity
import kotlinx.coroutines.flow.Flow

class GuestRepository(private val guestVisitDao: GuestVisitDao) {
    val allGuestVisits: Flow<List<GuestVisitEntity>> = guestVisitDao.getAllGuestVisits()
    val latest5GuestVisits: Flow<List<GuestVisitEntity>> = guestVisitDao.getLatest5GuestVisits()
    val totalCount: Flow<Int> = guestVisitDao.getTotalCount()

    suspend fun getGuestVisitById(id: Long): GuestVisitEntity? {
        return guestVisitDao.getGuestVisitById(id)
    }

    fun observeGuestVisitById(id: Long): Flow<GuestVisitEntity?> =
        guestVisitDao.observeGuestVisitById(id)

    fun getCountInPeriod(startTs: Long, endTs: Long): Flow<Int> {
        return guestVisitDao.getCountInPeriod(startTs, endTs)
    }

    fun searchAndFilterGuests(
        query: String,
        category: String,
        startTs: Long,
        endTs: Long
    ): Flow<List<GuestVisitEntity>> {
        val searchPattern = "%$query%"
        return guestVisitDao.searchAndFilterGuests(searchPattern, category, startTs, endTs)
    }

    suspend fun insertGuestVisit(guest: GuestVisitEntity): Long {
        return guestVisitDao.insertGuestVisit(guest)
    }

    suspend fun updateGuestVisit(guest: GuestVisitEntity): Int {
        return guestVisitDao.updateGuestVisit(guest)
    }

    suspend fun deleteGuestVisit(guest: GuestVisitEntity): Int {
        return guestVisitDao.deleteGuestVisit(guest)
    }
}
