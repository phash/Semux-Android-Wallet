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

package de.phash.manuel.asw.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.ManageAccounts
import de.phash.manuel.asw.semux.SemuxAddress
import org.jetbrains.anko.db.*

fun createQRCode(text: String, width: Int): Bitmap? {

    Log.i("QR", "creating QR code")
    val multiFormatWriter = MultiFormatWriter()
    val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, width, width)
    val barcodeEncoder = BarcodeEncoder()
    Log.i("QR", "QR code created")
    return barcodeEncoder.createBitmap(bitMatrix)

}

fun deleteSemuxDBAccount(db: MyDatabaseOpenHelper, account: ManageAccounts) {
    Log.i("DELETE", "address: ${account.account.address}")
    var addressToSearch = account.account.address
    if (account.account.address.startsWith("0x")) {
        addressToSearch = account.account.address.substring(2)
    }
    db.use {
        delete(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME, "address = {address}", "address" to addressToSearch)
    }
}

val semuxAddressRowParser = classParser<SemuxAddress>()
fun getAddresses(db: MyDatabaseOpenHelper): List<SemuxAddress> = db.use {
    Log.i("DATABASE", "calling selectAll")
    select(tableName = MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME).parseList(semuxAddressRowParser)
}

fun getSemuxAddress(db: MyDatabaseOpenHelper, address: String): SemuxAddress? = db.use {
    Log.i("DATABASE", "calling selecctAddress: ${address}")
    var addressToSearch = address
    if (address.startsWith("0x")) {
        addressToSearch = address.substring(2)
    }
    select(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME)
            .whereArgs("${SemuxAddress.COLUMN_ADDRESS} = {address}", "address" to addressToSearch)
            .exec { parseList(classParser<SemuxAddress>()) }.getOrNull(0)

}

fun updateSemuxAddress(db: MyDatabaseOpenHelper, semuxAddress: SemuxAddress) {
    val values = semuxAddress.toContentValues()
    db.use {

        update(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME,
                SemuxAddress.COLUMN_IV to semuxAddress.iv,
                SemuxAddress.COLUMN_SALT to semuxAddress.salt,
                SemuxAddress.COLUMN_PRIVATEKEY to semuxAddress.privateKey

        )
                .whereArgs("${SemuxAddress.COLUMN_ID} = {id}", "id" to semuxAddress.id!!)
                .exec()
    }
}


fun checkBalanceForWallet(db: MyDatabaseOpenHelper, context: Context): Boolean {

    val addresses = getAddresses(db)
    addresses.forEach {
        updateAddress(it.address, context)
    }
    return addresses.isNotEmpty()

}

fun updateAddress(address: String, context: Context) {

    val intent = Intent(context, APIService::class.java)
    // add infos for the service which file to download and where to store
    intent.putExtra(APIService.ADDRESS, address)

    intent.putExtra(APIService.TYP,
            APIService.check)
    context.startService(intent)
}