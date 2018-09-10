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
