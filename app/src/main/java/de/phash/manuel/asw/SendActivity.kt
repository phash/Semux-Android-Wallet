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

package de.phash.manuel.asw

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.key.*
import kotlinx.android.synthetic.main.activity_send.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import java.math.BigDecimal
import java.text.DecimalFormat


class SendActivity : AppCompatActivity() {
    var locked = ""
    var address = ""
    var available = ""
    var nonce = ""
    val df = DecimalFormat("0.#########")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        address = intent.getStringExtra("address")
        locked = intent.getStringExtra("locked")
        available = intent.getStringExtra("available")
        checkAccount()
        val addressText = "${df.format(BigDecimal(available).divide(APIService.SEMUXMULTIPLICATOR))} SEM"
        sendAddressTextView.text = intent.getStringExtra("address")
        sendAvailableTextView.text = addressText
        val lockText = "${df.format(BigDecimal(locked).divide(APIService.SEMUXMULTIPLICATOR))} SEM"
        sendLockedTextView.text = lockText

    }

    fun onSendTransactionClick(view: View) {
        val semuxAddressList = getSemuxAddress(database)

        if (sendReceivingAddressEditView.text.toString().isNotEmpty() && sendAmountEditView.text.toString().isNotEmpty()) {
            createTransaction()
        } else {
            if (sendReceivingAddressEditView.text.toString().isEmpty())
                Toast.makeText(this, "Receiver is empty", Toast.LENGTH_LONG).show()
            if (sendAmountEditView.text.toString().isEmpty())
                Toast.makeText(this, "Amount to send is empty", Toast.LENGTH_LONG).show()
        }

    }

    private fun createTransaction() {
        try {

            val receiver = Hex.decode0x(sendReceivingAddressEditView.text.toString())
            val semuxAddressList = getSemuxAddress(database)
            Log.i("PKEY", semuxAddressList.get(0).privateKey)
            val senderPkey = Key(Hex.decode0x(semuxAddressList.get(0).privateKey))
            val amount = Amount(sendAmountEditView.text.toString().toLong() * APIService.SEMUXMULTIPLICATOR.toLong())
            val fee = Amount(5000000)
            var transaction = Transaction(APIService.NETWORK, TransactionType.TRANSFER, receiver, amount, fee, nonce.toLong(), System.currentTimeMillis(), Bytes.EMPTY_BYTES)

            val signedTx = transaction.sign(senderPkey)

            sendTransaction(transaction)
        } catch (e: Exception) {
            Log.e("SIGN", e.localizedMessage)
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT)
        }
    }


    private fun sendTransaction(transaction: Transaction) {
        var raw = Hex.encode0x(transaction.toBytes())
        Log.i("SEND", raw)
        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.TRANSACTION_RAW, raw)
        intent.putExtra(APIService.TYP,
                APIService.transfer)
        startService(intent)
        Toast.makeText(this, "service started", Toast.LENGTH_SHORT)

    }


    fun getSemuxAddress(db: MyDatabaseOpenHelper): List<SemuxAddress> = db.use {
        Log.i("PKEY", "address: ${address}")
        select(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME)
                .whereArgs("${SemuxAddress.COLUMN_ADDRESS} = {address}", "address" to address.substring(2))
                .exec { parseList(classParser<SemuxAddress>()) }

    }


    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(
                APIService.NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    fun checkAccount() {

        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.ADDRESS, address)
        intent.putExtra(APIService.TYP,
                APIService.check)
        startService(intent)
        Toast.makeText(this, "service started", Toast.LENGTH_SHORT)

    }


    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val bundle = intent.extras
            if (bundle != null) {
                when (bundle.getString(APIService.TYP)) {
                    APIService.check -> check(bundle)
                    APIService.transfer -> transfer(bundle)
                }

            }
        }

        private fun transfer(bundle: Bundle) {
            val json = bundle.getString(APIService.JSON)
            val resultCode = bundle.getInt(APIService.RESULT)
            if (resultCode == Activity.RESULT_OK) {
                //  val account = Gson().fromJson(json, CheckBalance::class.java)
                Log.i("RES", json)
                Toast.makeText(this@SendActivity,
                        "transfer done",
                        Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@SendActivity, "transfer failed",
                        Toast.LENGTH_LONG).show()

            }
        }

        private fun check(bundle: Bundle) {

            val json = bundle.getString(APIService.JSON)
            val resultCode = bundle.getInt(APIService.RESULT)
            if (resultCode == Activity.RESULT_OK) {
                val account = Gson().fromJson(json, CheckBalance::class.java)
                Log.i("RES", json)
                nonce = account.result.nonce
                Toast.makeText(this@SendActivity,
                        "checked: ${account.message}",
                        Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@SendActivity, "check failed",
                        Toast.LENGTH_LONG).show()

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        startNewActivity(item, this)
        return super.onOptionsItemSelected(item)
    }
}
