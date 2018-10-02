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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.APIService.Companion.FEE
import de.phash.manuel.asw.semux.APIService.Companion.SEMUXFORMAT
import de.phash.manuel.asw.semux.APIService.Companion.unvote
import de.phash.manuel.asw.semux.APIService.Companion.vote
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.json.transactionraw.RawTransaction
import de.phash.manuel.asw.semux.key.*
import de.phash.manuel.asw.util.*
import kotlinx.android.synthetic.main.activity_vote.*
import kotlinx.android.synthetic.main.password_prompt.view.*
import java.math.BigDecimal

class VoteActivity : AppCompatActivity() {

    var address = ""
    var nonce: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //    voteAddressTextView.text = savedInstanceState?.getString("delegatesAddress") ?:""
        voteReceivingAddressEditView.setText(intent.getStringExtra("delegatesAddress") ?: "")

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(voteReceivingAddressEditView, InputMethodManager.SHOW_FORCED)
        address = intent.getStringExtra("address")
        checkAccount()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (address.isNotEmpty())
            singleAccountActivity(this, address)
        else
            balanceActivity(this)

    }

    fun onChoseDelegateClick(view: View) {
        delegatesActivity(this, address)
    }

    fun onVoteTransactionClick(view: View) {
        handleTransaction(vote)
    }

    fun onUnvoteTransactionClick(view: View) {
        handleTransaction(unvote)
    }

    fun handleTransaction(option: String) {
        if (voteReceivingAddressEditView.text.toString().isNotEmpty() && voteAmountEditView.text.toString().isNotEmpty()) {
            if (isPasswordSet(this)) {
                passwordSecured(option)
            } else {
                createTransaction(option, "default")
            }
        } else {
            if (voteReceivingAddressEditView.text.toString().isEmpty())
                Toast.makeText(this, "Receiver is empty", Toast.LENGTH_LONG).show()
            if (voteAmountEditView.text.toString().isEmpty())
                Toast.makeText(this, "Amount to vote is empty", Toast.LENGTH_LONG).show()
        }
    }

    fun passwordSecured(vote: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val promptView = inflater.inflate(R.layout.password_prompt, null)
        dialogBuilder.setView(promptView)

        dialogBuilder.setCancelable(true).setOnCancelListener({ dialog ->
            dialog.dismiss()
        })
                .setPositiveButton("SEND") { dialog, which ->
                    Log.i("PASSWORD", "positive button")
                    if (promptView.enterOldPassword.text.toString().isEmpty()) {
                        Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                    } else {
                        if (isPasswordCorrect(this, promptView.enterOldPassword.text.toString())) {
                            createTransaction(vote, promptView.enterOldPassword.text.toString())

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

    private fun createTransaction(option: String, password: String) {
        try {
            Log.i("VOTE", "-creating transaction for $option")
            firebase("5", type = option, mFirebaseAnalytics = FirebaseAnalytics.getInstance(this))

            val receiver = Hex.decode0x(voteReceivingAddressEditView.text.toString())
            val account = getSemuxAddress(database, address)
            Log.i("VOTE", "for account ${account?.address ?: "not found"}")

            val decryptedAcc = decryptAccount(account!!, password)
            val senderPkey = Key(Hex.decode0x(decryptedAcc.privateKey))

            val inSem = BigDecimal(voteAmountEditView.text.toString())
            val inNano = inSem.multiply(APIService.SEMUXMULTIPLICATOR)

            val amount = Amount.Unit.NANO_SEM.of(inNano.toLong())
            val type = if (option.equals(vote)) TransactionType.VOTE else TransactionType.UNVOTE
            Log.i("VOTETX", "type = ${type.name}")

            nonce.let {
                val transaction = Transaction(APIService.NETWORK, type, receiver, amount, FEE, nonce!!.toLong(), System.currentTimeMillis(), Bytes.EMPTY_BYTES)
                val signedTx = transaction.sign(senderPkey)
                voteTransaction(signedTx)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //Log.e("SIGN", e.localizedMessage)
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }


    private fun voteTransaction(transaction: Transaction) {
        val raw = Hex.encode0x(transaction.toBytes())
        Log.i("VOTE", raw)
        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.TRANSACTION_RAW, raw)
        intent.putExtra(APIService.TYP,
                APIService.transfer)
        startService(intent)

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

    override fun onRestart() {
        Log.i("RESTART", "restart")
        super.onRestart()
        checkAccount()
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
            Log.i("SENDTX", "vote callback received for callback${bundle?.getString(APIService.TYP)
                    ?: " nix"}")
            if (bundle != null) {
                when (bundle.getString(APIService.TYP)) {
                    APIService.check -> check(bundle)
                    APIService.transfer -> vote(bundle)
                }
            }
        }

        private fun vote(bundle: Bundle) {
            val json = bundle.getString(APIService.JSON)
            val resultCode = bundle.getInt(APIService.RESULT)
            Log.i("SENDTX", "vote callback received")
            if (resultCode == Activity.RESULT_OK) {
                //  val account = Gson().fromJson(json, CheckBalance::class.java)
                val tx = Gson().fromJson(json, RawTransaction::class.java)
                Log.i("SENDTX", json)
                if (tx.success) {

                    voteAmountEditView.text.clear()
                    Toast.makeText(this@VoteActivity,
                            "vote done",
                            Toast.LENGTH_LONG).show()
                    checkAccount()
                } else {
                    Toast.makeText(this@VoteActivity,
                            tx.message,
                            Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this@VoteActivity, "transfer failed",
                        Toast.LENGTH_LONG).show()
            }
        }

        private fun check(bundle: Bundle) {
            Log.i("SENDTX", "check")
            val json = bundle.getString(APIService.JSON)
            val resultCode = bundle.getInt(APIService.RESULT)
            if (resultCode == Activity.RESULT_OK) {
                val account = Gson().fromJson(json, CheckBalance::class.java)
                Log.i("RES", json)
                if (account.result.address.equals(address)) {
                    nonce = account.result.nonce

                    val addressText = "${SEMUXFORMAT.format(BigDecimal(account.result.available).divide(APIService.SEMUXMULTIPLICATOR))} SEM"
                    voteAddressTextView.text = intent.getStringExtra("address")
                    voteAvailableTextView.text = addressText
                    val lockText = "${SEMUXFORMAT.format(BigDecimal(account.result.locked).divide(APIService.SEMUXMULTIPLICATOR))} SEM"
                    voteLockedTextView.text = lockText
                    votePendingTextView.text = account.result.pendingTransactionCount.toString()
                } else checkAccount()

            } else {
                Toast.makeText(this@VoteActivity, "check failed",
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
