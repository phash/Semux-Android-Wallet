package de.phash.manuel.asw.semux

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.key.Network
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal


class APIService : IntentService("SemuxService") {

    companion object {
        val SEMUXMULTIPLICATOR = BigDecimal("1000000000")
        const val TYP = "type"
        const val ADDRESS = "address"
        const val RESULT = "result"
        const val JSON = "json"
        const val NOTIFICATION = "de.phash.manuel.asw.semux"
        const val TRANSACTION_RAW = "transactionraw"

        const val check = "check"
        const val vote = "vote"
        const val unvote = "unvote"
        const val transfer = "transfer"
        const val transactions = "transactions"
        //   private var API_ENDPOINT = "http://localhost:5171/"//"http://45.32.185.200/api"
        //   val NETWORK = Network.TESTNET

        private var API_ENDPOINT = "http://45.32.185.200/api"
        val NETWORK = Network.MAINNET
        //Network.TESTNET

    }


    override fun onHandleIntent(intent: Intent?) {

        val typ = intent?.getStringExtra(TYP) ?: "fehler"

        when (typ) {
            check -> getBalance(intent)
            transfer -> doTransfer(intent)
            transactions -> loadTransactions(intent)
            "fehler" -> Toast.makeText(this, "Irgendwas lief schief", Toast.LENGTH_SHORT).show()
        }

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
                println(call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                val account = Gson().fromJson(res, CheckBalance::class.java)
                var startVal = 0
                var endVal = 20
                if (account.result.transactionCount < 20) {
                    endVal = account.result.transactionCount
                } else if (account.result.transactionCount > 20) {
                    startVal = account.result.transactionCount
                    endVal = account.result.transactionCount - 20
                }

                val transactionRequest = Request.Builder()
                        .url("$API_ENDPOINT/account/transactions?address=$address&start=$startVal&end=$endVal")
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build()

                client.newCall(transactionRequest).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println(call.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val innerResult = response.body()?.string()
                        Log.i("TRX", "result-> $innerResult")
                        val notificationIntent = Intent(NOTIFICATION)
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
                    println(call.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val res = response.body()?.string()

                    val responseIntent = Intent(NOTIFICATION)
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
                println(call.toString())
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

/*    fun getApiEndPoint() {


        val query = ParseQuery.getQuery<ParseObject>("settings")
        query.whereEqualTo("name", "base").whereEqualTo("network", "testnet")
        query.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0) {
                    API_ENDPOINT = objects.get(0).getString("endpoint")
                    NETWORK = if(objects.get(0).getString("network").equals("mainnet"))Network.MAINNET else
                        Network.TESTNET
                } else {
                    val defaultEndpoint = ParseObject("endpoints")
                    defaultEndpoint.put("name", "base")
                    defaultEndpoint.put("endpoint", "http://45.32.185.200/api")
                    defaultEndpoint.put("network", "mainnet")
                    defaultEndpoint.saveInBackground()
                    API_ENDPOINT = "http://45.32.185.200/api"
                    NETWORK = Network.MAINNET
                    val testEndpoint = ParseObject("endpoints")
                    testEndpoint.put("name", "base")
                    testEndpoint.put("endpoint", "http://localhost/api")
                    testEndpoint.put("network", "testnet")
                    testEndpoint.saveInBackground()
                }
            }
        }

    }
*/
}
