package de.phash.manuel.asw.semux.json

data class Result(
        val address: String,
        val available: String,
        val locked: String,
        val nonce: String,
        val pendingTransactionCount: Int,
        val transactionCount: Int
)