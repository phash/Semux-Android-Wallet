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

package de.phash.manuel.asw.main

import com.google.gson.Gson
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.key.Key
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal


fun main() {
    var key = Key()
    val address = key.toAddressString()
    println("Call $address")
    val client = OkHttpClient()
    val request = Request.Builder()
            .url("${APIService.API_ENDPOINT}/account?address=$address")
            .addHeader("content-type", "application/json")
            .addHeader("cache-control", "no-cache")

            .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("error " + e.localizedMessage)
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            val res = response.body?.string()

            var balance = Gson().fromJson(res, CheckBalance::class.java)
            var avail = BigDecimal(balance.result.available)
            if (avail.compareTo(BigDecimal.ZERO) > 0)
                println("HIT")
            else (print("DANEBEN"))
        }
    })
}