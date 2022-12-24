package me.mrfunny.chatapp.api.util;

public class StringUtil {
    public static boolean isBadString(String toCheck) {
        return toCheck == null || toCheck.trim().equals("");
    }
}
