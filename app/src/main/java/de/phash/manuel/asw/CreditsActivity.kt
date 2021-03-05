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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.key.Network
import org.jetbrains.anko.browse
import org.jetbrains.anko.email

class CreditsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        setTitle(if (APIService.NETWORK == Network.MAINNET)  R.string.semuxMain else R.string.semuxTest)

    }

    fun onGithubClick(view: View) {
        browse("https://github.com/phash/Semux-Android-Wallet")
    }

    fun onPhashClick(view: View) {
        browse("https://phash.de")
    }

    fun onPhashMailClick(view: View) {
        email("phash@phash.de", "hire the dev", "Hi Phash\n\nI need your help!\nplease answer for getting the details of the deal")
    }

    fun onSempyClick(view: View) {
        browse("https://sempy.info")
    }

    fun onSemuxClick(view: View) {
        browse("https://semux.org")
    }

    fun onDiscordClick(view: View) {
        browse("https://discord.gg/qQVckKZ")
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
