package de.phash.manuel.asw.integration.cmc.impl


import android.app.IntentService
import android.content.Intent
import android.util.Log
import de.phash.manuel.asw.semux.APIService
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class CmCApiServiceImpl : IntentService("CmCApiServiceImpl") {

    fun calculate(intent: Intent?) {
        Log.i("CMCSERVICE", "calculate")
        val client = OkHttpClient()
        val request = Request.Builder()
                .addHeader("X-CMC_PRO_API_KEY", "")
                // .url("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=500&convert=USD")
                .url("https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=SEM&convert=USD")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                println(res)
                var contents = arrayOf<String>("", "SEM", "USD")
                val json = JSONObject(res)
                val data = json?.getJSONObject("data")
                println("-------------")
                println(data.toString())
                val currency = data.getJSONObject(contents[1].toUpperCase())
                val quote = currency.getJSONObject("quote")
                val myErg = quote.getJSONObject(contents[2].toUpperCase()).getDouble("price")

                Log.i("CMCSERVICE", "current Price {$myErg}")
                val notificationIntent = Intent(APIService.NOTIFICATION_CURRENTPRICE)
                notificationIntent.putExtra(APIService.TYP, APIService.currentPrice)
                notificationIntent.putExtra(APIService.currentPrice, myErg)

                sendBroadcast(notificationIntent)
            }
        })
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.i("CMCSERVICE", "startService")
        val typ = intent?.getStringExtra(APIService.TYP) ?: "fehler"
        Log.i("CMCSERVICE", typ)
        when (typ) {
            APIService.getprice -> calculate(intent)
        }
    }
}