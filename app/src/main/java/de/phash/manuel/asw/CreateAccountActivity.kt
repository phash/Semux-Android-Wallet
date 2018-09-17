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

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.key.Key
import de.phash.manuel.asw.util.EnCryptor
import kotlinx.android.synthetic.main.activity_create_account.*
import org.bouncycastle.util.encoders.Hex

class CreateAccountActivity : AppCompatActivity() {

    var key = Key()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        updateViews()
        save()
    }

    fun updateViews() {
        privateKey.text = Hex.toHexString(key.privateKey)
        publicKey.text = Hex.toHexString(key.publicKey)
        createdaddress.text = key.toAddressString()
    }

    fun save() {

        val encryptorp = EnCryptor()
        val encryptors = EnCryptor()
        val encryptedPrivK = encryptors.encryptText(key.toAddressString() + "s", de.phash.manuel.asw.semux.key.Hex.encode0x(key.privateKey))
        val encryptedPublK = encryptorp.encryptText(key.toAddressString() + "p", de.phash.manuel.asw.semux.key.Hex.encode0x(key.publicKey))

        val values = ContentValues()
        values.put("address", key.toAddressString())
        values.put("publickey", Hex.toHexString(encryptedPublK))
        values.put("privatekey", Hex.toHexString(encryptedPrivK))
        values.put("ivs", Hex.toHexString(encryptors.iv))
        values.put("ivp", Hex.toHexString(encryptorp.iv))

        database.use { insert(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME, null, values) }
        Toast.makeText(this, "new account created! Save your private key!", Toast.LENGTH_LONG)
        //balanceActivity(this)

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
