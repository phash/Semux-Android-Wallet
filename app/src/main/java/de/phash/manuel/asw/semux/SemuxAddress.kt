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

package de.phash.manuel.asw.semux

import android.content.ContentValues


data class SemuxAddress(val id: Int?,
                        val address: String,
                        val privateKey: String?,
                        val salt: String?,
                        val iv: String?,
                        val network: String?
) {
    companion object {
        val COLUMN_ID = "id"
        val COLUMN_ADDRESS = "address"
        val COLUMN_PRIVATEKEY = "privatekey"
        val COLUMN_IV = "iv"
        val COLUMN_SALT = "salt"
        val COLUMN_NETWORK = "network"
    }

    fun toContentValues(): ContentValues {
        return ContentValues(6).apply {
            put(COLUMN_ID, id)
            put(COLUMN_ADDRESS, address)
            put(COLUMN_PRIVATEKEY, privateKey)
            put(COLUMN_SALT, salt)
            put(COLUMN_IV, iv)
            put(COLUMN_NETWORK, network)
        }
    }
}

