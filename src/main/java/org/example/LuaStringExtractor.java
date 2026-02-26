package org.example;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Baoyi Chen
 */
public class LuaStringExtractor {
	
	
	public static final Pattern SUBTITLE_EXPR_PATTERN = Pattern.compile(
			"subtitle\\s*=\\s*(.+)$",
			Pattern.MULTILINE
	);
	
	public static final Pattern OUTTEXT_PATTERN = Pattern.compile(
			"trigger\\.action\\.outText\\s*\\(\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
			Pattern.DOTALL
	);
	
	public static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(
			"\"((?:\\\\.|[^\"\\\\])*)\""
	);
	
	public static void extractSubtitleStrings(String luaContent, Map<String, String> subtitles, Configure configure) {
		Matcher subtitleMatcher = SUBTITLE_EXPR_PATTERN.matcher(luaContent);
		while (subtitleMatcher.find()) {
			String expression = subtitleMatcher.group(1).trim();
			
			if (expression.endsWith(",")) {
				expression = expression.substring(0, expression.length() - 1);
			}
			
			Matcher stringMatcher = STRING_LITERAL_PATTERN.matcher(expression);
			while (stringMatcher.find()) {
				String text = stringMatcher.group(1);
				if (text != null && text.length() >= configure.getMinimumLength()) {
					subtitles.put(text, text);
				}
			}
		}
	}
	
	public static void extractOutTextStrings(String luaContent, Map<String, String> outTexts, Configure configure) {
		Matcher outMatcher = OUTTEXT_PATTERN.matcher(luaContent);
		while (outMatcher.find()) {
			String text = outMatcher.group(1);
			if (text != null && text.length() >= configure.getMinimumLength()) {
				outTexts.put(text, text);
			}
		}
	}
}
