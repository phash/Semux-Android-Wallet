package de.phash.manuel.asw.semux.key;
/**
 * Copyright (c) 2017-2018 The Semux Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import org.bouncycastle.util.Arrays;

public class SemuxByteArray implements Comparable<SemuxByteArray> {
    private final byte[] data;
    private final int hash;

    public SemuxByteArray(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data can not be null");
        }
        this.data = data;
        this.hash = Arrays.hashCode(data);
    }

    public static SemuxByteArray of(byte[] data) {
        return new SemuxByteArray(data);
    }

    public int length() {
        return data.length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SemuxByteArray) && Arrays.areEqual(data, ((SemuxByteArray) other).data);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(SemuxByteArray o) {
        return Arrays.compareUnsigned(data, o.data);
    }

    @Override
    public String toString() {
        return Hex.encode(data);
    }

    public static class ByteArrayKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext context) {
            return new SemuxByteArray(Hex.decode0x(key));
        }
    }
}