package de.phash.manuel.asw.semux


data class SemuxAddress(val id: Int?,
                        val address: String,
                        val publicKey: String,
                        val privateKey: String
) {
    companion object {
        val COLUMN_ADDRESS = "address"
        val COLUMN_PUBLICKEY = "publickey"
        val COLUMN_PRIVATEKEY = "privatekey"
    }
}

