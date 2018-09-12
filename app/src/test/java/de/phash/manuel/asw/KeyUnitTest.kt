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

package de.phash.manuel.asw

import de.phash.manuel.asw.semux.key.*
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
        println("tx to string: " + transaction.toString())

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

        val fromEncoded = Transaction.fromEncoded(Hex.decode0x(transactionRaw))
        val signed = fromEncoded.sign(Key())

        println("valid tx: ${signed.validate(Network.MAINNET)}")
        val client = OkHttpClient()

        val signedTxRaw = Hex.encode0x(signed.toBytes())
        println("Encoded: " + Hex.encode0x(signed.encoded))
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
        val network = Network.MAINNET
        val transaction = TransactionBuilder(account)
                .withFrom(Hex.encode0x(account.toAddress()))
                .withFee(fee.nano.toString())
                .withValue(fee.nano.toString())
                .withData(Hex.encode0x(Bytes.of("data")))
                .withNonce("1")
                .withType(TransactionType.TRANSFER)
                .withNetwork(network.name)
                .withTimestamp(System.currentTimeMillis().toString())
                .withTo(Hex.encode(addressReceiver))
                .buildSigned()

        println(transaction.toString())
        println("validTX unsigned: ${transaction.validate(network)}")
        println("raw: ${Hex.encode0x(transaction.encoded)}")
        println("tb: ${Hex.encode0x(transaction.toBytes())}")
        val endoded = Hex.encode0x(transaction.encoded)

        val tx = Transaction.fromEncoded(Hex.decode0x(endoded)).sign(account)
        println("raw: ${Hex.encode0x(tx.toBytes())}")
        println("validTX signed: ${tx.validate(network)}")
        assertTrue("Transaktion invalid", transaction.validate(network))
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
        val network = Network.MAINNET
        val transaction = TransactionBuilder(account)
                .withNetwork(network.name)
                .withFrom(Hex.encode0x(account.toAddress()))
                .withFee(fee.nano.toString())
                .withValue(fee.nano.toString())
                .withData(Hex.encode0x(Bytes.of("data")))
                .withNonce("1")
                .withType(TransactionType.TRANSFER)
                .withTimestamp(System.currentTimeMillis().toString())
                .withTo(Hex.encode(addressReceiver))
                .buildUnsigned()



        println("raw: ${Hex.encode0x(transaction.encoded)}")

        val endoded = Hex.encode0x(transaction.encoded)

        val tx = Transaction.fromEncoded(Hex.decode0x(endoded)).sign(account)


        println("raw: ${Hex.encode0x(tx.toBytes())}")
        println("validTX signed: ${tx.validate(network)}")
        assertTrue("Transaktion invalid", tx.validate(network))

    }
    // "0x00031409c5f2794d69717d538bfcc150644f7685945cfa00000002540be40000000000004c4b40000000000000000100000165c4f4f54700"

    @Test
    fun signRawtest() {
        val raw = "0x00031409c5f2794d69717d538bfcc150644f7685945cfa00000002540be40000000000004c4b40000000000000000100000165c4f4f54700"
        val key = Key()

        val tx = Transaction.fromEncoded(Hex.decode0x(raw))
        val sig = key.sign(tx.hash)
        println(sig.equals(tx.sign(key).signature))
        println(Hex.encode0x(tx.toBytes()))
    }


}
