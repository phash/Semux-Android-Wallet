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

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.util.createQRCode
import kotlinx.android.synthetic.main.activity_single_balance.*
import java.math.BigDecimal

class SingleBalanceActivity : AppCompatActivity() {
    var locked = ""
    var address = ""
    var available = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_balance)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        address = intent.getStringExtra("address")
        available = intent.getStringExtra("available")
        locked = intent.getStringExtra("locked")

        singleBalanceAddress.text = address
        singleBalanceAvailable.text = APIService.SEMUXFORMAT.format(BigDecimal(available).divide(APIService.SEMUXMULTIPLICATOR)) + " SEM"
        singleBalanceLocked.text = APIService.SEMUXFORMAT.format(BigDecimal(locked).divide(APIService.SEMUXMULTIPLICATOR)) + " SEM"
        createQR()

    }

    fun onImageClick(view: View) {
        val intent = Intent(this, QrViewActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    fun onSendClick(view: View) {
        val intent = Intent(this, SendActivity::class.java)

        intent.putExtra("address", address)
        intent.putExtra("available", available)
        intent.putExtra("locked", locked)

        startActivity(intent)
    }

    fun onVoteClick(view: View) {
        val intent = Intent(this, VoteActivity::class.java)
        intent.putExtra("address", address)
        intent.putExtra("available", available)
        intent.putExtra("locked", locked)
        startActivity(intent)
    }

    fun onTransactionsClick(view: View) {
        val intent = Intent(this, TransactionsActivity::class.java)
        intent.putExtra("address", address)
        startActivity(intent)
    }

    private fun createQR() {
        qrAddressImageView.setImageBitmap(createQRCode(address, 200))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        startNewActivity(item, this)
        return super.onOptionsItemSelected(item)
    }

}
