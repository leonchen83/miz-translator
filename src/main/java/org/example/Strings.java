package org.example;

/**
 * @author Baoyi Chen
 */
public class Strings {
	public static String trim(String s, char c) {
		String r = ltrim(s, c);
		return rtrim(r, c);
	}
	
	public static String rtrim(String s, char trim) {
		if (s == null) return null;
		int i = s.length() - 1;
		for (; i >= 0; i--) if (s.charAt(i) != trim) break;
		if (i < 0) return "";
		return s.substring(0, i + 1);
	}
	
	public static String ltrim(String s, char trim) {
		if (s == null) return null;
		int i = 0, n = s.length();
		for (; i < n; i++)
			if (s.charAt(i) != trim) {
				break;
			}
		if (i >= n) {
			return "";
		} else return s.substring(i);
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
