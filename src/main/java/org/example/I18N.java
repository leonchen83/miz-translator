package org.example;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Baoyi Chen
 */
public class I18N {
	
	public static String i18n(String file, String suffix, Configure configure) {
		return file + "." + configure.getLanguageCode() + "." + suffix;
	}
	
	public static String pi18n(String file, String suffix, Configure configure) {
		return file + "-" + configure.getLanguageCode() + "-" + suffix;
	}
	
	public static String localeVoice(Configure configure) {
		String locale = configure.getLanguageCode();
		switch (locale) {
			// 中文
			case "zh": return "zh-CN-YunyangNeural";
			// 日语
			case "ja": return "ja-JP-KeitaNeural";
			// 韩语
			case "ko": return "ko-KR-InJoonNeural";
			// 英语
			case "en": return "en-US-GuyNeural";
			// 西班牙语
			case "es": return "es-ES-ElviraNeural";
			// 法语
			case "fr": return "fr-FR-DeniseNeural";
			// 德语
			case "de": return "de-DE-KatjaNeural";
			// 葡萄牙语（巴西）
			case "pt": return "pt-BR-AntonioNeural";
			// 俄语
			case "ru": return "ru-RU-DariyaNeural";
			// 越南语
			case "vi": return "vi-VN-HoaiMyNeural";
			// 马来语
			case "ms": return "ms-MY-OsmanNeural";
			// 希腊语
			case "el": return "el-GR-AthinaNeural";
			// 丹麦语
			case "da": return "da-DK-ChristelNeural";
			// 挪威语
			case "nb": return "nb-NO-PernilleNeural";
			// 瑞典语
			case "sv": return "sv-SE-SofieNeural";
			// 意大利语
			case "it": return "it-IT-ElsaNeural";
			// 荷兰语
			case "nl": return "nl-NL-ColetteNeural";
			// 波兰语
			case "pl": return "pl-PL-ZofiaNeural";
			// 罗马尼亚语
			case "ro": return "ro-RO-AlinaNeural";
			// 捷克语
			case "cs": return "cs-CZ-VlastaNeural";
			// 匈牙利语
			case "hu": return "hu-HU-NoemiNeural";
			// 保加利亚语
			case "bg": return "bg-BG-KalinaNeural";
			// 乌克兰语
			case "uk": return "uk-UA-PolinaNeural";
			// 希伯来语
			case "he": return "he-IL-HilaNeural";
			// 阿拉伯语
			case "ar": return "ar-SA-ZariyahNeural";
			
			// 默认 fallback 英语
			default: return "en-US-GuyNeural";
		}
	}
	
	public static String localeLanguage(Configure configure, String spliter) {
		String locale = configure.getLanguageCode();
		switch (locale) {
			case "zh": // 中文
				return "。严格保留原分隔符" + spliter +
						"(程序需要通过这个" + spliter +
						"进行拆分, 如果翻译前和翻译后的分隔符数量不一致会导致问题), 例如：片段1" +
						spliter + "片段2" + spliter + "片段3";
			
			case "ja": // 日语
				return "。元の区切り文字 " + spliter +
						" を厳密に保持してください（プログラムはこの " + spliter +
						" で分割します。翻訳前後で区切り文字の数が一致しない場合、問題が発生します）, 例：フラグメント1" +
						spliter + "フラグメント2" + spliter + "フラグメント3";
			
			case "ko": // 韩语
				return "。원래 구분자 " + spliter +
						" 를 엄격하게 유지하세요(프로그램은 이 " + spliter +
						" 로 분할합니다. 번역 전후의 구분자 수가 일치하지 않으면 문제가 발생합니다), 예: 조각1" +
						spliter + "조각2" + spliter + "조각3";
			
			case "es": // 西班牙语
				return ". Conserve estrictamente el separador original " + spliter +
						" (el programa utiliza este " + spliter +
						" para dividir el contenido; si la cantidad de separadores antes y después de la traducción no coincide, se producirán errores), ejemplo: fragmento1" +
						spliter + "fragmento2" + spliter + "fragmento3";
			
			case "fr": // 法语
				return ". Conservez strictement le séparateur d'origine " + spliter +
						" (le programme utilise ce " + spliter +
						" pour effectuer la segmentation ; si le nombre de séparateurs diffère avant et après la traduction, des problèmes peuvent survenir), exemple : fragment1" +
						spliter + "fragment2" + spliter + "fragment3";
			
			case "de": // 德语
				return ". Behalten Sie den ursprünglichen Trennzeichen " + spliter +
						" strikt bei (das Programm verwendet dieses " + spliter +
						" zur Aufteilung; wenn die Anzahl der Trennzeichen vor und nach der Übersetzung nicht übereinstimmt, treten Probleme auf), Beispiel: Fragment1" +
						spliter + "Fragment2" + spliter + "Fragment3";
			
			case "pt": // 葡萄牙语（巴西 pt-BR）
				return ". Preserve rigorosamente o separador original " + spliter +
						" (o programa utiliza este " + spliter +
						" para dividir o conteúdo; se a quantidade de separadores antes e depois da tradução não coincidir, ocorrerão problemas), exemplo: fragmento1" +
						spliter + "fragmento2" + spliter + "fragmento3";
			
			case "ru": // 俄语
				return ". Строго сохраняйте исходный разделитель " + spliter +
						" (программа использует этот " + spliter +
						" для разделения; если количество разделителей до и после перевода не совпадает, возникнут проблемы), пример: фрагмент1" +
						spliter + "фрагмент2" + spliter + "фрагмент3";
			
			default:
				return ". Strictly preserve the original separator " + spliter +
						" (the program uses this " + spliter +
						" to split the content; if the number of separators before and after translation does not match, it may cause problems), e.g., fragment1" +
						spliter + "fragment2" + spliter + "fragment3";
		}
	}
	
	public static String nounsHint(Configure configure, Set<String> nounsSet) {
		String locale = configure.getLanguageCode();
		String nounsStr = nounsSet.toString();
		String radarStr = radarModels();
		switch (locale) {
			case "zh":
				return "。严格保证" + nounsStr + "这些飞行呼号或人名保留原文。严格保证Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray保留原文。坐标保留数字。雷达型号：" + radarStr + "保留原文。数字翻译例：ninety-nine翻译为99，four-zero-one翻译为401";
			case "ja":
				return "。厳格に保証します" + nounsStr + "これらの飛行コールサインや人名は原文のままです。厳格に保証しますFox-[1-3], Maverick, Magnum, Rifle, Mud, X-Rayは原文のままです。座標は数字を保持します。レーダーモデル：" + radarStr + "は原文のままです。数字の翻訳例：ninety-nineは99に、four-zero-oneは401に翻訳されます";
			case "ko":
				return "。엄격하게 보장합니다" + nounsStr + "이 비행 호출 부호나 인명은 원문 그대로입니다. 엄격하게 보장합니다Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray는 원문 그대로입니다. 좌표는 숫자를 유지합니다. 레이더 모델：" + radarStr + "는 원문 그대로입니다. 숫자 번역 예: ninety-nine는 99로, four-zero-one은 401로 번역됩니다";
			case "es":
				return ". Garantice estrictamente que " + nounsStr + " estos indicativos de vuelo o nombres se mantienen en el texto original. Garantice estrictamente Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray se mantienen en el original. Las coordenadas mantienen los números. Modelo de radar: " + radarStr + " se mantiene en el original. Ejemplo de traducción de números: ninety-nine → 99, four-zero-one → 401";
			case "fr":
				return ". Assurez-vous strictement que " + nounsStr + " ces indicatifs d'appel ou noms propres restent en texte original. Assurez-vous strictement que Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray restent en original. Les coordonnées conservent les nombres. Modèle de radar: " + radarStr + " reste en original. Exemple de traduction des nombres: ninety-nine → 99, four-zero-one → 401";
			case "de":
				return ". Stellen Sie strikt sicher, dass " + nounsStr + " diese Rufzeichen oder Namen im Original bleiben. Stellen Sie strikt sicher, dass Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray im Original bleiben. Koordinaten behalten die Zahlen. Radarmodell: " + radarStr + " bleibt im Original. Zahlenbeispiel: ninety-nine → 99, four-zero-one → 401";
			case "pt":
				return ". Garanta estritamente que " + nounsStr + " estes indicativos de voo ou nomes permaneçam no original. Garanta estritamente Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray permaneçam no original. As coordenadas mantêm os números. Modelo de radar: " + radarStr + " permanece no original. Exemplo de tradução de números: ninety-nine → 99, four-zero-one → 401";
			case "ru":
				return ". Строго сохраняйте " + nounsStr + " эти позывные или имена в оригинале. Строго сохраняйте Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray в оригинале. Координаты сохраняют числа. Модель радара: " + radarStr + " сохраняется в оригинале. Пример перевода чисел: ninety-nine → 99, four-zero-one → 401";
			default:
				return ". Strictly ensure that " + nounsStr + " these flight call signs or names remain in the original text. Strictly ensure Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray remain in original. Coordinates retain numbers. Radar model: " + radarStr + " remains in original. Number translation examples: ninety-nine → 99, four-zero-one → 401";
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
		if (nouns.length() <= 2) {
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
