package org.example;

import static org.example.Compressor.unzip;
import static org.example.Compressor.zip;
import static org.example.I18N.addNouns;
import static org.example.I18N.containsTranslatedLanguage;
import static org.example.I18N.i18n;
import static org.example.Strings.containsLowerCase;
import static org.example.Strings.convertToAscii;
import static org.example.Strings.isAllNumber;
import static org.example.Strings.isLikelyLua;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * @author Baoyi Chen
 */
public class Mission extends AbstractMission implements AutoCloseable {
	
	private final Set<String> nounsSet = new HashSet<>(256);
	private static final Pattern PATTERN = Pattern.compile("getValueDictByKey\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");
	
	private static Globals globals = JsePlatform.standardGlobals();
	private Translator translator;
	
	public Mission(Configure configure, File folder) {
		super(configure, folder);
		this.translator = new Translators(configure, nounsSet).getTranslator();
		this.translator.start();
	}
	
	public void convertMizToJson(File file) throws Exception {
		System.out.println("decompress : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		var map = parseText(tempDir, "dictionary");
		saveToJson(map, file.getName(), file.toPath().getParent());
		
		var voice = parseMission(tempDir, parseText(tempDir, "mapResource"));
		saveToJson(voice, file.getName() + ".voice", file.toPath().getParent());
		deleteDirectory(tempDir);
	}
	
	public void createProperNounsSet(File file) throws Exception {
		Path json = file.toPath().getParent().resolve(i18n(file.getName(), "json", configure));
		Map<String, String> map = readToMap(json);
		retrieveProperNounsSet(map, nounsSet);
	}
	
	public void convertJsonToChinese(File file) throws Exception {
		System.out.println("translating : " + file);
		Path json = file.toPath().getParent().resolve(i18n(file.getName(), "json", configure));
		Map<String, String> map = readToMap(json);
		map = translate(map);
		saveToJson(map, file.getName(), file.toPath().getParent());
		
		// voice
		Path voiceJson = file.toPath().getParent().resolve(i18n(file.getName() + ".voice", "json", configure));
		Map<String, String> voiceMap = readToMap(voiceJson);
		for (var entry : voiceMap.entrySet()) {
			String value = entry.getValue();
			if (map.containsKey(value)) {
				entry.setValue(map.get(value));
			}
		}
		saveToJson(voiceMap, file.getName() + ".voice", file.toPath().getParent());
	}
	
	public void convertChineseToMiz(File file) throws Exception {
		System.out.println("compressing : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		Path json = file.toPath().getParent().resolve(i18n(file.getName(), "json", configure));
		Map<String, String> map = readToMap(json);
		saveToFile(map, tempDir);
		Path dest = file.toPath().getParent().resolve(file.getName());
		zip(tempDir, dest);
		deleteDirectory(tempDir);
	}
	
	public Map<String, String> parseText(Path dir, String name) throws IOException {
		dir = dir.resolve("l10n").resolve("DEFAULT").resolve(name);
		String luaScript = readFile(dir);
		
		LuaValue chunk = globals.load(luaScript);
		chunk.call();
		
		LuaValue dictionary = globals.get(name);
		
		Map<String, String> map = new LinkedHashMap<>();
		
		LuaValue key = LuaValue.NIL;
		while (true) {
			Varargs next = dictionary.next(key);
			key = next.arg1();
			LuaValue value = next.arg(2);
			if (key.isnil()) break;
			map.put(key.tojstring(), value.tojstring());
		}
		
		return map;
	}
	
	public Map<String, String> parseMission(Path dir, Map<String, String> resource) throws IOException {
		dir = dir.resolve("mission");
		String luaScript = readFile(dir);
		
		LuaValue chunk = globals.load(luaScript);
		chunk.call();
		LuaValue dictionary = globals.get("mission");
		Map<String, String> map = new LinkedHashMap<>();
		parseMission(dictionary, resource, map);
		return map;
	}
	
	public void parseMission(LuaValue lua, Map<String, String> resource, Map<String, String> out) {
		try {
			LuaValue key = LuaValue.NIL;
			while (true) {
				Varargs next = lua.next(key);
				key = next.arg1();
				LuaValue value = next.arg(2);
				if (key.tojstring().equals("actions")) {
					parseAction(value, resource, out);
				} else if (value.type() == 5) {
					parseMission(value, resource, out);
				}
				if (key.isnil()) break;
			}
		} catch (Throwable ignore) {
			// do nothing
		}
	}
	
	public void parseAction(LuaValue lua, Map<String, String> resource, Map<String, String> out) {
		try {
			if (lua.type() == 5) {
				String textValue = null;
				String textKey = null;
				LuaValue k = LuaValue.NIL;
				while (true) {
					Varargs next = lua.next(k);
					k = next.arg1();
					LuaValue v = next.arg(2);
					if (k.isnil()) break;
					if (v.type() == 4/*String*/) {
						String value = v.tojstring();
						for (var entry : resource.entrySet()) {
							if (value.contains(entry.getKey()) && !out.containsKey(entry.getValue())) {
								Matcher m = PATTERN.matcher(value);
								if (!m.find()) continue;
								value = m.group(1);
								out.put(entry.getValue(), value);
							}
						}
					} else if (v.type() == 5 /*Table*/) {
						if (v.get("predicate").tojstring().equals("a_out_text_delay_u")) {
							textValue = v.get("text").tojstring();
						} else if (v.get("predicate").tojstring().equals("a_out_sound_u")) {
							textKey = v.get("file").tojstring();
						} else {
							textKey = v.get("file").tojstring();
							textValue = v.get("subtitle").tojstring();
						}
					}
				}
				if (textKey != null && textValue != null && resource.containsKey(textKey)) {
					textKey = resource.get(textKey);
					if (!out.containsKey(textKey)) {
						out.put(textKey, textValue);
					}
				}
			}
		} catch (Throwable ignore) {
			// do nothing
		}
	}
	
	public void retrieveProperNounsSet(Map<String, String> map, Set<String> nounsSet) {
		loop:
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || value.isEmpty() || value.isBlank()) {
				continue;
			}
			
			if (value.length() < configure.getMinimumLength()) {
				continue;
			}
			
			if (containsTranslatedLanguage(configure, value)) {
				continue;
			}
			
			if (isAllNumber(value)) {
				continue;
			}
			
			if (!containsLowerCase(value)) {
				continue;
			}
			
			if (value.equals(key)) {
				continue;
			}
			
			for (String filter : configure.getFilters()) {
				if (filter != null && (value.startsWith(filter) || value.endsWith(filter))) {
					continue loop;
				}
			}
			
			for (String keyFilter : configure.getKeyFilters()) {
				if (keyFilter != null && key.startsWith(keyFilter)) {
					continue loop;
				}
			}
			
			for (Map.Entry<String, String> e : configure.getFixed().entrySet()) {
				if (value.equals(e.getKey())) {
					entry.setValue(e.getValue());
					continue loop;
				}
			}
			
			if (isLikelyLua(value)) {
				try {
					globals.load(value);
					continue;
				} catch (LuaError error) {
					// do nothing;
				}
			}
			
			value = convertToAscii(value);
			boolean shouldParseNouns = false;
			if (key.startsWith("DictKey_ActionText_")) {
				shouldParseNouns = true;
			} else if (key.startsWith("DictKey_descriptionText_")) {
			} else if (key.startsWith("DictKey_sortie_")) {
				shouldParseNouns = true;
			} else if (key.startsWith("DictKey_descriptionRedTask_")) {
			} else if (key.startsWith("DictKey_descriptionBlueTask_")) {
			} else if (key.startsWith("DictKey_descriptionNeutralsTask_")) {
			} else if (key.startsWith("DictKey_subtitle_")) {
				shouldParseNouns = true;
			} else if (key.startsWith("DictKey_ActionRadioText_")) {
			} else if (key.startsWith("DictKey_")) {
				try {
					Long.parseLong(key.substring("DictKey_".length()));
					shouldParseNouns = true;
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
			if (shouldParseNouns) {
				addNouns(value, nounsSet);
			}
		}
	}
	
	public Map<String, String> translate(Map<String, String> map) {
		List<Map.Entry<String, String>> entries = new ArrayList<>(configure.getBatchSize());
		List<Map.Entry<String, String>> r = new ArrayList<>(map.size());
		loop:
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || value.isEmpty() || value.isBlank()) {
				continue;
			}
			
			if (value.length() < configure.getMinimumLength()) {
				continue;
			}
			
			if (containsTranslatedLanguage(configure, value)) {
				continue;
			}
			
			if (isAllNumber(value)) {
				continue;
			}
			
			if (!containsLowerCase(value)) {
				continue;
			}
			
			if (value.equals(key)) {
				continue;
			}
			
			for (String filter : configure.getFilters()) {
				if (filter != null && (value.startsWith(filter) || value.endsWith(filter))) {
					continue loop;
				}
			}
			
			for (String keyFilter : configure.getKeyFilters()) {
				if (keyFilter != null && key.startsWith(keyFilter)) {
					continue loop;
				}
			}
			
			for (Map.Entry<String, String> e : configure.getFixed().entrySet()) {
				if (value.equals(e.getKey())) {
					entry.setValue(e.getValue());
					continue loop;
				}
			}
			
			if (isLikelyLua(value)) {
				try {
					globals.load(value);
					continue;
				} catch (LuaError error) {
					// do nothing;
				}
			}
			
			value = convertToAscii(value);
			// formatted value
			entry.setValue(value);
			boolean needTranslate = false;
			if (key.startsWith("DictKey_ActionText_")) {
				needTranslate = true;
			} else if (key.startsWith("DictKey_descriptionText_")) {
				if (translatedMap.containsKey(value)) {
					entry.setValue(translatedMap.get(value));
				} else {
					entry.setValue(translator.translate(value, translatedMap));
				}
			} else if (key.startsWith("DictKey_sortie_")) {
				needTranslate = true;
			} else if (key.startsWith("DictKey_descriptionRedTask_")) {
				if (translatedMap.containsKey(value)) {
					entry.setValue(translatedMap.get(value));
				} else {
					entry.setValue(translator.translate(value, translatedMap));
				}
			} else if (key.startsWith("DictKey_descriptionBlueTask_")) {
				if (translatedMap.containsKey(value)) {
					entry.setValue(translatedMap.get(value));
				} else {
					entry.setValue(translator.translate(value, translatedMap));
				}
			} else if (key.startsWith("DictKey_descriptionNeutralsTask_")) {
				if (translatedMap.containsKey(value)) {
					entry.setValue(translatedMap.get(value));
				} else {
					entry.setValue(translator.translate(value, translatedMap));
				}
			} else if (key.startsWith("DictKey_subtitle_")) {
				needTranslate = true;
			} else if (key.startsWith("DictKey_ActionRadioText_")) {
				needTranslate = true;
			} else if (key.startsWith("DictKey_")) {
				try {
					Long.parseLong(key.substring("DictKey_".length()));
					needTranslate = true;
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
			
			if (needTranslate) {
				if (value.length() > 1024) {
					if (translatedMap.containsKey(value)) {
						entry.setValue(translatedMap.get(value));
					} else {
						entry.setValue(translator.translate(value, translatedMap));
					}
				} else {
					translates(entry, entries, r);
				}
			}
		}
		
		if (!entries.isEmpty()) {
			r.addAll(translator.translates(entries, translatedMap));
			entries.clear();
		}
		
		// merge the results back into the original map
		for (Map.Entry<String, String> entry : r) {
			String key = entry.getKey();
			String value = entry.getValue();
			String raw = map.get(key);
			if (containsTranslatedLanguage(configure, raw)) {
				// already translated, skip
				continue;
			}
			if (configure.getOriginal() && raw.length() <= 1024) {
				map.replace(key, raw + "\n" + value);
			} else {
				map.replace(key, value);
			}
		}
		saveTranslatedMap();
		return map;
	}
	
	private void translates(Map.Entry<String, String> entry, List<Map.Entry<String, String>> entries, List<Map.Entry<String, String>> list) {
		String key = entry.getKey();
		String value = entry.getValue();
		if (translatedMap.containsKey(value)) {
			if (configure.getOriginal() && value.length() <= 1024) {
				entry.setValue(value + "\n" + translatedMap.get(value));
			} else {
				entry.setValue(translatedMap.get(value));
			}
		} else {
			entries.add(Map.entry(key, value));
			if (entries.size() >= configure.getBatchSize()) {
				list.addAll(translator.translates(entries, translatedMap));
				entries.clear();
				saveTranslatedMap();
			}
		}
	}
	
	@Override
	public void close() throws Exception {
		if (translator != null) translator.stop();
	}
	
	public void reformatJsonFiles() {
		Set<String> nounsSet = new HashSet<>(256);
		for (var entry : translatedMap.entrySet()) {
			addNouns(entry.getKey(), nounsSet);
		}
		logger.info("nouns set: {}", nounsSet);
		for (var entry : translatedMap.entrySet()) {
			var value = entry.getValue();
			if (value.contains("PLAYER")) {
				value = value.replace("PLAYER", "玩家");
			}
			if (value.length() > 1024) {
				value = replaceUpperCaseToCapital(value, nounsSet);
			} else {
				int index = value.indexOf(':');
				if (index <= 0) {
					index = value.indexOf('：');
				}
				if (index <= 0) {
					value = replaceUpperCaseToCapital(value, nounsSet);
				} else {
					String prefix = value.substring(0, index + 1);
					String secondPart = value.substring(index + 1);
					secondPart = replaceUpperCaseToCapital(secondPart, nounsSet);
					value = prefix + secondPart;
				}
			}
			entry.setValue(value);
		}
		saveTranslatedMap();
	}
	
	private String replaceUpperCaseToCapital(String value, Set<String> nounsSet) {
		for (var nouns : nounsSet) {
			String upperNouns = nouns.toUpperCase();
			if (value.contains(upperNouns)) {
				value = value.replaceAll(upperNouns, nouns);
			}
		}
		return value;
	}
}
