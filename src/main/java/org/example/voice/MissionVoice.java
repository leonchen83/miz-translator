package org.example.voice;

import static org.example.Compressor.unzip;
import static org.example.Compressor.zip;
import static org.example.I18N.i18n;
import static org.example.I18N.localeVoice;
import static org.example.I18N.pi18n;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.example.AbstractMission;
import org.example.Configure;

/**
 * @author Baoyi Chen
 */
public class MissionVoice extends AbstractMission implements AutoCloseable {
	
	private static final int EDGE_TTS_MAX_RETRY = 3;
	private static final long EDGE_TTS_RETRY_DELAY_MS = 3000;
	
	public MissionVoice(Configure configure, File folder) {
		super(configure, folder);
	}
	
	@Override
	public void close() throws Exception {
		
	}
	
	public void translateTextToVoice(File file) {
		System.out.println("translating voice : " + file);
		Path voiceJson = file.toPath().getParent().resolve(i18n(file.getName() + ".voice", "json", configure));
		Map<String, String> voiceMap = readToMap(voiceJson);
		for (var entry : voiceMap.entrySet()) {
			String voiceFileName = entry.getKey();
			String text = entry.getValue();
			try {
				Path outOgg = file.toPath().getParent().resolve(pi18n(file.getName(), "voice", configure));
				Files.createDirectories(outOgg);
				outOgg = outOgg.resolve(voiceFileName);
				ttsToOgg(text, localeVoice(configure), outOgg);
				logger.info("Generated voice file: {}", outOgg);
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
		Path outOgg = file.toPath().getParent().resolve(pi18n(file.getName(), "voice", configure));
		Files.createDirectories(outOgg);
		saveToVoiceFiles(outOgg, tempDir);
		
		Path dest = file.toPath().getParent().resolve(file.getName());
		zip(tempDir, dest);
		deleteDirectory(tempDir);
	}
	
	public void ttsToOgg(String text, String voice, Path outOgg) throws Exception {
		if (Files.exists(outOgg)) return;
		
		Path wav = Files.createTempFile("tts-", ".wav");
		String ttsCmd = null;
		if (configure.getEdgeTTSProxy() != null) {
			ttsCmd = String.format("edge-tts --voice %s --text \"%s\" --write-media \"%s\" --proxy \"%s\"", voice, text.replace("\"", "\\\""), wav, configure.getEdgeTTSProxy());
		} else {
			ttsCmd = String.format("edge-tts --voice %s --text \"%s\" --write-media \"%s\"", voice, text.replace("\"", "\\\""), wav);
		}
		String ffmpegCmd = String.format("ffmpeg -y -i \"%s\" -c:a libvorbis -ar 44100 -ac 1 -q:a 3 \"%s\"", wav, outOgg);
		runEdgeTtsWithRetry(ttsCmd, outOgg);
		runCmd(ffmpegCmd);
		Files.deleteIfExists(wav);
	}
	
	static int runCmd(String cmd) throws Exception {
		boolean win = System.getProperty("os.name").toLowerCase().contains("win");
		
		ProcessBuilder pb = win ? new ProcessBuilder("cmd.exe", "/c", cmd) : new ProcessBuilder("bash", "-c", cmd);
		
		pb.redirectErrorStream(true);
		pb.inheritIO();
		
		Process p = pb.start();
		int code = p.waitFor();
		
		if (code != 0) {
			throw new RuntimeException("Command failed: " + cmd);
		}
		return code;
	}
	
	private void runEdgeTtsWithRetry(String cmd, Path outOgg) throws Exception {
		int attempt = 0;
		
		while (true) {
			attempt++;
			try {
				System.out.println("translating file: "+ outOgg);
				runCmd(cmd);
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
	
}
