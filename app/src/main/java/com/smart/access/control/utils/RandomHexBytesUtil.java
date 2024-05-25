package com.smart.access.control.utils;

import java.security.SecureRandom;

public class RandomHexBytesUtil {

    // Method to generate 10 random bytes and convert to hex string
    public static byte[] generateRandomBytes(int byteCount) {
        byte[] randomBytes = new byte[byteCount];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    // Method to convert a byte array to a hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0'); // Pad with leading zero if necessary
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


}

