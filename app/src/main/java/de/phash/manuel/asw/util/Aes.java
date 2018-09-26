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

package de.phash.manuel.asw.util;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.phash.manuel.asw.semux.key.CryptoException;

public class Aes {

    private Aes() {
    }

    private static Key generateKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)
            throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        // create output buffer
        int size = cipher.getOutputSize(data.length);
        byte[] buf = new byte[size];

        // process data
        int length1 = cipher.processBytes(data, 0, data.length, buf, 0);
        int length2 = cipher.doFinal(buf, length1);
        int length = length1 + length2;

        // copy buffer to result, without padding
        byte[] result = new byte[length];
        System.arraycopy(buf, 0, result, 0, result.length);

        return result;


    }

    /**
     * Encrypt data with AES/CBC/PKCS5Padding.
     *
     * @param raw
     * @param salt
     * @return
     * @throws CryptoException
     */
    public static byte[] encrypt(byte[] raw, String password, byte[] salt) throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(password, salt));
            AlgorithmParameters params = cipher.getParameters();
            //byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(raw);
        /*    PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            CipherParameters params = new ParametersWithIV(new KeyParameter(key), iv);
            aes.init(true, params);

            return cipherData(aes, raw);*/
            return ciphertext;
        } catch (DataLengthException | IllegalArgumentException | IllegalStateException
                e) {
            throw new CryptoException(e);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt data with AES/CBC/PKCS5Padding
     *
     * @param encrypted
     * @param salt
     * @param iv
     * @return
     * @throws CryptoException
     */
    public static byte[] decrypt(byte[] encrypted, byte[] salt, String password, byte[] iv) throws CryptoException {
        try {
         /*   PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            CipherParameters params = new ParametersWithIV(new KeyParameter(key), iv);
            aes.init(false, params);

            return cipherData(aes, encrypted);*/
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(password, salt), new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);

        } catch (DataLengthException | IllegalArgumentException | IllegalStateException
                e) {
            throw new CryptoException(e);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

}