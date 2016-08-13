package com.orcchg.chatclient.util.crypting;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import timber.log.Timber;

/**
 * @see 'http://www.java2s.com/Tutorial/Java/0490__Security/BasicRSAexample.htm'
 * @see 'https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/'
 */
public class RSACryptor {

    public static String encryptRSA(String source, String publicPem) {
        try {
            PublicKey key = getPublicKeyFromPEM(publicPem);
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] data = cipher.doFinal(source.getBytes());
            return Cryptor.bytesToHex(data);
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
        return source;
    }

    public static String decryptRSA(String source, String privatePem) {
        try {
            PrivateKey key = getPrivateKeyFromPEM(privatePem);
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] data = Cryptor.hexToBytes(source);
            byte[] plain = cipher.doFinal(data);
            return new String(plain);
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
        return source;
    }

    private static PublicKey getPublicKeyFromPEM(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.decode(pem, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private static PrivateKey getPrivateKeyFromPEM(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.decode(pem, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
