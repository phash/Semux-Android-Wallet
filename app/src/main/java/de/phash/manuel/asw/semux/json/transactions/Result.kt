package de.phash.manuel.asw.semux.json.transactions

data class Result(
        val data: String,
        val fee: String,
        val from: String,
        val gas: String,
        val gasPrice: String,
        val hash: String,
        val nonce: String,
        val timestamp: String,
        val to: String,
        val type: String,
        val value: String
)