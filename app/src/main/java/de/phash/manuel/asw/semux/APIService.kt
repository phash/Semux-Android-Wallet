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

//import com.google.firebase.database.FirebaseDatabase
import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.errorActivity
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.key.Amount
import de.phash.manuel.asw.semux.key.Network
import de.phash.manuel.asw.util.getAddresses
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap


class APIService : IntentService("SemuxService") {

    companion object {
        fun changeNetwork(network: Network){
            Log.i("SETTINGS", "change network to ${network.label()}")
            NETWORK = network
            when (network){
                Network.MAINNET -> API_ENDPOINT = API_ENDPOINT_MAINNET
                Network.TESTNET -> API_ENDPOINT = API_ENDPOINT_TESTNET
            }
        }

        const val FORCE = "force"

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
        const val NOTIFICATION_DELEGATES = "de.phash.manuel.asw.semux.delegates"
        const val NOTIFICATION_ACCOUNTVOTES = "de.phash.manuel.asw.semux.accountvotes"
        const val TRANSACTION_RAW = "transactionraw"
        const val VOTETYPE = "votetype"

        const val check = "check"
        const val vote = "vote"
        const val unvote = "unvote"
        const val transfer = "transfer"
        const val transactions = "transactions"
        const val delegates = "delegates"
        const val accountvotes = "accountvotes"
        const val checkall = "checkall"
        //   private var API_ENDPOINT = "http://localhost:5171/"//"http://45.32.185.200/api"
        //   val NETWORK = Network.TESTNET

        val API_ENDPOINT_MAINNET = "https://api.semux.online/v2.3.0"
        val API_ENDPOINT_TESTNET = "https://api.testnet.semux.online/v2.3.0"
        var API_ENDPOINT = API_ENDPOINT_MAINNET

        var NETWORK = Network.MAINNET

        var lastChecked = HashMap<String, Long>()
        var cachedAccounts = HashMap<String, String>()

    }


    override fun onHandleIntent(intent: Intent?) {

        val typ = intent?.getStringExtra(TYP) ?: "fehler"
        Log.i("HANDLEINTENT", "handling $typ")

        when (typ) {
            check -> getBalance(intent)
            transfer -> doTransfer(intent)
            transactions -> loadTransactions(intent)
            delegates -> loadDelegates(intent)
            accountvotes -> getVotesForAccount(intent)
            checkall -> checkAll(intent)

            "fehler" -> Toast.makeText(this, "Irgendwas lief schief", Toast.LENGTH_SHORT).show()
        }

    }



    private fun checkAll(intent: Intent?) {
        val addresses = getAddresses(database)
        if (intent?.getBooleanExtra(FORCE, false) ?: false) {

            resetCache()
        }
        addresses.forEach {
            checkAddressCached(it.address);
        }

    }

    private fun loadDelegates(intent: Intent?) {

        Log.i("DELEGATES", "delegates loading")
        getVotesForAccount(intent)
        val client = OkHttpClient()

        val request = Request.Builder()
                .url("$API_ENDPOINT/delegates")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")

                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("DELEGATES", call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                Log.i("DELEGATES", res)

                val notificationIntent = Intent(NOTIFICATION_DELEGATES)
                notificationIntent.putExtra(TYP, delegates)
                notificationIntent.putExtra(RESULT, Activity.RESULT_OK)
                notificationIntent.putExtra(JSON, res)
                notificationIntent.putExtra("address", intent?.getStringExtra("address"))
                sendBroadcast(notificationIntent)

            }
        })
    }


    //This only works for addresses given
    private fun loadTransactions(intent: Intent?) {
        try {

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
                    var endVal = 20/* account.result.transactionCount
                    if (account.result.transactionCount > 20) {
                        startVal = account.result.transactionCount - 20
                    }*/

                    val transactionRequest = Request.Builder()
                            .url("$API_ENDPOINT/account/transactions?address=$address&from=$startVal&to=$endVal")
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
        } catch (e: java.lang.Exception) {
            Log.e("API", e.message)
        }

    }


    private fun doTransfer(intent: Intent?) {
        val transactionRaw = intent?.getStringExtra(TRANSACTION_RAW)
        transactionRaw?.let {
            resetCache()
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

    public fun resetCache() {
        Log.i("CACHE", "resetCache")
        lastChecked.clear()
    }

    fun getVotesForAccount(intent: Intent?) {
        try {
            val address = intent?.getStringExtra(ADDRESS)
            Log.i("OWNVOTES", "getVotes for $address")

            val client = OkHttpClient()
            val request = Request.Builder()
                    .url("$API_ENDPOINT/account/votes?address=$address")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")

                    .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("OWNVOTES", call.toString())

                }

                override fun onResponse(call: Call, response: Response) {
                    val res = response.body()?.string()
                    Log.i("OWNVOTES", res)
                    val notificationIntent = Intent(NOTIFICATION_ACCOUNTVOTES)
                    notificationIntent.putExtra(TYP, accountvotes)
                    notificationIntent.putExtra(RESULT, Activity.RESULT_OK)
                    notificationIntent.putExtra(JSON, res)
                    sendBroadcast(notificationIntent)
                }
            })
        } catch (e: Exception) {
            Log.e("API", e.message)
        }

    }


    fun getBalance(intent: Intent?) {
        try {

            val address = intent?.getStringExtra(ADDRESS)
            address?.let {
                checkAddressCached(it)
            }
        } catch (e: Exception) {
            errorActivity(this@APIService, "API not reachable")
        }

    }

    private fun checkAddressCached(address: String) {
        Log.i("CHECKADDRESSCACHED", "checking if cache is to be used for " + address + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                ?: 0L)) + ")")

        if (cacheNeedsUpdate(address)) {
            Log.i("CHECKADDRESSCACHED", "update account  " + address + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                    ?: 0L)) + ")")
            checkAddress(address)

        } else {
            Log.i("CHECKADDRESSCACHED", "checking cached values for" + address + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                    ?: 0L)) + ")")
            var cached = cachedAccounts.get(address)

            if (cached != null) {
                Log.i("CHECKADDRESSCACHED", "using cached values for" + address + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                        ?: 0L)) + ")")
                sendNotificationIntent(cached)
            } else {
                Log.i("CHECKADDRESSCACHED", "update cached values for" + address + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                        ?: 0L)) + ")")
                checkAddress(address)
            }
        }
    }

    private fun cacheNeedsUpdate(address: String): Boolean {
        Log.i("CACHEUPDATE", "cache needs update for " + address)
        return lastChecked.get(address) ?: 0L + 30000L < Calendar.getInstance().timeInMillis
    }

    private fun checkAddress(address: String) {
        Log.i("CHECKADDRESS", address + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                ?: 0L)) + ")")
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("$API_ENDPOINT/account?address=$address")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")

                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("CHECKADDRESS", call.toString())
                errorActivity(this@APIService, "API not reachable")
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                if (res != null) {

                    cachedAccounts.put(address, res);
                    Log.i("CHECKADDRESS", "updated cached accounts" + " lastChecked: " + lastChecked + " (" + (Calendar.getInstance().timeInMillis - (lastChecked.get(address)
                            ?: 0L)) + ")")
                    lastChecked.put(address, Calendar.getInstance().timeInMillis)
                    sendNotificationIntent(res)
                }
            }
        })


    }

    private fun sendNotificationIntent(res: String?) {
        val notificationIntent = Intent(NOTIFICATION)
        notificationIntent.putExtra(TYP, check)
        notificationIntent.putExtra(RESULT, Activity.RESULT_OK)
        notificationIntent.putExtra(JSON, res)
        sendBroadcast(notificationIntent)
    }

}
