package org.example;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Baoyi Chen
 */
public class I18N {
	
	public static String localeLanguage(Configure configure, String spliter) {
		String locale = configure.getLanguageCode();
		if (locale == null || locale.isEmpty() || "zh".equals(locale)) {
			return "。保留原分隔符" + spliter + "例如：片段1" + spliter + "片段2" + spliter + "片段3";
		} else if( "ja".equals(locale)) {
			return "。区切り文字を保持します" + spliter + "例：フラグメント1" + spliter + "フラグメント2" + spliter + "フラグメント3";
		} else if ("ko".equals(locale)) {
			return "。구분 기호를 유지합니다" + spliter + "예: 조각 1" + spliter + "조각 2" + spliter + "조각 3";
		} else {
			return "。保留原分隔符" + spliter + "例如：片段1" + spliter + "片段2" + spliter + "片段3";
		}
	}
	
	public static String nounsHint(Configure configure, Set<String> nounsSet) {
		String locale = configure.getLanguageCode();
		if (locale == null || locale.isEmpty() || "zh".equals(locale)) {
			return "。严格保证" + nounsSet + "这些词不翻译。严格保证Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray不翻译";
		} else if ("ja".equals(locale)) {
			return "。厳密に保証します" + nounsSet + "これらの単語は翻訳されません。Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Rayは翻訳されません";
		} else if ("ko".equals(locale)) {
			return "。엄격하게 보장합니다" + nounsSet + "이 단어는 번역되지 않습니다. Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray은 번역되지 않습니다";
		} else {
			return "。严格保证" + nounsSet + "这些词不翻译。严格保证Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray不翻译";
		}
	}
	
	public static boolean containsTranslatedLanguage(Configure configure, String content) {
		for (int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);
			if ((ch >= '\u4e00' && ch <= '\u9fa5') || // Chinese
					(ch >= '\u3040' && ch <= '\u30ff') || // Japanese Hiragana and Katakana
					(ch >= '\uac00' && ch <= '\ud7af')) { // Korean Hangul
				return true;
			}
		}
		return false;
	}
	
	public static void addNouns(String text, Set<String> nounsSet) {
		String nouns = retrieveProperNouns(text);
		if (nouns != null) {
			nounsSet.add(nouns);
		}
	}
	
	public static String retrieveProperNouns(String text) {
		if (text.length() >= 1024) return null;
		int index = text.indexOf(':');
		if (index <= 0) {
			index = text.indexOf('：');
		}
		if (index <= 0) {
			return null;
		}
		String nouns = text.substring(0, index);
		nouns = nouns.replaceAll("\\r?\\n|\\r", "").trim();
		
		if (nouns.length() >= 16) {
			return null;
		}
		
		index = nouns.indexOf(' ');
		if (index > 0) {
			nouns = nouns.substring(0, index);
		}
		
		for (int i = 0; i < nouns.length(); i++) {
			char c = nouns.charAt(i);
			if (!isLowerCase(c) && !isUpperCase(c)) {
				return null;
			}
		}
		
		nouns = nouns.toLowerCase();
		if (nouns.equals("player")) {
			return null;
		}
		return StringUtils.capitalize(nouns);
	}
	
	private static boolean isLowerCase(char c) {
		return c >= 'a' && c <= 'z';
	}
	
	private static boolean isUpperCase(char c) {
		return c >= 'A' && c <= 'Z';
	}
}
