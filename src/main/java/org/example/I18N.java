package org.example;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Baoyi Chen
 */
public class I18N {
	
	public static final Set<String> PROTECTED_TERMS = new HashSet<>();
	
	static {
		PROTECTED_TERMS.add("STRIKE");
		PROTECTED_TERMS.add("VIPER");
		PROTECTED_TERMS.add("FALCON");
		PROTECTED_TERMS.add("PANTHER");
		PROTECTED_TERMS.add("TIGER");
		PROTECTED_TERMS.add("KNIGHT");
		PROTECTED_TERMS.add("RAM");
		PROTECTED_TERMS.add("TROJAN");
		PROTECTED_TERMS.add("HAWG");
		PROTECTED_TERMS.add("RAPTOR");
		PROTECTED_TERMS.add("REAPER");
		PROTECTED_TERMS.add("HORNET");
		PROTECTED_TERMS.add("MAGIC");
		PROTECTED_TERMS.add("OVERLORD");
		PROTECTED_TERMS.add("WIZARD");
		PROTECTED_TERMS.add("DARKSTAR");
		PROTECTED_TERMS.add("SKYEYE");
		PROTECTED_TERMS.add("TEXACO");
		PROTECTED_TERMS.add("SHELL");
		PROTECTED_TERMS.add("ARCO");
		PROTECTED_TERMS.add("DEVIL");
		PROTECTED_TERMS.add("CHEVY");
		PROTECTED_TERMS.add("CHEV");
		PROTECTED_TERMS.add("DODGE");
		PROTECTED_TERMS.add("LANCER");
		PROTECTED_TERMS.add("COLT");
		PROTECTED_TERMS.add("SPRINGFIELD");
		PROTECTED_TERMS.add("ENFIELD");
		PROTECTED_TERMS.add("UZI");
		PROTECTED_TERMS.add("PYTHON");
		PROTECTED_TERMS.add("VENOM");
		PROTECTED_TERMS.add("AXEMAN");
		PROTECTED_TERMS.add("JESTER");
		PROTECTED_TERMS.add("SABER");
		PROTECTED_TERMS.add("EAGLE");
		PROTECTED_TERMS.add("VIKING");
		PROTECTED_TERMS.add("CASE 1");
		PROTECTED_TERMS.add("CASE 2");
		PROTECTED_TERMS.add("CASE 3");
		PROTECTED_TERMS.add("ARCHANGEL");
		PROTECTED_TERMS.add("SWORD");
		PROTECTED_TERMS.add("WEASEL");
		PROTECTED_TERMS.add("SPARTAN");
		PROTECTED_TERMS.add("HAMMER");
		PROTECTED_TERMS.add("WARDEN");
		PROTECTED_TERMS.add("SENTINEL");
	}
	
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
		}
		throw new IllegalArgumentException("required option:<voice>");
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
		Set<String> nounsCopy = new HashSet<>(nounsSet);
		nounsCopy.addAll(PROTECTED_TERMS);
		String nounsStr = nounsCopy.toString();
		String radarStr = radarModels();
		switch (locale) {
			case "zh":
				return "。严格保证" + nounsStr + "这些飞行呼号或人名保留原文。严格保证Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie保留原文。坐标保留数字。雷达型号：" + radarStr + "保留原文。数字翻译例：ninety-nine翻译为99，four-zero-one翻译为401";
			case "ja":
				return "。厳格に保証します" + nounsStr + "これらの飛行コールサインや人名は原文のままです。厳格に保証しますFox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlieは原文のままです。座標は数字を保持します。レーダーモデル：" + radarStr + "は原文のままです。数字の翻訳例：ninety-nineは99に、four-zero-oneは401に翻訳されます";
			case "ko":
				return "。엄격하게 보장합니다" + nounsStr + "이 비행 호출 부호나 인명은 원문 그대로입니다. 엄격하게 보장합니다Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie는 원문 그대로입니다. 좌표는 숫자를 유지합니다. 레이더 모델：" + radarStr + "는 원문 그대로입니다. 숫자 번역 예: ninety-nine는 99로, four-zero-one은 401로 번역됩니다";
			case "es":
				return ". Garantice estrictamente que " + nounsStr + " estos indicativos de vuelo o nombres se mantienen en el texto original. Garantice estrictamente Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie se mantienen en el original. Las coordenadas mantienen los números. Modelo de radar: " + radarStr + " se mantiene en el original. Ejemplo de traducción de números: ninety-nine → 99, four-zero-one → 401";
			case "fr":
				return ". Assurez-vous strictement que " + nounsStr + " ces indicatifs d'appel ou noms propres restent en texte original. Assurez-vous strictement que Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie restent en original. Les coordonnées conservent les nombres. Modèle de radar: " + radarStr + " reste en original. Exemple de traduction des nombres: ninety-nine → 99, four-zero-one → 401";
			case "de":
				return ". Stellen Sie strikt sicher, dass " + nounsStr + " diese Rufzeichen oder Namen im Original bleiben. Stellen Sie strikt sicher, dass Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie im Original bleiben. Koordinaten behalten die Zahlen. Radarmodell: " + radarStr + " bleibt im Original. Zahlenbeispiel: ninety-nine → 99, four-zero-one → 401";
			case "pt":
				return ". Garanta estritamente que " + nounsStr + " estes indicativos de voo ou nomes permaneçam no original. Garanta estritamente Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie permaneçam no original. As coordenadas mantêm os números. Modelo de radar: " + radarStr + " permanece no original. Exemplo de tradução de números: ninety-nine → 99, four-zero-one → 401";
			case "ru":
				return ". Строго сохраняйте " + nounsStr + " эти позывные или имена в оригинале. Строго сохраняйте Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo в оригинале. Координаты сохраняют числа. Модель радара: " + radarStr + " сохраняется в оригинале. Пример перевода чисел: ninety-nine → 99, four-zero-one → 401";
			default:
				return ". Strictly ensure that " + nounsStr + " these flight call signs or names remain in the original text. Strictly ensure Fox-[1-3], Maverick, Magnum, Rifle, Mud, X-Ray, Alpha, Bravo, Charlie remain in original. Coordinates retain numbers. Radar model: " + radarStr + " remains in original. Number translation examples: ninety-nine → 99, four-zero-one → 401";
		}
	}
	
	public static String militarySlang(Configure configure) {
		String locale = configure.getLanguageCode();
		switch (locale) {
			case "zh":
				return "。军事术语翻译例: [ " +
						"Cleared Hot -> 允许开火, " +
						"Hot -> 朝我方/进入攻击态势, " +
						"Cold -> 远离态势/脱离航向, " +
						"is cold -> 已失效/无活动迹象, " +
						"Break left -> 左转规避, " +
						"Break right -> 右转规避, " +
						"Bandit -> 敌机（确认敌对）, " +
						"Bogey -> 不明空中目标, " +
						"Splash one -> 击落一架, " +
						"Splash two -> 击落两架, " +
						"Angels 25 -> 高度25000英尺, " +
						"FL150 -> 高度15000英尺, " +
						"Tally -> 目视确认, " +
						"No joy -> 未目视到目标, " +
						"In the pipe -> 导弹已发射/导弹飞行中, " +
						"Winchester -> 弹药耗尽, " +
						"Anchor at 3000 -> 在3000英尺建立盘旋待命, " +
						"20 miles -> 20海里, " +
						"Bullseye -> 靶眼(不要翻译成牛眼), " +
						"Bulls -> 靶眼(不要翻译成公牛), " +
						"Bulls 270/20 -> 靶眼 270度 20海里, " +
						"Feet dry -> 在陆地上空(不要翻译成干脚), " +
						"Feet wet -> 已入海上空(不要翻译成脚湿), " +
						"Fence In -> 进入作战状态(Fence不要翻译成围栏或栅栏), " +
						"Fence Out -> 退出作战状态(Fence不要翻译成围栏或栅栏), " +
						"Fenced In -> 已进入作战状态(Fenced不要翻译成围栏或栅栏), " +
						"Fenced Out -> 已退出作战状态(Fenced不要翻译成围栏或栅栏), " +
						"Tapes On -> 录像开启, " +
						"Tapes Off -> 录像关闭, " +
						"on station -> 就位, " +
						"playtime -> 任务时间, " +
						"Tow copy -> 二号 收到(不要翻译成收到两份), " +
						"Three copy -> 三号 收到(不要翻译成收到三份), " +
						"Four copy -> 四号 收到(不要翻译成收到四份), " +
						"Holding hands with XX -> 与XX紧密编队, " +
						"XXX is clear -> XXX安全, " +
						"CASE 1 Recovery -> CASE 1回收(Recovery一般翻译成回收), " +
						"CAT 4 -> 4号弹射器 ]";
			
			case "ja":
				return "。軍事用語翻訳例: [ " +
						"Hot -> 朝自機/攻撃態勢, " +
						"Cold -> 遠ざかる/接触なし, " +
						"is cold -> 無効化済み, " +
						"Break left -> 左に回避, " +
						"Break right -> 右に回避, " +
						"Bandit -> 敵機（確認済み）, " +
						"Bogey -> 未確認機, " +
						"Splash one -> 1機撃墜, " +
						"Splash two -> 2機撃墜, " +
						"Angels 25 -> 高度25000フィート, " +
						"FL150 -> 高度15000フィート, " +
						"Tally -> 視認, " +
						"No joy -> 視認不可, " +
						"In the pipe -> ミサイル飛行中, " +
						"Winchester -> 弾薬切れ, " +
						"Anchor at 3000 -> 高度3000フィートで待機旋回, " +
						"20 miles -> 20海里, " +
						"Bullseye -> ブルズアイ, " +
						"Bulls -> ブルズアイ, " +
						"Bulls 270/20 -> ブルズアイ 270度 20海里, " +
						"Feet dry -> 陸上, " +
						"Feet wet -> 海上, " +
						"Fence In -> 作戦状態に入る, " +
						"Fence Out -> 作戦状態から退出, " +
						"Tapes On -> 録画開始, " +
						"Tapes Off -> 録画停止 ]";
			
			
			case "ko":
				return "。군사용어 번역 예: [ " +
						"Hot -> 우리 쪽으로/공격 태세, " +
						"Cold -> 멀어짐/접촉 없음, " +
						"is cold -> 무력화됨, " +
						"Break left -> 좌측 회피, " +
						"Break right -> 우측 회피, " +
						"Bandit -> 적기(확인됨), " +
						"Bogey -> 미확인기, " +
						"Splash one -> 1기 격추, " +
						"Splash two -> 2기 격추, " +
						"Angels 25 -> 고도 25000피트, " +
						"FL150 -> 고도 15000피트, " +
						"Tally -> 육안 확인, " +
						"No joy -> 시각 확인 불가, " +
						"In the pipe -> 미사일 비행 중, " +
						"Winchester -> 탄약 소진, " +
						"Anchor at 3000 -> 3000피트에서 선회 대기, " +
						"20 miles -> 20해리, " +
						"Bullseye -> 불아이, " +
						"Bulls -> 불아이, " +
						"Bulls 270/20 -> 불아이 270도 20해리, " +
						"Feet dry -> 육상, " +
						"Feet wet -> 해상, " +
						"Fence In -> 작전 상태 진입, " +
						"Fence Out -> 작전 상태 해제, " +
						"Tapes On -> 녹화 시작, " +
						"Tapes Off -> 녹화 종료 ]";
			
			case "es":
				return ". Ejemplos de traducción de jerga militar: [ " +
						"Hot -> hacia nosotros/en actitud de ataque, " +
						"Cold -> alejándose/de contacto perdido, " +
						"is cold -> neutralizado, " +
						"Break left -> virar a la izquierda, " +
						"Break right -> virar a la derecha, " +
						"Bandit -> avión enemigo (confirmado), " +
						"Bogey -> contacto no identificado, " +
						"Splash one -> un derribo, " +
						"Splash two -> dos derribos, " +
						"Angels 25 -> altitud 25000 pies, " +
						"FL150 -> nivel de vuelo 150, " +
						"Tally -> contacto visual, " +
						"No joy -> sin contacto visual, " +
						"In the pipe -> misil en vuelo, " +
						"Winchester -> sin munición, " +
						"Anchor at 3000 -> órbita a 3000 pies, " +
						"20 miles -> 20 millas náuticas, " +
						"Bullseye -> punto de referencia, " +
						"Bulls -> punto de referencia, " +
						"Bulls 270/20 -> punto de referencia 270° 20 millas, " +
						"Feet dry -> sobre tierra, " +
						"Feet wet -> sobre agua, " +
						"Fence In -> entrar en estado de combate, " +
						"Fence Out -> salir del estado de combate, " +
						"Tapes On -> iniciar grabación, " +
						"Tapes Off -> detener grabación ]";
			
			case "fr":
				return ". Exemples de traduction de jargon militaire: [ " +
						"Hot -> vers nous/en posture d'attaque, " +
						"Cold -> s'éloigne/pas de contact, " +
						"is cold -> neutralisé, " +
						"Break left -> virage à gauche, " +
						"Break right -> virage à droite, " +
						"Bandit -> avion ennemi (confirmé), " +
						"Bogey -> contact non identifié, " +
						"Splash one -> un abattu, " +
						"Splash two -> deux abattus, " +
						"Angels 25 -> altitude 25000 pieds, " +
						"FL150 -> niveau de vol 150, " +
						"Tally -> contact visuel, " +
						"No joy -> pas de contact visuel, " +
						"In the pipe -> missile en vol, " +
						"Winchester -> plus de munitions, " +
						"Anchor at 3000 -> orbite à 3000 pieds, " +
						"20 miles -> 20 milles nautiques, " +
						"Bullseye -> point de référence, " +
						"Bulls -> point de référence, " +
						"Bulls 270/20 -> point de référence 270° 20 milles, " +
						"Feet dry -> au-dessus de terre, " +
						"Feet wet -> au-dessus de l'eau, " +
						"Fence In -> entrer en posture de combat, " +
						"Fence Out -> sortir de la posture de combat, " +
						"Tapes On -> début enregistrement, " +
						"Tapes Off -> fin enregistrement ]";
			
			case "de":
				return ". Beispiele für militärische Umgangssprache: [ " +
						"Hot -> auf uns/Angriffsbereitschaft, " +
						"Cold -> entfernt/kein Kontakt, " +
						"is cold -> neutralisiert, " +
						"Break left -> scharf links abdrehen, " +
						"Break right -> scharf rechts abdrehen, " +
						"Bandit -> feindliches Flugzeug (bestätigt), " +
						"Bogey -> unbekanntes Ziel, " +
						"Splash one -> ein Abschuss, " +
						"Splash two -> zwei Abschüsse, " +
						"Angels 25 -> Höhe 25000 Fuß, " +
						"FL150 -> Flugfläche 150, " +
						"Tally -> Sichtkontakt, " +
						"No joy -> kein Sichtkontakt, " +
						"In the pipe -> Rakete unterwegs, " +
						"Winchester -> keine Munition, " +
						"Anchor at 3000 -> Warteschleife auf 3000 Fuß, " +
						"20 miles -> 20 Seemeilen, " +
						"Bullseye -> Referenzpunkt, " +
						"Bulls -> Referenzpunkt, " +
						"Bulls 270/20 -> Referenzpunkt 270° 20 Seemeilen, " +
						"Feet dry -> über Land, " +
						"Feet wet -> über Wasser, " +
						"Fence In -> Kampfstufe aktiv, " +
						"Fence Out -> Kampfstufe deaktiviert, " +
						"Tapes On -> Aufnahme starten, " +
						"Tapes Off -> Aufnahme stoppen ]";
			
			case "pt":
				return ". Exemplos de tradução de gíria militar: [ " +
						"Hot -> em direção a nós/ataque, " +
						"Cold -> afastando-se/sem contato, " +
						"is cold -> neutralizado, " +
						"Break left -> virar à esquerda, " +
						"Break right -> virar à direita, " +
						"Bandit -> aeronave inimiga (confirmada), " +
						"Bogey -> contato não identificado, " +
						"Splash one -> um abatido, " +
						"Splash two -> dois abatidos, " +
						"Angels 25 -> altitude 25000 pés, " +
						"FL150 -> nível de voo 150, " +
						"Tally -> contato visual, " +
						"No joy -> sem contato visual, " +
						"In the pipe -> míssil em voo, " +
						"Winchester -> sem munição, " +
						"Anchor at 3000 -> órbita a 3000 pés, " +
						"20 miles -> 20 milhas náuticas, " +
						"Bullseye -> ponto de referência, " +
						"Bulls -> ponto de referência, " +
						"Bulls 270/20 -> ponto de referência 270° 20 milhas, " +
						"Feet dry -> sobre terra, " +
						"Feet wet -> sobre água, " +
						"Fence In -> entrar em estado de combate, " +
						"Fence Out -> sair do estado de combate, " +
						"Tapes On -> iniciar gravação, " +
						"Tapes Off -> parar gravação ]";
			
			case "ru":
				return ". Примеры перевода военного сленга: [ " +
						"Hot -> на нас/в боевой готовности, " +
						"Cold -> удаляется/нет контакта, " +
						"is cold -> нейтрализован, " +
						"Break left -> резкий левый поворот, " +
						"Break right -> резкий правый поворот, " +
						"Bandit -> вражеский самолёт (подтверждён), " +
						"Bogey -> неопознанная цель, " +
						"Splash one -> один сбит, " +
						"Splash two -> два сбито, " +
						"Angels 25 -> высота 25000 футов, " +
						"FL150 -> эшелон 150, " +
						"Tally -> визуальный контакт, " +
						"No joy -> визуального контакта нет, " +
						"In the pipe -> ракета в полёте, " +
						"Winchester -> боекомплект израсходован, " +
						"Anchor at 3000 -> барражирование на 3000 футов, " +
						"20 miles -> 20 морских миль, " +
						"Bullseye -> опорная точка, " +
						"Bulls -> опорная точка, " +
						"Bulls 270/20 -> опорная точка 270° 20 морских миль, " +
						"Feet dry -> над сушей, " +
						"Feet wet -> над водой, " +
						"Fence In -> вход в боевой режим, " +
						"Fence Out -> выход из боевого режима, " +
						"Tapes On -> запись включена, " +
						"Tapes Off -> запись выключена ]";
			
			default:
				return ". Military slang translation examples: [ " +
						"Hot -> head-on/engaging, " +
						"Cold -> moving away/disengaged, " +
						"is cold -> neutralized, " +
						"Break left -> break left, " +
						"Break right -> break right, " +
						"Bandit -> confirmed enemy, " +
						"Bogey -> unidentified contact, " +
						"Splash one -> one kill, " +
						"Splash two -> two kills, " +
						"Angels 25 -> altitude 25000 feet, " +
						"FL150 -> flight level 150, " +
						"Tally -> visual contact, " +
						"No joy -> no visual contact, " +
						"In the pipe -> missile in flight, " +
						"Winchester -> out of ammo, " +
						"Anchor at 3000 -> orbit at 3000 feet, " +
						"20 miles -> 20 nautical miles, " +
						"Bullseye -> reference point, " +
						"Bulls -> reference point, " +
						"Bulls 270/20 -> reference point 270° 20 nm, " +
						"Feet dry -> over land, " +
						"Feet dry -> over land, " +
						"Feet wet -> over water, " +
						"Fence In -> enter combat state, " +
						"Fence Out -> exit combat state, " +
						"Tapes On -> start recording, " +
						"Tapes Off -> stop recording ]";
		}
	}
	
	private static String radarModels() {
		return "Guideline, Goa, Gammon, Grumble, Gainful, Gecko, Gadfly, Grison, Flat Face, Spoon Rest, Low Blow, Fan Song, Land Roll, Snow Drift, Gun Dish, Hot Shot, Dog Ear, Grouse";
	}
	
	public static boolean containsTranslatedLanguage(Configure configure, String content) {
		var locale = configure.getLanguageCode();
		for (int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);
			switch (locale) {
				case "zh": // Chinese
					if (ch >= '\u4e00' && ch <= '\u9fa5') return true;
					break;
				case "ja": // Japanese
					if ((ch >= '\u3040' && ch <= '\u30ff') || // Hiragana & Katakana
							(ch >= '\u4e00' && ch <= '\u9faf')) // Kanji
						return true;
					break;
				case "ko": // Korean
					if (ch >= '\uac00' && ch <= '\ud7af') return true;
					break;
				case "uk": // Ukrainian
					if (ch >= '\u0400' && ch <= '\u04FF') return true;
					break;
				case "ru": // Russian
					if (ch >= '\u0400' && ch <= '\u04FF') return true;
					break;
				case "he": // Hebrew
					if (ch >= '\u0590' && ch <= '\u05FF') return true;
					break;
				case "ar": // Arabic
					if ((ch >= '\u0600' && ch <= '\u06FF') || (ch >= '\u0750' && ch <= '\u077F')) return true;
					break;
				default:
					if (ch >= '\u00C0' && ch <= '\u017F') return true;
			}
		}
		return false;
	}
	
	public static void addNouns(String text, Set<String> nounsSet) {
		String nouns = retrieveProperNouns(text);
		if (nouns != null) {
			nounsSet.add(nouns.toUpperCase());
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
