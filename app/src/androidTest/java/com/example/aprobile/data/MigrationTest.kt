package com.example.aprobile.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate3To4_preservesDataAndAddsDefaults() {
        var db = helper.createDatabase(TEST_DB, 3)
        db.execSQL(
            "INSERT INTO medications (name, dosage, instructions, times, startDate, endDate, enabled, currentQuantity, lowStockAlert, profile, colorTag) " +
                "VALUES ('Dipirona', '500mg', NULL, '08:00', 1, NULL, 1, 20, 5, 'Eu', 'Azul')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)

        val cursor = db.query("SELECT name, soundEnabled, vibrateEnabled FROM medications")
        assertTrue(cursor.moveToFirst())
        assertEquals("Dipirona", cursor.getString(0))
        assertEquals(1, cursor.getInt(1))
        assertEquals(1, cursor.getInt(2))
        cursor.close()
        db.close()
    }

    @Test
    fun migrate4To5_createsAppointmentsTable() {
        var db = helper.createDatabase(TEST_DB_4_5, 4)
        db.execSQL(
            "INSERT INTO medications (name, dosage, times, startDate, enabled, profile, colorTag, soundEnabled, vibrateEnabled) " +
                "VALUES ('Ibuprofeno', '400mg', '12:00', 1, 1, 'Eu', 'Verde', 1, 1)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB_4_5, 5, true, AppDatabase.MIGRATION_4_5)

        db.execSQL("INSERT INTO appointments (title, dateTime) VALUES ('Dentista', 1000)")
        val cursor = db.query("SELECT title, dateTime FROM appointments")
        assertTrue(cursor.moveToFirst())
        assertEquals("Dentista", cursor.getString(0))
        cursor.close()
        db.close()
    }

    @Test
    fun migrate5To6_createsHealthTables() {
        var db = helper.createDatabase(TEST_DB_5_6, 5)
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB_5_6, 6, true, AppDatabase.MIGRATION_5_6)

        db.execSQL("INSERT INTO health_profiles (profile, bloodType) VALUES ('Eu', 'O+')")
        db.execSQL("INSERT INTO health_events (profile, timestamp, type, title) VALUES ('Eu', 1000, 'FEVER', 'Febre')")

        val cursor = db.query("SELECT bloodType FROM health_profiles WHERE profile = 'Eu'")
        assertTrue(cursor.moveToFirst())
        assertEquals("O+", cursor.getString(0))
        cursor.close()
        db.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
        private const val TEST_DB_4_5 = "migration-test-4-5"
        private const val TEST_DB_5_6 = "migration-test-5-6"
    }
}
