package org.example;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Baoyi Chen
 */
public class AsciiConverter {
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
