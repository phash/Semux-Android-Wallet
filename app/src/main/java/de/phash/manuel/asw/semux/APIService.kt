package de.phash.manuel.asw.semux

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.parse.ParseObject
import com.parse.ParseQuery
import de.phash.manuel.asw.semux.json.CheckBalance
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal

private val API_ENDPOINT = getApiEndPoint() //"http://45.32.185.200/api"

fun getApiEndPoint(): String {
    var apiEndPoint = "heinz"
    val query = ParseQuery.getQuery<ParseObject>("settings")
    query.whereEqualTo("name", "base")
    query.findInBackground { objects, e ->
        if (e == null) {
            if (objects.size > 0) {
                apiEndPoint = objects.get(0).getString("endpoint")
            } else {
                val defaultEndpoint = ParseObject("endpoints")
                defaultEndpoint.put("name", "base")
                defaultEndpoint.put("endpoint", "http://45.32.185.200/api")
                defaultEndpoint.saveInBackground()
                apiEndPoint = "http://45.32.185.200/api"
            }
        }
    }
    return apiEndPoint

}

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

    }


    override fun onHandleIntent(intent: Intent?) {

        val typ = intent?.getStringExtra(TYP) ?: "fehler"

        when (typ) {
            check -> getBalance(intent)
            transfer -> doTransfer(intent)
            transactions -> loadTransactions(intent)
            "fehler" -> Toast.makeText(this, "Irgendwas lief schief", Toast.LENGTH_SHORT)
        }

    }

    //This only works for addresses given
    private fun loadTransactions(intent: Intent?) {
        var address = intent?.getStringExtra(ADDRESS)
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

                val request = Request.Builder()
                        .url("$API_ENDPOINT/account/transactions?address=${address}&start=${startVal}&end=${endVal}")
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println(call.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val res = response.body()?.string()

                        val intent = Intent(NOTIFICATION)
                        intent.putExtra(TYP, transactions)
                        intent.putExtra(RESULT, Activity.RESULT_OK)
                        intent.putExtra(JSON, res)
                        sendBroadcast(intent)
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

                val intent = Intent(NOTIFICATION)
                intent.putExtra(TYP, check)
                intent.putExtra(RESULT, Activity.RESULT_OK)
                intent.putExtra(JSON, res)
                sendBroadcast(intent)
            }
        })

    }
}
