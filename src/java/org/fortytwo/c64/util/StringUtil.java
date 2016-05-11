package org.fortytwo.c64.util;


public class StringUtil
{
	public static String leftPad(String str, int width) {
		if (str.length() >= width) {
			return str;
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = str.length(); i < width; i++) {
				sb.append(" ");
			}
			sb.append(str);
			return sb.toString();
		}
	}
}
