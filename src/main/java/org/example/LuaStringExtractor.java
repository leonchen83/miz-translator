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
			"trigger\\.action\\.outText\\s*\\(\\s*(['\"])((?:\\\\.|(?!\\1).)*?)\\1",
			Pattern.DOTALL
	);
	
	
	public static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(
			"(['\"])((?:\\\\.|(?!\\1).)*?)\\1"
	);
	
	public static void extractSubtitleStrings(String luaContent, Map<String, String> subtitles, Configure configure) {
		
		Matcher subtitleMatcher = SUBTITLE_EXPR_PATTERN.matcher(luaContent);
		
		while (subtitleMatcher.find()) {
			
			String expression = subtitleMatcher.group(1).trim();
			
			if (expression.endsWith(",")) {
				expression = expression.substring(0, expression.length() - 1);
			}
			
			Matcher matcher = STRING_LITERAL_PATTERN.matcher(expression);
			
			while (matcher.find()) {
				String text = matcher.group(2); // 内容在 group(2)
				
				if (text.length() >= configure.getMinimumLength()) {
					subtitles.put(text, text);
				}
			}
		}
	}
	
	public static void extractOutTextStrings(String luaContent, Map<String, String> outTexts, Configure configure) {
		
		Matcher matcher = OUTTEXT_PATTERN.matcher(luaContent);
		
		while (matcher.find()) {
			
			String text = matcher.group(2);
			
			if (text.length() >= configure.getMinimumLength()) {
				outTexts.put(text, text);
			}
		}
	}
	
	public static String replaceInSubtitle(String content, Map<String, String> map) {
		
		Matcher matcher = SUBTITLE_EXPR_PATTERN.matcher(content);
		StringBuffer result = new StringBuffer();
		
		while (matcher.find()) {
			
			String expression = matcher.group(1);
			
			String replacedExpression = replaceStringLiterals(expression, map);
			
			matcher.appendReplacement(result,
					Matcher.quoteReplacement("subtitle = " + replacedExpression));
		}
		
		matcher.appendTail(result);
		return result.toString();
	}
	
	public static String replaceInOutText(String content, Map<String, String> map) {
		
		Matcher matcher = OUTTEXT_PATTERN.matcher(content);
		StringBuffer sb = new StringBuffer();
		
		while (matcher.find()) {
			
			String quote = matcher.group(1);
			String original = matcher.group(2);
			
			String translated = map.getOrDefault(original, original);
			
			translated = translated
					.replace("\\", "\\\\")
					.replace(quote, "\\" + quote);
			
			String replacement =
					matcher.group(0).replace(original, translated);
			
			matcher.appendReplacement(sb,
					Matcher.quoteReplacement(replacement));
		}
		
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	private static String replaceWithPattern(String content, Pattern pattern, Map<String, String> map, String quote) {
		
		Matcher matcher = pattern.matcher(content);
		StringBuffer sb = new StringBuffer();
		
		while (matcher.find()) {
			
			String original = matcher.group(1);
			String translated = map.getOrDefault(original, original);
			
			// 转义处理
			translated = translated
					.replace("\\", "\\\\")
					.replace(quote, "\\" + quote);
			
			matcher.appendReplacement(sb,
					Matcher.quoteReplacement(
							matcher.group(0).replace(original, translated)
					));
		}
		
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	private static String replaceLiteral(String content, Map<String, String> map) {
		
		Matcher matcher = STRING_LITERAL_PATTERN.matcher(content);
		StringBuffer sb = new StringBuffer();
		
		while (matcher.find()) {
			
			String quote = matcher.group(1);      // ' 或 "
			String original = matcher.group(2);   // 内容
			
			String translated = map.getOrDefault(original, original);
			
			translated = translated
					.replace("\\", "\\\\")
					.replace(quote, "\\" + quote);
			
			matcher.appendReplacement(sb,
					Matcher.quoteReplacement(quote + translated + quote));
		}
		
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	private static String replaceStringLiterals(String expression, Map<String, String> map) {
		return replaceLiteral(expression, map);
	}
}