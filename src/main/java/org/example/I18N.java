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
			return "。严格保证" + nounsSet + "这些飞行呼号或人名保留原文。严格保证Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray保留原文。坐标保留数字。雷达型号：" + radarModels() + "保留原文";
		} else if ("ja".equals(locale)) {
			return "。厳格に保証します" + nounsSet + "これらの飛行コールサインや人名は原文のままです。厳格に保証しますFox-[1-3], Maverick, Magnum, Rifle, Mud, X-Rayは原文のままです。座標は数字を保持します。レーダーモデル：" + radarModels() + "は原文のままです";
		} else if ("ko".equals(locale)) {
			return "。엄격하게 보장합니다" + nounsSet + "이 비행 호출 부호나 인명은 원문 그대로입니다. 엄격하게 보장합니다Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray는 원문 그대로입니다. 좌표는 숫자를 유지합니다. 레이더 모델：" + radarModels() + "는 원문 그대로입니다";
		} else {
			return "。严格保证" + nounsSet + "这些飞行呼号或人名保留原文。严格保证Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray保留原文。坐标保留数字。雷达型号：" + radarModels() + "保留原文";
		}
	}
	
	private static String radarModels() {
		return "Guideline, Goa, Gammon, Grumble, Gainful, Gecko, Gadfly, Grison, Flat Face, Spoon Rest, Low Blow, Fan Song, Land Roll, Snow Drift, Gun Dish, Hot Shot, Dog Ear, Grouse";
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
		if (text.length() > 1024) return null;
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
