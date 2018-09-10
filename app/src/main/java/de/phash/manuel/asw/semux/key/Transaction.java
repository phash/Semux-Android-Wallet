/**
 * Copyright (c) 2017-2018 The Semux Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package de.phash.manuel.asw.semux.key;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

import static de.phash.manuel.asw.semux.key.VerifyKt.verify;


public class Transaction {

    private final byte networkId;

    private final TransactionType type;

    private final byte[] to;

    private final Amount value;

    private final Amount fee;

    private final long nonce;

    private final long timestamp;

    private final byte[] data;

    private final byte[] encoded;

    private final byte[] hash;

    private Key.Signature signature;

    /**
     * Create a new transaction.
     *
     * @param network
     * @param type
     * @param to
     * @param value
     * @param fee
     * @param nonce
     * @param timestamp
     * @param data
     */
    public Transaction(Network network, TransactionType type, byte[] to, Amount value, Amount fee, long nonce,
                       long timestamp, byte[] data) {
        this.networkId = network.id();
        this.type = type;
        this.to = to;
        this.value = value;
        this.fee = fee;
        this.nonce = nonce;
        this.timestamp = timestamp;
        this.data = data;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(networkId);
        enc.writeByte(type.toByte());
        enc.writeBytes(to);
        enc.writeAmount(value);
        enc.writeAmount(fee);
        enc.writeLong(nonce);
        enc.writeLong(timestamp);
        enc.writeBytes(data);
        this.encoded = enc.toBytes();
        this.hash = Hash.h256(encoded);
    }


    /**
     * Sign this transaction.
     *
     * @param key
     * @return
     */
    public Transaction sign(Key key) {
        this.signature = key.sign(this.hash);
        return this;
    }

    public static final int PUBLIC_KEY_LEN = 44;
    public static final int PRIVATE_KEY_LEN = 48;
    public static final int ADDRESS_LEN = 20;

    /**
     * <p>
     * Validate transaction format and signature. </>
     * <p>
     * <p>
     * NOTE: this method does not check transaction validity over the state. Use
     * </p>
     *
     * @param network
     * @return true if success, otherwise false
     */
    public boolean validate(Network network) {


        boolean resHash = hash != null && hash.length == Hash.HASH_LEN;
        boolean resNw = networkId == network.id();
        boolean resType = type != null;
        boolean resTo = to != null && to.length == ADDRESS_LEN;
        boolean resVal = value.gte0();
        boolean resFee = fee.gte0();
        boolean resNonce = nonce >= 0;
        boolean resTime = timestamp > 0;
        boolean resData = data != null;
        boolean resEncode = encoded != null;
        boolean resSinature = signature != null && !Arrays.equals(signature.getAddress(), Bytes.EMPTY_ADDRESS);

        boolean resEncHash = Arrays.equals(Hash.h256(encoded), hash);
        boolean resHashSig = verify(hash, signature);

        // The coinbase key is publicly available. People can use it for transactions.
        // It won't introduce any fundamental loss to the system but could potentially
        // cause confusion for block explorer, and thus are prohibited.
        boolean resTypeCoinbase = (type == TransactionType.COINBASE
                || (!Arrays.equals(signature.getAddress(), Constants.COINBASE_ADDRESS) &&
                !Arrays.equals(to, Constants.COINBASE_ADDRESS)));

        return (resHash && resNw && resTime && resTo && resType && resSinature && resEncHash && resEncode && resVal && resFee && resNonce && resData && resHashSig && resTypeCoinbase);

    }


    /**
     * Returns the transaction network id.
     *
     * @return
     */
    public byte getNetworkId() {
        return networkId;
    }

    /**
     * Returns the transaction hash.
     *
     * @return
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Returns the transaction type.
     *
     * @return
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Parses the from address from signature.
     *
     * @return an address if the signature is valid, otherwise null
     */
    public byte[] getFrom() {
        return (signature == null) ? null : signature.getAddress();
    }

    /**
     * Returns the recipient address.
     *
     * @return
     */
    public byte[] getTo() {
        return to;
    }

    /**
     * Returns the value.
     *
     * @return
     */
    public Amount getValue() {
        return value;
    }

    /**
     * Returns the transaction fee.
     *
     * @return
     */
    public Amount getFee() {
        return fee;
    }

    /**
     * Returns the nonce.
     *
     * @return
     */
    public long getNonce() {
        return nonce;
    }

    /**
     * Returns the timestamp.
     *
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the extra data.
     *
     * @return
     */
    public byte[] getData() {
        return data;
    }

    public byte[] getEncoded() {
        return encoded;
    }

    /**
     * Decodes an byte-encoded transaction that is not yet signed by a private key.
     *
     * @param encoded
     *            the bytes of encoded transaction
     * @return the decoded transaction
     */
    public static Transaction fromEncoded(byte[] encoded) {
        SimpleDecoder decoder = new SimpleDecoder(encoded);

        byte networkId = decoder.readByte();
        byte type = decoder.readByte();
        byte[] to = decoder.readBytes();
        Amount value = decoder.readAmount();
        Amount fee = decoder.readAmount();
        long nonce = decoder.readLong();
        long timestamp = decoder.readLong();
        byte[] data = decoder.readBytes();

        return new Transaction(Network.of(networkId), TransactionType.of(type), to, value, fee, nonce, timestamp, data);
    }

    /**
     * Returns the signature.
     *
     * @return
     */
    public Key.Signature getSignature() {
        return signature;
    }

    /**
     * Converts into a byte array.
     *
     * @return
     */
    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(hash);
        enc.writeBytes(encoded);
        enc.writeBytes(signature.toBytes());

        return enc.toBytes();
    }


    /**
     * Returns size of the transaction in bytes
     *
     * @return size in bytes
     */
    public int size() {
        return toBytes().length;
    }

    @Override
    public String toString() {
        return "Transaction [type=" + type + ", from=" + signature != null && getFrom() != null ? Hex.encode(getFrom()) : "NO FROM" + ", to=" + Hex.encode(to) + ", value="
                + value + ", fee=" + fee + ", nonce=" + nonce + ", timestamp=" + timestamp + ", data="
                + Hex.encode(data) + ", hash=" + Hex.encode(hash) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Transaction that = (Transaction) o;

        return new EqualsBuilder()
                .append(encoded, that.encoded)
                .append(hash, that.hash)
                .append(signature, that.signature)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(encoded)
                .append(hash)
                .append(signature)
                .toHashCode();
    }
}
