package org.example;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Baoyi Chen
 */
public class LuaStringExtractor {
	
	private static final Pattern SUBTITLE_EXPR_PATTERN = Pattern.compile(
			"subtitle\\s*=\\s*(.+)$",
			Pattern.MULTILINE
	);
	
	private static final Pattern OUTTEXT_PATTERN = Pattern.compile(
			"trigger\\.action\\.outText\\s*\\(\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
			Pattern.DOTALL
	);
	
	private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(
			"\"((?:\\\\.|[^\"\\\\])*)\""
	);
	
	private static Set<String> extractSubtitleStrings(String luaContent, Configure configure) {
		Set<String> subtitles = new HashSet<>();
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
					subtitles.add(text);
				}
			}
		}
		return subtitles;
	}
	
	private static Set<String> extractOutTextStrings(String luaContent, Configure configure) {
		Set<String> outTexts = new HashSet<>();
		Matcher outMatcher = OUTTEXT_PATTERN.matcher(luaContent);
		while (outMatcher.find()) {
			String text = outMatcher.group(1);
			if (text != null && text.length() >= configure.getMinimumLength()) {
				outTexts.add(text);
			}
		}
		return outTexts;
	}
}
