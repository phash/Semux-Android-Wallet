package de.phash.manuel.asw.semux

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface SemuxAddressDao {
    @Query("SELECT * from semuxaddress")
    fun getAll(): List<SemuxAddress>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(semuxAddress: SemuxAddress)

}