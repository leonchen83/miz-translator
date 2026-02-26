package org.example.voice;

import static org.example.Compressor.unzip;
import static org.example.Compressor.zip;
import static org.example.I18N.i18n;
import static org.example.I18N.localeVoice;
import static org.example.I18N.pi18n;
import static org.example.Strings.getFileExt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.AbstractMission;
import org.example.Configure;
import org.example.OSDetector;
import org.example.PythonDetector;
import org.example.RuntimeOS;
import org.example.Translators;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Baoyi Chen
 */
public class MissionVoice extends AbstractMission implements AutoCloseable {
	
	private static final int EDGE_TTS_MAX_RETRY = 3;
	private static final long EDGE_TTS_RETRY_DELAY_MS = 3000;
	
	private Map<String, String> env = new HashMap<>();
	private final RuntimeOS os = OSDetector.detect();
	
	public MissionVoice(Configure configure, File folder) {
		super(configure, folder);
		if (configure.getTtsProxy() != null) {
			String proxy = configure.getTtsProxy();
			env.put("http_proxy", proxy);
			env.put("https_proxy", proxy);
			env.put("HTTP_PROXY", proxy);
			env.put("HTTPS_PROXY", proxy);
		}
		this.translator = new Translators(configure).getTranslator();
		this.translator.start();
	}
	
	@Override
	public void close() throws Exception {
		if (this.translator != null) {
			this.translator.stop();
		}
	}
	
	public void translateVoiceToText(File file) throws Exception {
		System.out.println("translating voice to text : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		Path voiceJson = file.toPath().getParent().resolve(i18n(file.getName() + ".voice", "json", configure));
		Map<String, String> voiceMap = readToMap(voiceJson);
		List<String> files = new ArrayList<>();
		for (var entry : voiceMap.entrySet()) {
			String voiceFileName = entry.getKey();
			String text = entry.getValue();
			if (text == null || text.isBlank() || text.equals("nil")) {
				files.add(voiceFileName);
			}
		}
		Path voice = tempDir.resolve("l10n").resolve("DEFAULT");
		
		Map<String, String> json = fasterWhisperToText(files, voice, configure, env);
		
		if (json != null && !json.isEmpty()) {
			for (var entry : json.entrySet()) {
				String voiceFileName = entry.getKey();
				String text = entry.getValue();
				voiceMap.put(voiceFileName, text);
			}
		}
		saveToJson(voiceMap, file.getName() + ".voice", file.toPath().getParent());
		voiceMap = translate(voiceMap, true);
		saveToJson(voiceMap, file.getName() + ".voice", file.toPath().getParent());
		deleteDirectory(tempDir);
	}
	
	public void translateTextToVoice(File file) {
		System.out.println("translating text to voice : " + file);
		Path voiceJson = file.toPath().getParent().resolve(i18n(file.getName() + ".voice", "json", configure));
		Map<String, String> voiceMap = readToMap(voiceJson);
		
		for (var entry : voiceMap.entrySet()) {
			String voiceFileName = entry.getKey();
			String text = entry.getValue();
			if (text == null || text.isBlank() || text.equals("nil")) {
				continue;
			}
			try {
				Path sound = file.toPath().getParent().resolve(pi18n(file.getName(), "voice", configure));
				Files.createDirectories(sound);
				sound = sound.resolve(voiceFileName);
				String voice = null;
				if (configure.getVoice() != null) {
					voice = configure.getVoice();
				} else {
					voice = localeVoice(configure);
				}
				
				String ext = getFileExt(voiceFileName);
				ttsToSound(text, voice, sound, env, ext);
				logger.info("Generated voice file: {}", sound);
			} catch (Exception e) {
				logger.error("Failed to generate voice for text: {}", text, e);
			}
		}
	}
	
	public void convertVoiceToMiz(File file) throws IOException {
		System.out.println("compressing : " + file);
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		unzip(file.toPath(), tempDir);
		Path json = file.toPath().getParent().resolve(i18n(file.getName(), "json", configure));
		Map<String, String> map = readToMap(json);
		saveToFile(map, tempDir);
		Path sound = file.toPath().getParent().resolve(pi18n(file.getName(), "voice", configure));
		Files.createDirectories(sound);
		saveToVoiceFiles(sound, tempDir);
		
		Path dest = file.toPath().getParent().resolve(file.getName());
		zip(tempDir, dest);
		deleteDirectory(tempDir);
	}
	
	public void ttsToSound(String text, String voice, Path out, Map<String, String> env, String ext) throws Exception {
		if (Files.exists(out)) return;
		Path wav = Files.createTempFile("tts-", ".wav");
		String ttsCmd = ttsCmd(text, voice, wav, configure);
		runEdgeTtsWithRetry(ttsCmd, out, env);
		if (ext.equals("ogg")) {
			String ffmpegCmd = null;
			if (os == RuntimeOS.MAC) {
				ffmpegCmd = String.format("ffmpeg -y -i \"%s\" -c:a vorbis -ar 44100 -ac 2 -q:a 3 -strict -2 \"%s\"", wav, out);
			} else {
				ffmpegCmd = String.format("ffmpeg -y -i \"%s\" -c:a libvorbis -ar 44100 -ac 1 -q:a 3 \"%s\"", wav, out);
			}
			runCmd(ffmpegCmd, env);
		} else if (ext.equals("wav")) {
			Files.copy(wav, out);
		} else {
			throw new IllegalArgumentException("Unsupported audio format: " + ext);
		}
		Files.deleteIfExists(wav);
	}
	
	private void runEdgeTtsWithRetry(String cmd, Path outOgg, Map<String, String> env) throws Exception {
		int attempt = 0;
		
		while (true) {
			attempt++;
			try {
				System.out.println("translating file: "+ outOgg);
				runCmd(cmd, env);
				return;
			} catch (Exception e) {
				if (attempt >= EDGE_TTS_MAX_RETRY) {
					System.out.println("failed translating file: "+ outOgg);
					throw e;
				}
				Thread.sleep(EDGE_TTS_RETRY_DELAY_MS);
			}
		}
	}
	
	private String ttsCmd(String text, String voice, Path wav, Configure configure) {
		String ttsService = configure.getTtsService();
		text = sanitizeForTTS(text);
		text = text.replace("\"", "\\\"");
		if (ttsService == null || ttsService.equals("edge-tts")) {
			if (configure.getTtsProxy() != null) {
				return String.format("edge-tts --voice %s --text \"%s\" --write-media \"%s\" --proxy \"%s\"", voice, text, wav, configure.getTtsProxy());
			} else {
				return String.format("edge-tts --voice %s --text \"%s\" --write-media \"%s\"", voice, text, wav);
			}
		} else {
			throw new IllegalArgumentException("Unsupported TTS service: " + ttsService);
		}
	}
	
	private Map<String, String> fasterWhisperToText(List<String> files, Path audioDir, Configure configure, Map<String, String> env) throws Exception {
		if (files == null || files.isEmpty()) {
			return Map.of();
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		Path listFile = Files.createTempFile("whisper-files-", ".json");
		Path outFile  = Files.createTempFile("whisper-out-", ".json");
		
		Files.writeString(listFile, mapper.writeValueAsString(files), StandardCharsets.UTF_8);
		
		String confPath = System.getProperty("conf");
		if (confPath == null) {
			throw new IllegalStateException("System property 'conf' is not set");
		}
		
		Path confDir = Paths.get(confPath).toAbsolutePath().getParent();
		Path pyScript = confDir.resolve("batch_whisper_list.py");
		
		if (!Files.exists(pyScript)) {
			throw new FileNotFoundException("batch_whisper_list.py not found: " + pyScript);
		}
		
		String python = PythonDetector.detectPython();
		String cmd = String.format("%s \"%s\" \"%s\" \"%s\" \"%s\"", python, pyScript.toAbsolutePath(), audioDir, listFile, outFile);
		runCmd(cmd, env);
		
		var result = readToMap(outFile);
		
		Files.deleteIfExists(listFile);
		Files.deleteIfExists(outFile);
		
		return result;
	}
	
	private int runCmd(String cmd, Map<String, String> env) throws Exception {
		
		ProcessBuilder pb = os == RuntimeOS.WINDOWS ? new ProcessBuilder("cmd.exe", "/c", cmd) : new ProcessBuilder("bash", "-c", cmd);
		
		if (env != null) {
			pb.environment().putAll(env);
		}
		
		pb.redirectErrorStream(true);
		pb.inheritIO();
		
		Process p = pb.start();
		int code = p.waitFor();
		
		if (code != 0) {
			throw new RuntimeException("Command failed: " + cmd);
		}
		return code;
	}
	
	private String readSrtText(Path srt) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		try (BufferedReader reader = Files.newBufferedReader(srt, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				if (line.isEmpty()) {
					continue;
				}
				
				if (line.matches("\\d+")) {
					continue;
				}
				
				if (line.contains("-->")) {
					continue;
				}
				
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(line);
			}
		}
		
		return sb.toString();
	}
	
	private static String sanitizeForTTS(String s) {
		if (s == null) return "";
		return s.replaceAll("[*_`~#|\\[\\]]", " ").replaceAll("\\s+", " ").trim();
	}
}
