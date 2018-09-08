package de.phash.manuel.asw.semux

import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.room.*
import android.content.Context

@Database(entities = arrayOf(SemuxAddress::class), version = 1)
abstract class SemuxAddressDatabase : RoomDatabase() {

    abstract fun semuxAddressDao(): SemuxAddressDao

     companion object {
         private var INSTANCE: SemuxAddressDatabase? = null

         fun getInstance(context: Context): SemuxAddressDatabase? {
             if (INSTANCE == null) {
                 synchronized(SemuxAddressDatabase::class) {
                     INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                             SemuxAddressDatabase::class.java, "semux.db")
                             .build()
                 }
             }
             return INSTANCE
         }

         fun destroyInstance() {
             INSTANCE = null
         }
     }


}