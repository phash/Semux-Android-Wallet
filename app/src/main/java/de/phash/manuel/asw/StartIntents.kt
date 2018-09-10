package de.phash.manuel.asw

import android.content.Context
import android.content.Intent

fun balanceActivity(context: Context) {
    val intent = Intent(context, BalancesActivity::class.java)
    context.startActivity(intent)
}

fun createActivity(context: Context) {
    val intent = Intent(context, CreateAccountActivity::class.java)
    context.startActivity(intent)
}