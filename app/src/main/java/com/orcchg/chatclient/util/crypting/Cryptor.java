package com.orcchg.chatclient.util.crypting;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class Cryptor {

    public static String hash256(String data) throws NoSuchAlgorithmException {
        return encrypt(data, "SHA-256");
    }

    public static String md5(String data) throws NoSuchAlgorithmException {
        return encrypt(data, "MD5");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) {
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private static String encrypt(String data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(data.getBytes());
        String encrypted = bytesToHex(md.digest());
        Timber.v("Encrypted data: %s", encrypted);
        return encrypted;
    }
}
