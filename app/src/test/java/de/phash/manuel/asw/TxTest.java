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

package de.phash.manuel.asw;

import junit.framework.Assert;

import org.junit.Test;

import de.phash.manuel.asw.semux.key.Amount;
import de.phash.manuel.asw.semux.key.Bytes;
import de.phash.manuel.asw.semux.key.Hex;
import de.phash.manuel.asw.semux.key.Key;
import de.phash.manuel.asw.semux.key.Network;
import de.phash.manuel.asw.semux.key.Transaction;
import de.phash.manuel.asw.semux.key.TransactionType;

import static de.phash.manuel.asw.semux.key.Amount.Unit.MILLI_SEM;

public class TxTest {
    private static Key key = new Key();
    private static Network network = Network.MAINNET;
    private static TransactionType type = TransactionType.TRANSFER;
    private static byte[] from = key.toAddress();
    private static byte[] to = new Key().toAddress();
    private static Amount value = MILLI_SEM.of(1);
    private static Amount fee = MILLI_SEM.of(1);

    @Test
    public void testTx() {
        long now = System.currentTimeMillis();
        long nonce = 0L;
        Transaction tx = new Transaction(network, type, to, value, fee, nonce, now, Bytes.EMPTY_BYTES).sign(key);

        System.out.println(Hex.encode0x(tx.toBytes()));
        Assert.assertTrue("invalid tx", tx.validate(network));

    }
}
