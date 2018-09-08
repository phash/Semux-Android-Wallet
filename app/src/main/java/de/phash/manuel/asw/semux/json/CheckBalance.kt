package de.phash.manuel.asw.semux.json

data class CheckBalance(
        val message: String,
        val result: Result,
        val success: Boolean
)