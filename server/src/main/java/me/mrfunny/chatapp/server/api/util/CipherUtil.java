package me.mrfunny.chatapp.server.api.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CipherUtil {
    public static String sha256(String text) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(text.getBytes(StandardCharsets.UTF_8));
        return String.format("%064x", new BigInteger(1, hash));
    }
}
