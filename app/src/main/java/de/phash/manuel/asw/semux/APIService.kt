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

package de.phash.manuel.asw.semux

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
//import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.json.delegates.Delegates
import de.phash.manuel.asw.semux.key.Amount
import de.phash.manuel.asw.semux.key.Network
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal
import java.text.DecimalFormat


class APIService : IntentService("SemuxService") {

    companion object {

        val SEMUXFORMAT = DecimalFormat("0.#########")
        val FEE = Amount.Unit.MILLI_SEM.of(5)
        val SEMUXMULTIPLICATOR = BigDecimal("1000000000")
        const val TYP = "type"
        const val ADDRESS = "address"
        const val RESULT = "result"
        const val JSON = "json"
        const val NOTIFICATION = "de.phash.manuel.asw.semux"
        const val NOTIFICATION_TRANSACTION = "de.phash.manuel.asw.semux.transaction"
        const val NOTIFICATION_TRANSFER = "de.phash.manuel.asw.semux.transfer"
        const val TRANSACTION_RAW = "transactionraw"
        const val VOTETYPE = "votetype"

        const val check = "check"
        const val vote = "vote"
        const val unvote = "unvote"
        const val transfer = "transfer"
        const val transactions = "transactions"
        const val delegates = "delegates"
        //   private var API_ENDPOINT = "http://localhost:5171/"//"http://45.32.185.200/api"
        //   val NETWORK = Network.TESTNET

        private var API_ENDPOINT = "https://sempy.online/api"


        val NETWORK = Network.MAINNET
        //Network.TESTNET
        //  val firebase = FirebaseDatabase.getInstance()
        //   val firebaseReference = firebase.reference

    }


    override fun onHandleIntent(intent: Intent?) {

        val typ = intent?.getStringExtra(TYP) ?: "fehler"

        when (typ) {
            check -> getBalance(intent)
            transfer -> doTransfer(intent)
            transactions -> loadTransactions(intent)
            delegates -> loadDelegates(intent)
            "fehler" -> Toast.makeText(this, "Irgendwas lief schief", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadDelegates(intent: Intent?) {
        val voteType = intent?.getStringExtra(VOTETYPE)

        Log.i("DELEGATE", "type: $voteType")

        val client = OkHttpClient()

        val request = Request.Builder()
                .url("$API_ENDPOINT/delegates")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")

                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("TRX", call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                val delegates = Gson().fromJson(res, Delegates::class.java)


            }
        })
    }

    //This only works for addresses given
    private fun loadTransactions(intent: Intent?) {
        val address = intent?.getStringExtra(ADDRESS)

        Log.i("TRX", "address: $address")

        val client = OkHttpClient()

        val request = Request.Builder()
                .url("$API_ENDPOINT/account?address=${address}")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")

                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("TRX", call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                val account = Gson().fromJson(res, CheckBalance::class.java)
                var startVal = 0
                var endVal = account.result.transactionCount
                if (account.result.transactionCount > 20) {
                    startVal = account.result.transactionCount - 20
                }

                val transactionRequest = Request.Builder()
                        .url("$API_ENDPOINT/account/transactions?address=$address&start=$startVal&end=$endVal")
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build()

                client.newCall(transactionRequest).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.i("TRX", call.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val innerResult = response.body()?.string()
                        Log.i("TRX", "result-> $innerResult")
                        val notificationIntent = Intent(NOTIFICATION_TRANSACTION)
                        notificationIntent.putExtra(TYP, transactions)
                        notificationIntent.putExtra(RESULT, Activity.RESULT_OK)
                        notificationIntent.putExtra(JSON, innerResult)
                        sendBroadcast(notificationIntent)
                    }
                })
            }
        })


    }


    private fun doTransfer(intent: Intent?) {
        val transactionRaw = intent?.getStringExtra(TRANSACTION_RAW)
        transactionRaw?.let {

            val client = OkHttpClient()

            Log.i("RAW", transactionRaw)

            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("raw", transactionRaw)
                    .build()
            Log.i("SEND", "$API_ENDPOINT/transaction/raw?raw=${transactionRaw}")
            val request = Request.Builder()
                    .url("$API_ENDPOINT/transaction/raw?raw=${transactionRaw}")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .post(requestBody)
                    .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("TRX", call.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val res = response.body()?.string()

                    val responseIntent = Intent(NOTIFICATION_TRANSFER)
                    responseIntent.putExtra(TYP, transfer)
                    responseIntent.putExtra(RESULT, Activity.RESULT_OK)
                    responseIntent.putExtra(JSON, res)
                    sendBroadcast(responseIntent)
                }
            })
        }
    }


    fun getBalance(intent: Intent?) {
        val address = intent?.getStringExtra(ADDRESS)

        val client = OkHttpClient()
        val request = Request.Builder()
                .url("$API_ENDPOINT/account?address=$address")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")

                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("TRX", call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()

                val notificationIntent = Intent(NOTIFICATION)
                notificationIntent.putExtra(TYP, check)
                notificationIntent.putExtra(RESULT, Activity.RESULT_OK)
                notificationIntent.putExtra(JSON, res)
                sendBroadcast(notificationIntent)
            }
        })

    }


}
