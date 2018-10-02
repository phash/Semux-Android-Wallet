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

import android.content.Context
import android.content.Intent
import android.view.MenuItem

fun balanceActivity(context: Context) {
    val intent = Intent(context, BalancesActivity::class.java)
    context.startActivity(intent)
}

fun createActivity(context: Context) {
    val intent = Intent(context, CreateAccountActivity::class.java)
    context.startActivity(intent)
}

fun startNewActivity(item: MenuItem, context: Context) {

    when (item.itemId) {
        R.id.balancesMenu -> balanceActivity(context)
        R.id.settingsMenu -> settingsActivity(context)
        R.id.dashboardMenu -> dashboardActivity(context)
        R.id.creditMenu -> creditsActivity(context)
    }
}

fun singleAccountActivity(context: Context, address: String) {
    val intent = Intent(context, SingleBalanceActivity::class.java)
    intent.putExtra("address", address)
    context.startActivity(intent)
}

fun settingsActivity(context: Context) {
    val intent = Intent(context, SettingsActivity::class.java)
    context.startActivity(intent)
}

fun delegatesActivity(context: Context, address: String) {
    val intent = Intent(context, SelectDelegate::class.java)
    intent.putExtra("address", address)
    context.startActivity(intent)
}

fun dashboardActivity(context: Context) {
    val intent = Intent(context, DashBoardActivity::class.java)
    context.startActivity(intent)
}

fun creditsActivity(context: Context) {
    val intent = Intent(context, CreditsActivity::class.java)
    context.startActivity(intent)
}

fun manageActivity(context: Context) {
    val intent = Intent(context, ManageActivity::class.java)
    context.startActivity(intent)
}

fun importActivity(context: Context) {
    val intent = Intent(context, ImportKeyActivity::class.java)
    context.startActivity(intent)
}

fun setPasswordActivity(context: Context) {
    val intent = Intent(context, PasswordActivity::class.java)
    context.startActivity(intent)
}