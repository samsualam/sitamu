package com.example.sitamu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sitamu.data.model.AdminEntity
import com.example.sitamu.data.model.GuestVisitEntity
import com.example.sitamu.utils.SecurityUtils

@Database(entities = [AdminEntity::class, GuestVisitEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adminDao(): AdminDao
    abstract fun guestVisitDao(): GuestVisitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sitamu_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Ensure first login works, including databases left empty by older builds.
                        // A single statement preserves all existing accounts and passwords.
                        db.execSQL(
                            "INSERT INTO admins (name, username, passwordHash) " +
                                "SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM admins)",
                            arrayOf("Administrator", "admin", SecurityUtils.hashPassword("admin123"))
                        )
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
