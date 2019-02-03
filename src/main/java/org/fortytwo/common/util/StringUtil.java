package org.fortytwo.common.util;

public class StringUtil {
    public static String leftPad(String str, int width) {

        return (str.length() >= width)
                ? str
                : " ".repeat(width - str.length()) + str;
    }
}
