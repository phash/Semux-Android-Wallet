package de.phash.manuel.asw

import de.phash.manuel.asw.semux.key.*
import de.phash.semux.Key
import okhttp3.*
import org.junit.Test
import java.io.IOException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class KeyUnitTest {
    @Test
    fun keyTest() {
        var keyString = "302e020100300506032b6570042204203d423ee2f60c8b9a6a78beea9282686e6c300e4cbae154ac4c81eae1801d087a"

        var key = Key(Hex.decode0x(keyString))
        key.sign(ByteArray(1))
    }

    @Test
    fun TransactionTest() {
        val amount = Amount(1000)
        val fee = Amount(1000)
        val addressReceiver = "0x2e2fcd3a04771c15837a6ac6ea7e7675a82c83d8"
        val receiver = Hex.decode0x(addressReceiver)
        val nonce = 0L
        var transaction = Transaction(Network.MAINNET, TransactionType.TRANSFER, receiver, amount, fee, nonce, System.currentTimeMillis(), Bytes.EMPTY_BYTES)

        val key = Key()

        transaction.sign(key)
        println(transaction.toString())

        val enc = Transaction.fromEncoded(transaction.encoded)

        println(enc.toString())
    }

    @Test
    fun TransactionTestSend() {
        val amount = Amount(1000)
        val fee = Amount(1000)
        val addressReceiver = "0x2e2fcd3a04771c15837a6ac6ea7e7675a82c83d8"
        val receiver = Hex.decode0x(addressReceiver)
        val nonce = 0L
        var transaction = Transaction(Network.MAINNET, TransactionType.TRANSFER, receiver, amount, fee, nonce, System.currentTimeMillis(), Bytes.EMPTY_BYTES)

        val key = Key()

        transaction.sign(key)
        println(transaction.toString())

        val transactionRaw = Hex.encode0x(transaction.encoded)
        val client = OkHttpClient()

        println("Encoded: " + Hex.encode0x(transaction.encoded))


        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("raw", transactionRaw)
                .build()

        val request = Request.Builder()
                .url("http://45.32.185.200/api/transaction/raw?raw=${transactionRaw}")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .post(requestBody)
                .build()
        println("call")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(call.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string()
                println("RESULT: " + res)
            }
        })
    }
}
