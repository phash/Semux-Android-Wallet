package de.phash.manuel.asw.integration.cmc.impl


import android.app.IntentService
import android.content.Intent
import android.util.Log
import de.phash.manuel.asw.semux.APIService
import okhttp3.*
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime

class CmCApiServiceImpl : IntentService("CmCApiServiceImpl") {

    companion object {

        lateinit var instance: CmCApiServiceImpl
            private set

        var lastPriceCheck: LocalDateTime? = null

        var lastPrice: Double = 0.0

        var conversionUnit = "USD"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun checkPrice(intent: Intent?) {
        Log.i("CMCSERVICE", "lastprice: $lastPrice")
        if (lastPrice == 0.0 ) {
            calculate(intent)
        }
        Log.i("CMCSERVICE", "check cache")
        lastPriceCheck?.let {

            if (it.plusMinutes(5L).isBefore(LocalDateTime.now())) {
                Log.i("CMCSERVICE", "renew cache - last checked $lastPriceCheck")
                calculate(intent)
            } else{

                Log.i("CMCSERVICE", "using cached values")
                sendAnswer(lastPrice)
            }
        } ?: run {
            Log.i("CMCSERVICE", "initial run")
            calculate(intent)}
    }

    fun calculate(intent: Intent?) {
        Log.i("CMCSERVICE", "calculate")
        val client = OkHttpClient()
        val request = Request.Builder()
                .addHeader("X-CMC_PRO_API_KEY", "")
                // .url("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=500&convert=USD")
                .url("https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=SEM&convert=$conversionUnit")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(call.toString())
            }

            override fun onResponse(call: Call, response: Response) {

                val res = response.body?.string()
                println(res)
                var contents = arrayOf<String>("", "SEM", conversionUnit)
                val json = JSONObject(res)
                if (json.isNull("data")) return
                json.getJSONObject("data").let {
                    if (StringUtils.isBlank(it.toString())) return
                    println(it.toString())
                    val currency = it.getJSONObject(contents[1].toUpperCase())
                    val quote = currency.getJSONObject("quote")
                    val myErg = quote.getJSONObject(contents[2].toUpperCase()).getDouble("price")
                    lastPrice = myErg
                    lastPriceCheck = LocalDateTime.now()
                    Log.i("CMCSERVICE", "current Price {$myErg}")
                    sendAnswer(myErg)
                }
            }
        })
    }

    private fun sendAnswer(myErg: Double) {
        val notificationIntent = Intent(APIService.NOTIFICATION_CURRENTPRICE)
        notificationIntent.putExtra(APIService.TYP, APIService.currentPrice)
        notificationIntent.putExtra(APIService.currentPrice, myErg)

        sendBroadcast(notificationIntent)
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.i("CMCSERVICE", "startService")
        val typ = intent?.getStringExtra(APIService.TYP) ?: "fehler"
        Log.i("CMCSERVICE", typ)
        when (typ) {
            APIService.getprice -> checkPrice(intent)
        }
    }
}
