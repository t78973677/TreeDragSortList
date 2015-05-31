package org.jain.utils;

public class StringUtil {

    public static boolean isEmpty(String value) {
        return value == null || value.trim().equals("") || value.length() == 0;
    }
}
