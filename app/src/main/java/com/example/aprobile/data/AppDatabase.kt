package com.example.aprobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MedicationEntity::class,
        DoseRecordEntity::class,
        AppointmentEntity::class,
        HealthProfileEntity::class,
        HealthEventEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN soundEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE medications ADD COLUMN vibrateEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `appointments` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`doctor` TEXT, " +
                        "`location` TEXT, " +
                        "`dateTime` INTEGER NOT NULL, " +
                        "`notes` TEXT)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_appointments_dateTime` ON `appointments` (`dateTime`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `health_profiles` (" +
                        "`profile` TEXT NOT NULL, `bloodType` TEXT, `allergies` TEXT, `conditions` TEXT, " +
                        "`weight` TEXT, `height` TEXT, `emergencyContact` TEXT, `notes` TEXT, PRIMARY KEY(`profile`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `health_events` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profile` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`value` TEXT, `notes` TEXT)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_events_profile` ON `health_events` (`profile`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_events_timestamp` ON `health_events` (`timestamp`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
