package com.example.aprobile.data

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.io.FileInputStream

/**
 * Converte um banco SQLite em texto puro (instalações antigas) para um banco SQLCipher
 * criptografado, preservando todos os dados. Usa ATTACH + sqlcipher_export().
 *
 * Se o arquivo já estiver criptografado, não faz nada.
 */
object DatabaseEncryptionMigrator {

    fun migrateIfNeeded(context: Context, dbName: String, passphrase: String) {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) return
        if (!isPlaintextSqlite(dbFile)) return

        val encryptedPath = dbFile.absolutePath + ".enc"
        val encryptedFile = File(encryptedPath)
        if (encryptedFile.exists()) encryptedFile.delete()

        try {
            val db = SQLiteDatabase.openOrCreateDatabase(dbFile, "", null, null, null)
            try {
                db.rawExecSQL("ATTACH DATABASE '$encryptedPath' AS encrypted KEY '$passphrase'")
                db.rawExecSQL("SELECT sqlcipher_export('encrypted')")
                db.rawExecSQL("DETACH DATABASE encrypted")
            } finally {
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedFile.delete()
            return
        }

        File(dbFile.absolutePath + "-wal").delete()
        File(dbFile.absolutePath + "-shm").delete()
        File(dbFile.absolutePath + "-journal").delete()

        if (!dbFile.delete()) {
            encryptedFile.delete()
            return
        }
        if (!encryptedFile.renameTo(dbFile)) {
            try {
                encryptedFile.copyTo(dbFile, overwrite = true)
            } finally {
                encryptedFile.delete()
            }
        }
    }

    private fun isPlaintextSqlite(file: File): Boolean {
        return try {
            val header = ByteArray(16)
            val read = FileInputStream(file).use { it.read(header) }
            read == 16 && String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
        } catch (e: Exception) {
            false
        }
    }
}
