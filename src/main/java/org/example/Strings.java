package org.example;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Baoyi Chen
 */
public class Strings {
	private static final Map<Character, String> UNICODE_TO_ASCII_MAP = new HashMap<>();
	
	static {
		UNICODE_TO_ASCII_MAP.put('‘', "'");
		UNICODE_TO_ASCII_MAP.put('’', "'");
		UNICODE_TO_ASCII_MAP.put('“', "\"");
		UNICODE_TO_ASCII_MAP.put('”', "\"");
		UNICODE_TO_ASCII_MAP.put('—', "--");
		UNICODE_TO_ASCII_MAP.put('–', "-");
		UNICODE_TO_ASCII_MAP.put('×', "x");
		UNICODE_TO_ASCII_MAP.put('÷', "/");
		UNICODE_TO_ASCII_MAP.put('±', "+/-");
		UNICODE_TO_ASCII_MAP.put('≈', "~");
		UNICODE_TO_ASCII_MAP.put('≤', "<=");
		UNICODE_TO_ASCII_MAP.put('≥', ">=");
		UNICODE_TO_ASCII_MAP.put('°', "deg");
		UNICODE_TO_ASCII_MAP.put('€', "EUR");
		UNICODE_TO_ASCII_MAP.put('£', "GBP");
		UNICODE_TO_ASCII_MAP.put('¥', "YEN");
		UNICODE_TO_ASCII_MAP.put('¢', "c");
		UNICODE_TO_ASCII_MAP.put('©', "(C)");
		UNICODE_TO_ASCII_MAP.put('®', "(R)");
		UNICODE_TO_ASCII_MAP.put('™', "(TM)");
		UNICODE_TO_ASCII_MAP.put(' ', " ");
		UNICODE_TO_ASCII_MAP.put('：', ":");
		UNICODE_TO_ASCII_MAP.put('…', "...");
	}
	
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
	
	public static boolean containsLowerCase(String value) {
		final int sz = value.length();
		for (int i = 0; i < sz; i++) {
			char c = value.charAt(i);
			if (c >= 'a' && c <= 'z') {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isLikelyLua(String code) {
		return code.matches("(?s).*\\b(function|local|if|then|end|return|print|for|while|do)\\b.*") || code.matches("(?s).*[=(){};].*");
	}
	
	public static boolean isAllNumber(String str) {
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
		}
		return true;
	}
	
	public static String convertToAscii(String input) {
		if (input == null) {
			return null;
		}
		
		String normalized = Normalizer.normalize(input, Normalizer.Form.NFKD);
		
		StringBuilder asciiString = new StringBuilder();
		for (char c : normalized.toCharArray()) {
			if (c < 128) {
				asciiString.append(c);
			} else if (UNICODE_TO_ASCII_MAP.containsKey(c)) {
				asciiString.append(UNICODE_TO_ASCII_MAP.get(c));
			} else {
			}
		}
		
		return asciiString.toString();
	}
}
