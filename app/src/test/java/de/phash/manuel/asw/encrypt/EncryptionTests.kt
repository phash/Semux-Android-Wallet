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

package de.phash.manuel.asw.encrypt

import de.phash.manuel.asw.semux.key.Bytes
import de.phash.manuel.asw.semux.key.Hex
import de.phash.manuel.asw.semux.key.Key
import de.phash.manuel.asw.util.createAccount
import de.phash.manuel.asw.util.decryptAccount
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class EncryptionTests {


    @Test
    fun keyTest() {
        var keyString = "302e020100300506032b6570042204203d423ee2f60c8b9a6a78beea9282686e6c300e4cbae154ac4c81eae1801d087a"

        var key = Key(Hex.decode0x(keyString))
        println("key.toAddressString: " + key.toAddressString())
        println("key.toAddress: " + key.toAddress())
        println("key.toAddressString0x: " + key.toAddressString())
        key.sign(ByteArray(1))
    }

    @Test
    fun encrypt() {
        val pass = "myPassword"
        val key = Key()
        val semuxAddress = createAccount(key, pass)
        var values = semuxAddress.toContentValues()
        Assertions.assertEquals(key.toAddressString(), semuxAddress.address)

    }


    @Test
    fun encryptAndDecrypt() {

        val pass = "1234567890123456"
        val key = Key()
        val semuxAddress = createAccount(key, pass)

        Assertions.assertEquals(key.toAddressString(), semuxAddress.address)
        Assertions.assertNotEquals(Bytes.toString(key.privateKey), semuxAddress.privateKey)

        println("ivs " + semuxAddress.iv)

        val decryptedAddress = decryptAccount(semuxAddress, pass)

        Assertions.assertEquals(key.toAddressString(), decryptedAddress.address)
        Assertions.assertEquals(Hex.encode0x(key.privateKey), decryptedAddress.privateKey)

    }


}
