package de.phash.manuel.asw.semux.json.transactions

data class TransactionsResult(
        val message: String,
        val result: List<Result>,
        val success: Boolean
)