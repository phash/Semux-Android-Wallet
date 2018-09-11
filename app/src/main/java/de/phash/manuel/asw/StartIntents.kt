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
        R.id.createAccout -> createActivity(context)
        R.id.importPrivateKey -> importActivity(context)
    }
}

fun importActivity(context: Context) {
    val intent = Intent(context, ImportKeyActivity::class.java)
    context.startActivity(intent)
}
