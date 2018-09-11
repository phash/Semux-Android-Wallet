package de.phash.manuel.asw.semux.json.transactions

data class Result(
        val blockNumber: String,
        val data: String,
        val fee: String,
        val from: String,
        val hash: String,
        val nonce: String,
        val timestamp: String,
        val to: String,
        val type: String,
        val value: String
)