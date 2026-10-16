package com.example.aprobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        MedicationEntity::class,
        DoseRecordEntity::class,
        AppointmentEntity::class,
        HealthProfileEntity::class,
        HealthEventEntity::class,
        ProfileEntity::class
    ],
    version = 11,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun healthDao(): HealthDao
    abstract fun profileDao(): ProfileDao

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `profiles` (" +
                        "`name` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`name`))"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE health_events ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN scheduleType TEXT NOT NULL DEFAULT 'DAILY'")
                db.execSQL("ALTER TABLE medications ADD COLUMN daysOfWeek TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN barcode TEXT")
            }
        }

        private const val DB_NAME = "medication_database"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext

                // Carrega a lib nativa do SQLCipher antes de abrir/factory.
                System.loadLibrary("sqlcipher")

                val passphrase = DatabaseKeyStore.getOrCreatePassphrase(appContext)
                DatabaseEncryptionMigrator.migrateIfNeeded(appContext, DB_NAME, passphrase)
                val factory = SupportOpenHelperFactory(passphrase.toByteArray())

                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
