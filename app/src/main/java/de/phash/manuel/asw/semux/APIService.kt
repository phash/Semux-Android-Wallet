package de.phash.manuel.asw.semux

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.os.AsyncTask
import okhttp3.*
import java.io.IOException
import java.net.URL

class APIService : IntentService("SemuxService") {

    companion object {
    val TYP = "type"
    val ADDRESS = "address"
        val RESULT = "result"
        val JSON = "json"
    val NOTIFICATION = "de.phash.manuel.asw.semux"

    val check = "check"

    }


    override fun onHandleIntent(intent: Intent?) {

        val typ = intent?.getStringExtra(TYP)


        when (typ) {
            check -> getBalance(intent)
        }

    }

    internal inner class JsonTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg strings: String): String {
            return URL("http://45.32.185.200/api/account?address=${strings[0]}").readText()
        }
    }


    fun getBalance(intent: Intent?) {
        val address = intent?.getStringExtra(ADDRESS)
        var task = JsonTask()
        var result = task.execute(address)

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
                intent.putExtra(RESULT, Activity.RESULT_OK)
                intent.putExtra(JSON, res)
                sendBroadcast(intent)
            }
        })

    }
}
