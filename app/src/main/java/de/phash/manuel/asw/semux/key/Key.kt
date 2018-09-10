package de.phash.semux

import de.phash.manuel.asw.semux.key.Bytes
import de.phash.manuel.asw.semux.key.Hash
import de.phash.manuel.asw.semux.key.Hex
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.KeyPairGenerator
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


class Key {


    private val gen = KeyPairGenerator()
    var sk: EdDSAPrivateKey
    var pk: EdDSAPublicKey

    /**
     * Creates a random ED25519 key pair.
     */
    constructor() {
        val keypair = gen.generateKeyPair()
        sk = keypair.private as EdDSAPrivateKey
        pk = keypair.public as EdDSAPublicKey
    }

    /**
     * Creates an ED25519 key pair with a specified private key
     *
     * @param privateKey
     * the private key in "PKCS#8" format
     * @throws InvalidKeySpecException
     */
    @Throws(InvalidKeySpecException::class)
    constructor(privateKey: ByteArray) {
        this.sk = EdDSAPrivateKey(PKCS8EncodedKeySpec(privateKey))
        this.pk = EdDSAPublicKey(EdDSAPublicKeySpec(sk.a, sk.params))
    }

    /**
     * Creates an ED25519 key pair with the specified public and private keys.
     *
     * @param privateKey
     * the private key in "PKCS#8" format
     * @param publicKey
     * the public key in "X.509" format, for verification purpose only
     *
     * @throws InvalidKeySpecException
     */
    @Throws(InvalidKeySpecException::class)
    constructor(privateKey: ByteArray, publicKey: ByteArray) {
        this.sk = EdDSAPrivateKey(PKCS8EncodedKeySpec(privateKey))
        this.pk = EdDSAPublicKey(EdDSAPublicKeySpec(sk.a, sk.params))

        if (!Arrays.equals(getPublicKey(), publicKey)) {
            throw InvalidKeySpecException("Public key and private key do not match!")
        }
    }

    fun generate() {
        val gen = KeyPairGenerator()
        val keyPair = gen.generateKeyPair()

    }


    /**
     * Returns the private key, encoded in "PKCS#8".
     */
    fun getPrivateKey(): ByteArray {
        return sk.encoded
    }

    /**
     * Returns the public key, encoded in "X.509".
     *
     * @return
     */
    fun getPublicKey(): ByteArray {
        return pk.encoded
    }

    /**
     * Returns the Semux address.
     */
    fun toAddress(): ByteArray {
        return Hash.h160(getPublicKey())
    }

    /**
     * Returns the Semux address in [String].
     */
    fun toAddressString(): String {
        return Hex.encode(toAddress())
    }

    /**
     * Signs a message.
     *
     * @param message
     * message
     * @return
     */
    fun sign(message: ByteArray): Signature {
        try {
            val sig: ByteArray

                val engine = EdDSAEngine()
                engine.initSign(sk)
                sig = engine.signOneShot(message)


            return Signature(sig, pk.abyte)
        }
        catch(e : Exception ){
            throw e
        }

    }

    class Signature
    /**
     * Creates a Signature instance.
     *
     * @param s
     * @param a
     */
    (
            /**
             * Returns the S byte array.
             *
             * @return
             */
            val s: ByteArray?,
            /**
             * Returns the A byte array.
             *
             * @return
             */
            val a: ByteArray?) {

        /**
         * Returns the public key of the signer.
         *
         * @return
         */
        val publicKey: ByteArray
            get() = Bytes.merge(X509, a)

        /**
         * Returns the address of signer.
         *
         * @return
         */
        val address: ByteArray
            get() = Hash.h160(publicKey)

        init {
            if (s == null || s.size != S_LEN || a == null || a.size != A_LEN) {
                throw IllegalArgumentException("Invalid S or A")
            }
        }

        /**
         * Converts into a byte array.
         *
         * @return
         */
        fun toBytes(): ByteArray {
            return Bytes.merge(s, a)
        }

        override fun equals(o: Any?): Boolean {
            if (this === o)
                return true

            if (o == null || javaClass != o.javaClass)
                return false

            val signature = o as Signature?

            return EqualsBuilder()
                    .append(s, signature!!.s)
                    .append(a, signature.a)
                    .isEquals
        }

        override fun hashCode(): Int {
            return HashCodeBuilder(17, 37)
                    .append(s)
                    .append(a)
                    .toHashCode()
        }

        companion object {
            val LENGTH = 96

            private val X509 = Hex.decode("302a300506032b6570032100")
            private val S_LEN = 64
            private val A_LEN = 32

            /**
             * Parses from byte array.
             *
             * @param bytes
             * @return a [Signature] if success,or null
             */
            fun fromBytes(bytes: ByteArray?): Signature? {
                if (bytes == null || bytes.size != LENGTH) {
                    return null
                }

                val s = Arrays.copyOfRange(bytes, 0, S_LEN)
                val a = Arrays.copyOfRange(bytes, LENGTH - A_LEN, LENGTH)

                return Signature(s, a)
            }
        }
    }
}