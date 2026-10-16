package com.example.aprobile.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "encryption_migration_test"

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migratesPlaintextDatabaseToEncrypted() = runBlocking {
        System.loadLibrary("sqlcipher")
        context.deleteDatabase(dbName)

        // 1) Cria banco em texto puro e insere um medicamento.
        val plain = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
        plain.medicationDao().insertMedication(
            MedicationEntity(name = "Teste Migracao", dosage = "1 comprimido", times = listOf("08:00"))
        )
        plain.close()

        // 2) Converte para criptografado (SQLCipher).
        val passphrase = DatabaseKeyStore.getOrCreatePassphrase(context)
        DatabaseEncryptionMigrator.migrateIfNeeded(context, dbName, passphrase)

        // 3) Abre criptografado e confirma que o dado sobreviveu.
        val encrypted = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .openHelperFactory(SupportOpenHelperFactory(passphrase.toByteArray()))
            .build()
        val meds = encrypted.medicationDao().getAllMedicationsSync()
        encrypted.close()

        assertEquals(1, meds.size)
        assertEquals("Teste Migracao", meds.first().name)
    }
}
