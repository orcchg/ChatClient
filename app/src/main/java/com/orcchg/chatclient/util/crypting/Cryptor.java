package com.orcchg.chatclient.util.crypting;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class Cryptor {

    public static String encrypt(String data) {
        try {
            return Cryptor.hash256(data);
        } catch (NoSuchAlgorithmException e) {
            Timber.e("Failed to encrypt password: %s", Log.getStackTraceString(e));
        }
        return data;
    }

    public static String hash256(String data) throws NoSuchAlgorithmException {
        return encrypt(data, "SHA-256");
    }

    public static String md5(String data) throws NoSuchAlgorithmException {
        return encrypt(data, "MD5");
    }

    static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) {
//            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
            result.append(Integer.toHexString((byt & 0xFF) | 0x100).substring(1, 3));
        }
        return result.toString();
    }

    static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static String encrypt(String data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(data.getBytes());
        String encrypted = bytesToHex(md.digest());
        Timber.v("Encrypted data: %s", encrypted);
        return encrypted;
    }
}
