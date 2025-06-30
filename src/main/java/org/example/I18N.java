/*
 * Copyright 2016-2017 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example;

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
}
