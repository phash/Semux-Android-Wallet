package de.phash.manuel.asw.semux

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.key.*
import de.phash.manuel.asw.util.decryptAccount
import de.phash.manuel.asw.util.getSemuxAddress
import kotlinx.android.synthetic.main.activity_send.*
import java.math.BigDecimal

public fun sendTransaction(transaction: Transaction, activity: Activity) {
    var raw = Hex.encode0x(transaction.toBytes())
    Log.i("SEND", raw)
    val intent = Intent(activity, APIService::class.java)
    // add infos for the service which file to download and where to store
    intent.putExtra(APIService.FORCE, true)
    intent.putExtra(APIService.TRANSACTION_RAW, raw)
    intent.putExtra(APIService.TYP,
            APIService.transfer)
    activity.startService(intent)
}

