package de.phash.manuel.asw.semux.json.transactions

data class Result(
        val data: String,
        val fee: String,
        val from: String,
        val gas: String,
        val gasPrice: String,
        val hash: String,
        val nonce: Int,
        val timestamp: Long,
        val to: String,
        val type: String,
        val value: String
)