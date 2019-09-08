/*
 * MIT License
 *
 * Copyright (c) 2018 Manuel Roedig / Phash
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.phash.manuel.asw.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.semux.key.Network
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "SemuxDatabase.db", null, 5) {
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
        db.createTable(SEMUXADDRESS_TABLENAME, true,
                SemuxAddress.COLUMN_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                SemuxAddress.COLUMN_ADDRESS to TEXT,
                SemuxAddress.COLUMN_PRIVATEKEY to TEXT,
                SemuxAddress.COLUMN_SALT to TEXT,
                SemuxAddress.COLUMN_IV to TEXT,
                SemuxAddress.COLUMN_NETWORK to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        //db.dropTable("SemuxAddress", true)

        db.execSQL("ALTER TABLE SemuxAddress ADD network VARCHAR(32)")
        db.execSQL("UPDATE SemuxAddress SET network = \"${Network.MAINNET.label()}\" ")
        /*query("SemuxAddress", true,
                SemuxAddress.COLUMN_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                SemuxAddress.COLUMN_ADDRESS to TEXT,
                SemuxAddress.COLUMN_PRIVATEKEY to TEXT,
                SemuxAddress.COLUMN_SALT to TEXT,
                SemuxAddress.COLUMN_IV to TEXT)*/
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(applicationContext)