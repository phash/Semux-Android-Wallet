package de.phash.manuel.asw.semux

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class SemuxAddress(@PrimaryKey(autoGenerate = true) var id: Long?,
                        @ColumnInfo(name = "address") val address:String,
                        @ColumnInfo(name = "privateKey")  val privateKey: ByteArray,
                        @ColumnInfo(name = "publicKey") val publicKey: ByteArray
){
    constructor():this(null,"","".toByteArray(),"".toByteArray())
    constructor(toAddressString: String, toHexString: String?) : this()
}
