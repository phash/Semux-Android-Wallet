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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.util.createQRCode
import de.phash.manuel.asw.util.updateAddress
import kotlinx.android.synthetic.main.activity_single_balance.*
import java.math.BigDecimal

class SingleBalanceActivity : AppCompatActivity() {

    var address = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_balance)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        address = intent.getStringExtra("address")
        setUp()

    }

    private fun setUp() {
        updateAddress(address, this)
        singleBalanceAddress.text = address
        createQR()
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("RESTART", "address: $address")
        setUp()
    }

    fun onImageClick(view: View) {
        val intent = Intent(this, QrViewActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    fun onSendClick(view: View) {
        val intent = Intent(this, SendActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    fun onVoteClick(view: View) {
        val intent = Intent(this, VoteActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    fun onTransactionsClick(view: View) {
        val intent = Intent(this, TransactionsActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    private fun createQR() {
        qrAddressImageView.setImageBitmap(createQRCode(address, 200))
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

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i("RECEIVE", "DashBoard received Broadcast")
            val bundle = intent.extras
            if (bundle != null) {
                val json = bundle.getString(APIService.JSON)
                val resultCode = bundle.getInt(APIService.RESULT)
                if (resultCode == Activity.RESULT_OK) {
                    val account = Gson().fromJson(json, CheckBalance::class.java)
                    Log.i("RES", json)

                    if (account.result.address.equals(address)) {
                        singleBalanceAvailable.text = APIService.SEMUXFORMAT.format(BigDecimal(account.result.available).divide(APIService.SEMUXMULTIPLICATOR)) + " SEM"
                        singleBalanceLocked.text = APIService.SEMUXFORMAT.format(BigDecimal(account.result.locked).divide(APIService.SEMUXMULTIPLICATOR)) + " SEM"
                        singleTx.text = account.result.transactionCount.toString()
                        singlePendingTx.text = account.result.pendingTransactionCount.toString()
                    } else {
                        updateAddress(address, this@SingleBalanceActivity)
                    }

                } else {
                    Toast.makeText(this@SingleBalanceActivity, "check failed",
                            Toast.LENGTH_LONG).show()
                }
            }
        }
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
}
