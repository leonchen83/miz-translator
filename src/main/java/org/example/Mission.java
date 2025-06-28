package org.example;

import static java.util.zip.Deflater.NO_COMPRESSION;
import static org.apache.commons.lang3.StringUtils.isAllUpperCase;
import static org.example.AsciiConverter.convertToAscii;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Baoyi Chen
 */
public class Mission implements AutoCloseable {
	
	static Logger logger = LoggerFactory.getLogger(Mission.class);
	
	private static ObjectMapper mapper = new ObjectMapper();
	private static final int BATCH_SIZE = 48;
	
	static {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	private final Map<String, String> translatedMap = new LinkedHashMap<>(4096);
	
	private static Globals globals = JsePlatform.standardGlobals();
	private Translator translator;
	private Configure configure;
	private File folder;
	private String translatedMapFileName = "translated_map";
	
	public Mission(Configure configure, File folder) {
		this.configure = configure;
		this.folder = folder;
		logger.info("configure: {}", configure);
		this.translator = new Translators(configure).getTranslator();
		this.translator.start();
		loadTranslatedMap();
	}
	
	public void loadTranslatedMap() {
		Path path = folder.toPath().resolve(translatedMapFileName + "." + configure.getLanguageCode() + ".json");
		if (Files.exists(path)) {
			try {
				translatedMap.putAll(mapper.readValue(path.toFile(), new TypeReference<>() {}));
				logger.info("Loaded translated map from {}", path);
			} catch (IOException e) {
				logger.error("Failed to load translated map from {}", path, e);
			}
		} else {
			logger.warn("No translated map found at {}", path);
		}
	}
	
	public void saveTranslatedMap() {
		Path path = folder.toPath().resolve(translatedMapFileName + "." + configure.getLanguageCode() + ".json");
		try {
			mapper.writeValue(path.toFile(), translatedMap);
		} catch (IOException e) {
			logger.error("Failed to save translated map to {}", path, e);
		}
	}
	
	public void convertMizToJson(File file) throws Exception {
		System.out.println("decompress : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		var map = parseText(tempDir);
		saveToJson(map, file.getName(), file.toPath().getParent());
		deleteDirectory(tempDir);
	}
	
	public void convertJsonToChinese(File file) throws Exception {
		System.out.println("translating : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		Path json = file.toPath().getParent().resolve(file.getName() + ".json");
		Map<String, String> map = readToMap(json);
		map = translate(map);
		saveToJson(map, file.getName(), file.toPath().getParent());
		deleteDirectory(tempDir);
	}
	
	public void convertChineseToMiz(File file) throws Exception {
		System.out.println("compressing : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		Path json = file.toPath().getParent().resolve(file.getName() + ".json");
		Map<String, String> map = readToMap(json);
		saveToFile(map, tempDir);
		Path dest = file.toPath().getParent().resolve(file.getName());
		zip(tempDir, dest);
		deleteDirectory(tempDir);
	}
	
	private Map<String, String> readToMap(Path path) {
		try {
			return mapper.readValue(path.toFile(), new TypeReference<>() {
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void saveToJson(Map<String, String> map, String name, Path path) {
		try {
			mapper.writeValue(path.resolve(name + ".json").toFile(), map);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String readFile(Path filePath) throws IOException {
		StringBuilder content = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				content.append(line).append("\n");
			}
		}
		return content.toString();
	}
	
	public Map<String, String> parseText(Path dir) throws IOException {
		dir = dir.resolve("l10n").resolve("DEFAULT").resolve("dictionary");
		String luaScript = readFile(dir);
		
		LuaValue chunk = globals.load(luaScript);
		chunk.call();
		
		LuaValue dictionary = globals.get("dictionary");
		
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
	
	public Map<String, String> translate(Map<String, String> map) {
		List<Map.Entry<String, String>> entries = new ArrayList<>(BATCH_SIZE);
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
			
			if (containsChineseOrJapaneseOrKorean(value)) {
				continue;
			}
			
			if (isAllNumber(value)) {
				continue;
			}
			
			if (isAllUpperCase(value)) {
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
				translates(entry, entries, r);
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
			if (containsChineseOrJapaneseOrKorean(raw)) {
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
			if (entries.size() >= BATCH_SIZE) {
				list.addAll(translator.translates(entries, translatedMap));
				entries.clear();
				saveTranslatedMap();
			}
		}
	}
	
	public static boolean isLikelyLua(String code) {
		return code.matches("(?s).*\\b(function|local|if|then|end|return|print|for|while|do)\\b.*") || code.matches("(?s).*[=(){};].*");
	}
	
	public void saveToFile(Map<String, String> map, Path tempDir) throws IOException {
		String country = configure.getCountryCode();
		Path countryPath = tempDir.resolve("l10n").resolve(country).resolve("dictionary");
		Files.createDirectories(countryPath.getParent());
		StringBuilder builder = new StringBuilder();
		builder.append("dictionary = \n{\n");
		map.forEach((key, value) -> {
			builder.append("    [\"").append(key).append("\"] = ");
			if (value == null || value.isEmpty() || value.isBlank()) {
				builder.append("\"\"");
			} else {
				value = value.replaceAll("\n", "\\\\\n").replace("\"", "\\\"");
				builder.append("\"").append(value).append("\"");
			}
			builder.append(",\n");
		});
		builder.append("}-- end of dictionary\n");
		try (FileWriter fileWriter = new FileWriter(countryPath.toFile())) {
			fileWriter.write(builder.toString());
		}
	}
	
	public void unzip(Path zip, Path destDir) throws IOException {
		try (ZipFile zipFile = new ZipFile(zip)) {
			zipFile.getEntries().asIterator().forEachRemaining(entry -> {
				Path outputPath = destDir.resolve(entry.getName());
				try {
					if (entry.isDirectory()) {
						Files.createDirectories(outputPath);
					} else {
						Files.createDirectories(outputPath.getParent());
						try (InputStream inputStream = zipFile.getInputStream(entry)) {
							Files.copy(inputStream, outputPath);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}
	
	public void zip(Path sourceDir, Path zip) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
			zos.setLevel(NO_COMPRESSION);
			Files.walk(sourceDir)
					.filter(path -> !Files.isDirectory(path))
					.forEach(path -> {
						String zipEntryName = sourceDir.relativize(path).toString().replace("\\", "/");
						try {
							ZipEntry zipEntry = new ZipEntry(zipEntryName);
							zos.putNextEntry(zipEntry);
							Files.copy(path, zos);
							zos.closeEntry();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
		}
	}
	
	public boolean isAllNumber(String str) {
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch < '0' || ch > '9') {
				return false;
			}
		}
		return true;
	}
	
	public boolean containsChineseOrJapaneseOrKorean(String str) {
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if ((ch >= '\u4e00' && ch <= '\u9fa5') || // Chinese
				(ch >= '\u3040' && ch <= '\u30ff') || // Japanese Hiragana and Katakana
				(ch >= '\uac00' && ch <= '\ud7af')) { // Korean Hangul
				return true;
			}
		}
		return false;
	}
	
	public boolean deleteDirectory(Path directory) {
		if (!Files.exists(directory)) {
			return false;
		}
		
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public void close() throws Exception {
		if (translator != null) translator.stop();
	}
}
