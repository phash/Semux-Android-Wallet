/**
 * Copyright (c) 2017-2018 The Semux Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package de.phash.manuel.asw.semux.key;


public enum Capability {

    /**
     * A mandatory capability for all clients.
     */
    SEMUX,

    /**
     * This client supports the CORE protocol.
     */
    CORE,

    /**
     * This client supports the LIGHT protocol.
     */
    LIGHT,

    /**
     * The client supports FAST_SYNC protocol.
     */
    FAST_SYNC;

    public static Capability of(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }

}
