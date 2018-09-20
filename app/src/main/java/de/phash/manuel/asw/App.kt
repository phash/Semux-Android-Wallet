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

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.util.firebase
import de.phash.manuel.asw.util.getAdresses
import java.util.*


class App : Application() {

    private var timer = Timer()

    override fun onCreate() {
        super.onCreate()
        timer = Timer()
        timer.scheduleAtFixedRate(UpdateBalTask(), 50, 30000)

        //startService(Intent(this.applicationContext, AlertReceiver::class.java))

    }

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private fun updateBalances() {

        val bundle = Bundle()
        firebase("8", "update balances", FirebaseAnalytics.getInstance(this))

        val adresses = getAdresses(database)

        updateBalanceList(adresses)
    }

    private fun updateBalanceList(adresses: List<SemuxAddress>) {

        adresses.forEach {
            updateAddress(it.address)
        }
    }

    private fun updateAddress(address: String) {

        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.ADDRESS, address)
        intent.putExtra(APIService.TYP,
                APIService.check)
        startService(intent)
    }

    inner class UpdateBalTask : TimerTask() {
        override fun run() {
            Log.i("TIMER", "timer runs")
            updateBalances()
        }
    }
}