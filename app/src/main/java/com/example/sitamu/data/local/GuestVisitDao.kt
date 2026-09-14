package com.example.sitamu.data.local

import androidx.room.*
import com.example.sitamu.data.model.GuestVisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestVisitDao {
    @Query("SELECT * FROM guest_visits ORDER BY createdAt DESC")
    fun getAllGuestVisits(): Flow<List<GuestVisitEntity>>

    @Query("SELECT * FROM guest_visits ORDER BY createdAt DESC LIMIT 5")
    fun getLatest5GuestVisits(): Flow<List<GuestVisitEntity>>

    @Query("SELECT * FROM guest_visits WHERE id = :id LIMIT 1")
    suspend fun getGuestVisitById(id: Long): GuestVisitEntity?

    @Query("SELECT * FROM guest_visits WHERE id = :id LIMIT 1")
    fun observeGuestVisitById(id: Long): Flow<GuestVisitEntity?>

    @Query("SELECT COUNT(*) FROM guest_visits")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM guest_visits WHERE createdAt BETWEEN :startTs AND :endTs")
    fun getCountInPeriod(startTs: Long, endTs: Long): Flow<Int>

    @Query("""
        SELECT * FROM guest_visits 
        WHERE (name LIKE :searchQuery OR phone LIKE :searchQuery OR institutionName LIKE :searchQuery OR purpose LIKE :searchQuery)
        AND (:category = '' OR institutionCategory = :category)
        AND (createdAt BETWEEN :startTs AND :endTs)
        ORDER BY createdAt DESC
    """)
    fun searchAndFilterGuests(
        searchQuery: String,
        category: String,
        startTs: Long,
        endTs: Long
    ): Flow<List<GuestVisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuestVisit(guestVisit: GuestVisitEntity): Long

    @Update
    suspend fun updateGuestVisit(guestVisit: GuestVisitEntity): Int

    @Delete
    suspend fun deleteGuestVisit(guestVisit: GuestVisitEntity): Int
}
