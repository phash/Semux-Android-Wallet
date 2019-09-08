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
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.APIService.Companion.FEE
import de.phash.manuel.asw.semux.APIService.Companion.SEMUXMULTIPLICATOR
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.json.transactionraw.RawTransaction
import de.phash.manuel.asw.semux.key.*
import de.phash.manuel.asw.util.*
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.android.synthetic.main.password_prompt.view.*
import java.math.BigDecimal


class SendActivity : AppCompatActivity() {

    var address = ""
    private var nonce: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        setTitle(if (APIService.NETWORK == Network.MAINNET)  R.string.semuxMain else R.string.semuxTest)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(sendReceivingAddressEditView, InputMethodManager.SHOW_FORCED)
        imm.showSoftInput(sendAmountEditView, InputMethodManager.SHOW_FORCED)
        val inputMgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        sendAmountEditView.showSoftInputOnFocus = true
        sendReceivingAddressEditView.showSoftInputOnFocus = true

        address = intent.getStringExtra("address")

        sendReceivingAddressEditView.setText( intent.getStringExtra("targetAddress")?:"")

        checkAccount()
    }

    override fun onRestart() {
        super.onRestart()
        checkAccount()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (address.isNotEmpty())
            singleAccountActivity(this, address)
        else
            balanceActivity(this)

    }

    fun onSendTransactionClick(view: View) {

        if (sendReceivingAddressEditView.text.toString().isNotEmpty() && sendAmountEditView.text.toString().isNotEmpty()) {
            if (isPasswordSet(this)) {
                passwordSecured()
            } else {
                createTransaction("default")
            }
        } else {
            if (sendReceivingAddressEditView.text.toString().isEmpty())
                Toast.makeText(this, "Receiver is empty", Toast.LENGTH_LONG).show()
            if (sendAmountEditView.text.toString().isEmpty())
                Toast.makeText(this, "Amount to send is empty", Toast.LENGTH_LONG).show()
        }
    }

    fun passwordSecured() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val promptView = inflater.inflate(R.layout.password_prompt, null)
        dialogBuilder.setView(promptView)

        dialogBuilder.setCancelable(true).setOnCancelListener(DialogInterface.OnCancelListener { dialog ->
            dialog.dismiss()
        })
                .setPositiveButton("SEND") { dialog, which ->
                    Log.i("PASSWORD", "positive button")
                    if (promptView.enterOldPassword.text.toString().isEmpty()) {
                        Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                    } else {
                        if (isPasswordCorrect(this, promptView.enterOldPassword.text.toString())) {
                            createTransaction(promptView.enterOldPassword.text.toString())

                        } else {
                            Log.i("PASSWORD", "PW false")
                            Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("CANCEL") { dialog, which ->
                    Log.i("PASSWORD", "negative button")
                    dialog.dismiss()
                }
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }


    private fun createTransaction(password: String) {
        try {

            val receiver = Hex.decode0x(sendReceivingAddressEditView.text.toString())
            val account = getSemuxAddress(database, address)

            val decryptedAcc = decryptAccount(account!!, password)
            val senderPkey = Key(Hex.decode0x(decryptedAcc.privateKey))

            val inSem = BigDecimal(sendAmountEditView.text.toString())
            val inNano = inSem.multiply(SEMUXMULTIPLICATOR)

            val amount = Amount.Unit.NANO_SEM.of(inNano.toLong())
            val type = TransactionType.TRANSFER
            Log.i("SENDTX", "type = ${type.name}")

            nonce.let {
                val transaction = Transaction(APIService.NETWORK, type, receiver, amount, FEE, nonce!!.toLong(), System.currentTimeMillis(), Bytes.EMPTY_BYTES)
                val signedTx = transaction.sign(senderPkey)
                sendTransaction(signedTx)

            }


        } catch (e: CryptoException) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SIGN", e.localizedMessage ?: "NIX")
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendTransaction(transaction: Transaction) {
        var raw = Hex.encode0x(transaction.toBytes())
        Log.i("SEND", raw)
        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.FORCE, true)
        intent.putExtra(APIService.TRANSACTION_RAW, raw)
        intent.putExtra(APIService.TYP,
                APIService.transfer)
        startService(intent)

    }
    fun onScanClicked(view: View){
        scanActivity(this, address)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(
                APIService.NOTIFICATION_TRANSFER))
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
    }


    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val bundle = intent.extras
            if (bundle != null) {
                Log.i("SENDTX", "received callback for ${bundle.getString(APIService.TYP)}")
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
                val tx = Gson().fromJson(json, RawTransaction::class.java)
                Log.i("SENDTX", json)
                if (tx.success) {

                    sendAmountEditView.setText("", TextView.BufferType.EDITABLE)
                    Toast.makeText(this@SendActivity,
                            "transfer done",
                            Toast.LENGTH_LONG).show()
                    checkAccount()
                } else {
                    Toast.makeText(this@SendActivity,
                            tx.message,
                            Toast.LENGTH_LONG).show()

                }
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
                if (account.result.address.equals(address)) {

                    nonce = account.result.nonce
                    sendAddressTextView.text = address
                    sendAvailableTextView.text = "${APIService.SEMUXFORMAT.format(BigDecimal(account.result.available).divide(APIService.SEMUXMULTIPLICATOR))} SEM"
                    sendLockedTextView.text = "${APIService.SEMUXFORMAT.format(BigDecimal(account.result.locked).divide(APIService.SEMUXMULTIPLICATOR))} SEM"
                    sendPendingTextView.text = account.result.pendingTransactionCount.toString()
                } else checkAccount()

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
