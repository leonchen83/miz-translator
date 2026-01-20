package org.example;

import static org.example.I18N.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Baoyi Chen
 */
public abstract class AbstractMission {
	protected static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	protected Translator translator;
	protected String translatedMapFileName = "translated_map";
	protected Configure configure;
	protected File folder;
	protected final Map<String, String> translatedMap = new LinkedHashMap<>(4096);
	protected static Logger logger = LoggerFactory.getLogger(Mission.class);
	
	public AbstractMission(Configure configure, File folder) {
		this.configure = configure;
		this.folder = folder;
		logger.info("configure: {}", configure);
		loadTranslatedMap();
	}
	
	protected Map<String, String> readToMap(Path path) {
		try {
			return mapper.readValue(path.toFile(), new TypeReference<>() {
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void saveToJson(Map<String, String> map, String name, Path path) {
		try {
			mapper.writeValue(path.resolve(i18n(name, "json", configure)).toFile(), map);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String readFile(Path filePath) throws IOException {
		StringBuilder content = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				content.append(line).append("\n");
			}
		}
		return content.toString();
	}
	
	protected void loadTranslatedMap() {
		Path path = folder.toPath().resolve(i18n(translatedMapFileName, "json", configure));
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
	
	protected void saveTranslatedMap() {
		Path path = folder.toPath().resolve(i18n(translatedMapFileName, "json", configure));
		try {
			mapper.writeValue(path.toFile(), translatedMap);
		} catch (IOException e) {
			logger.error("Failed to save translated map to {}", path, e);
		}
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
	
	public void saveVoiceFile(Path voice, Path tempDir) throws IOException {
		Path defaultDir = tempDir.resolve("l10n").resolve("DEFAULT");
		Files.createDirectories(defaultDir);
		Path target = defaultDir.resolve(voice.getFileName());
		Files.copy(voice, target, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void saveToVoiceFiles(Path voiceDir, Path tempDir) throws IOException {
		if (!Files.exists(voiceDir) || !Files.isDirectory(voiceDir)) {
			throw new IllegalArgumentException("voiceDir " + voiceDir + " must exist and be a directory");
		}
		
		Files.createDirectories(tempDir);
		
		Path defaultDir = tempDir.resolve("l10n").resolve("DEFAULT");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(voiceDir)) {
			for (Path file : stream) {
				if (Files.isRegularFile(file)) {
					Path target = defaultDir.resolve(file.getFileName());
					Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}
	
	protected boolean deleteDirectory(Path directory) {
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
}
