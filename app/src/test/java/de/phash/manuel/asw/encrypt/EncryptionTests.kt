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

import de.phash.manuel.asw.semux.key.Hex
import de.phash.manuel.asw.semux.key.Key
import org.junit.jupiter.api.Test


class EncryptionTests {


    @Test
    fun testEncryption() {
        val encrypted = "9d543d36b9b78e88ac31188ec3d3c365dad772eb4add9a2031b485ac7250457441ee2b78f58947700c239b44f45bf4b99ddc6f0c345209dcff85d1f413cddc91cda70898d34a2864b6bded50c7016b2998a281651025cd0f1a7ffe4d0dac1db4fe410046db2bdaab7b8200b83e933e22f17a"
        val key = Key(Hex.decode0x(encrypted))
        println("der Key: ${key.toAddressString()} - ${Hex.encode(key.privateKey)}")
    }
}
