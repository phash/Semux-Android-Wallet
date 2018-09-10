package de.phash.manuel.asw.semux.key

import de.phash.semux.Key
import net.i2p.crypto.eddsa.EdDSAEngine

/**
 * Verifies a signature.
 *
 * @param message
 * message
 * @param signature
 * signature
 * @return True if the signature is valid, otherwise false
 */
fun verify(message: ByteArray?, signature: Key.Signature?): Boolean {
    if (message != null && signature != null) { // avoid null pointer exception
        try {

            val engine = EdDSAEngine()
            engine.initVerify(PublicKeyCache.computeIfAbsent(signature.publicKey))

            return engine.verifyOneShot(message, signature.s)

        } catch (e: Exception) {
            // do nothing
        }

    }

    return false
}