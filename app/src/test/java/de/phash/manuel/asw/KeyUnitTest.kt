package de.phash.manuel.asw

import de.phash.manuel.asw.semux.key.*
import de.phash.semux.Key
import okhttp3.*
import org.junit.Assert.assertTrue
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
        val amount = Amount.Unit.MILLI_SEM.of(1000)
        val fee = Amount.Unit.MILLI_SEM.of(5)
        val addressReceiver = "0x2e2fcd3a04771c15837a6ac6ea7e7675a82c83d8"
        val receiver = Hex.decode0x(addressReceiver)
        val nonce = 1L
        var transaction = Transaction(Network.MAINNET, TransactionType.TRANSFER, receiver, amount, fee, nonce, System.currentTimeMillis(), Bytes.of("data"))

        val key = Key()

        transaction.sign(key)
        println(transaction.toString())

        println("valid tx: ${transaction.validate(Network.MAINNET)}")

        val enc = Transaction.fromEncoded(transaction.encoded)

        println(enc.toString())
    }

    @Test
    fun TransactionTestSend() {
        val amount = Amount.Unit.MILLI_SEM.of(1000)
        val fee = Amount.Unit.MILLI_SEM.of(5)
        val addressReceiver = "0x2e2fcd3a04771c15837a6ac6ea7e7675a82c83d8"
        val receiver = Hex.decode0x(addressReceiver)
        val nonce = 0L
        var transaction = Transaction(Network.MAINNET, TransactionType.TRANSFER, receiver, amount, fee, nonce, System.currentTimeMillis(), Bytes.of("data"))

        println("tx to String " + transaction.toString())

        val transactionRaw = Hex.encode0x(transaction.encoded)
        println("valid tx: ${transaction.validate(Network.MAINNET)}")

        val fromEncoded = Transaction.fromEncoded(Hex.decode0x(transactionRaw))
        val signed = fromEncoded.sign(Key())

        val client = OkHttpClient()

        val signedTxRaw = Hex.encode0x(signed.toBytes())
        println("Encoded: " + Hex.encode0x(transaction.encoded))
        println("transactionRaw: $transactionRaw")
        println("signedTxRaw: $signedTxRaw")

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

    @Test
    fun transactionBuilderTest() {

        val account = Key()
        val amount = Amount.Unit.MILLI_SEM.of(1000)
        val fee = Amount.Unit.MILLI_SEM.of(5)
        val to = Key()
        val addressReceiver = to.toAddress()
        //   val receiver = Hex.decode0x(addressReceiver)
        val nonce = 1L

        val transaction = TransactionBuilder(account)
                .withFrom(Hex.encode0x(account.toAddress()))
                .withFee(fee.nano.toString())
                .withValue(fee.nano.toString())
                .withData(Hex.encode0x(Bytes.of("data")))
                .withNonce("1")
                .withType(TransactionType.TRANSFER)
                .withNetwork(Network.MAINNET)
                .withTimestamp(System.currentTimeMillis().toString())
                .withTo(Hex.encode(addressReceiver))
                .buildSigned()

        println(transaction.toString())
        println("validTX unsigned: ${transaction.validate(Network.MAINNET)}")
        println("raw: ${Hex.encode0x(transaction.encoded)}")
        println("tb: ${Hex.encode0x(transaction.toBytes())}")
        val endoded = Hex.encode0x(transaction.encoded)

        val tx = Transaction.fromEncoded(Hex.decode0x(endoded)).sign(account)
        println("raw: ${Hex.encode0x(tx.toBytes())}")
        println("validTX signed: ${tx.validate(Network.MAINNET)}")
        assertTrue("Transaktion invalid", transaction.validate(Network.MAINNET))
    }

    @Test
    fun transactionBuilderUnsignedTest() {

        val account = Key()
        val amount = Amount.Unit.MILLI_SEM.of(1000)
        val fee = Amount.Unit.MILLI_SEM.of(5)
        val to = Key()
        val addressReceiver = to.toAddress()
        //   val receiver = Hex.decode0x(addressReceiver)
        val nonce = 1L

        val transaction = TransactionBuilder(account)
                .withFrom(Hex.encode0x(account.toAddress()))
                .withFee(fee.nano.toString())
                .withValue(fee.nano.toString())
                .withData(Hex.encode0x(Bytes.of("data")))
                .withNonce("1")
                .withType(TransactionType.TRANSFER)
                .withNetwork(Network.MAINNET)
                .withTimestamp(System.currentTimeMillis().toString())
                .withTo(Hex.encode(addressReceiver))
                .buildUnsigned()

        println("validTX unsigned: ${transaction.validate(Network.MAINNET)}")
        println("raw: ${Hex.encode0x(transaction.encoded)}")

        val endoded = Hex.encode0x(transaction.encoded)

        val tx = Transaction.fromEncoded(Hex.decode0x(endoded)).sign(account)

        println("raw: ${Hex.encode0x(tx.toBytes())}")
        println("validTX signed: ${tx.validate(Network.MAINNET)}")
        assertTrue("Transaktion invalid", tx.validate(Network.MAINNET))
    }
}
