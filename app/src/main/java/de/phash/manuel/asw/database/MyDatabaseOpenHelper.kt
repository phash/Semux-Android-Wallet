package de.phash.manuel.asw.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "SemuxDatabase", null, 1) {
    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        val SEMUXADDRESS_TABLENAME = "SemuxAddress"

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable("SemuxAddress", true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                "address" to TEXT,
                "publickey" to TEXT,
                "privatekey" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable("SemuxAddress", true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(applicationContext)