package de.phash.manuel.asw.semux

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal

class APIService : IntentService("SemuxService") {

    companion object {
        val SEMUXMULTIPLICATOR = BigDecimal("1000000000")
        val TYP = "type"
        val ADDRESS = "address"
        val RESULT = "result"
        val JSON = "json"
        val NOTIFICATION = "de.phash.manuel.asw.semux"
        val TRANSACTION_RAW = "transactionraw"

        val check = "check"
        val transfer = "transfer"

    }


    override fun onHandleIntent(intent: Intent?) {

        val typ = intent?.getStringExtra(TYP) ?: "fehler"

        when (typ) {
            check -> getBalance(intent)
            transfer -> doTransfer(intent)
            "fehler" -> Toast.makeText(this, "Irgendwas lief schief", Toast.LENGTH_SHORT)
        }

    }


    private fun doTransfer(intent: Intent?) {
        val transactionRaw = intent?.getStringExtra(TRANSACTION_RAW)
        val client = OkHttpClient()

        Log.i("RAW", transactionRaw)


        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("raw", transactionRaw)
                .build()
        Log.i("SEND", "http://45.32.185.200/api/transaction/raw?raw=${transactionRaw}")
        val request = Request.Builder()
                .url("http://45.32.185.200/api/transaction/raw?raw=${transactionRaw}")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .post(requestBody)
                .build()

        //val request = Request.Builder()
        //.url("http://45.32.185.200/api/transaction/raw?raw=${transactionRaw}")
//                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()


                val intent = Intent(NOTIFICATION)
                intent.putExtra(TYP, transfer)
                intent.putExtra(RESULT, Activity.RESULT_OK)
                intent.putExtra(JSON, res)
                sendBroadcast(intent)
            }
        })
    }



    fun getBalance(intent: Intent?) {
        val address = intent?.getStringExtra(ADDRESS)

        val client = OkHttpClient()
        val request = Request.Builder()
                .url("http://45.32.185.200/api/account?address=${address}")
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
