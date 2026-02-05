package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Baoyi Chen
 */
public class PythonDetector {
	
	public static String detectPython() throws Exception {
		boolean win = System.getProperty("os.name").toLowerCase().contains("win");
		if (win) {
			return detectPythonWindows();
		} else {
			return detectPythonUnix();
		}
	}
	
	private static String detectPythonWindows() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "where python");
		pb.redirectErrorStream(true);
		Process p = pb.start();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line = reader.readLine();
			int code = p.waitFor();
			if (code != 0 || line == null || line.isEmpty()) {
				throw new RuntimeException("Cannot detect python on Windows");
			}
			return line.trim();
		}
	}
	
	private static String detectPythonUnix() throws Exception {
		String[] candidates = {"python3", "python"};
		for (String cmd : candidates) {
			String path = resolveAliasWithShell(cmd);
			if (path != null && !path.isEmpty()) {
				return path;
			}
		}
		throw new RuntimeException("Cannot detect python on Unix");
	}
	
	private static String resolveAliasWithShell(String cmd) throws Exception {
		String shellConfig = getShellConfigFile();
		String bashCmd = shellConfig == null ? String.format("%s", cmd) : String.format("source %s >/dev/null 2>&1; alias %s 2>/dev/null || echo %s", shellConfig, cmd, cmd);
		
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line = reader.readLine();
			if (line != null) {
				line = line.trim();
				// alias 情况: python='python3.11'
				if (line.contains("=")) {
					String target = line.split("=")[1].replaceAll("['\"]", "").trim();
					return whichAbsolute(target);
				} else {
					return whichAbsolute(line);
				}
			}
		}
		p.waitFor();
		return null;
	}
	
	private static String getShellConfigFile() {
		String home = System.getProperty("user.home");
		String zshrc = home + "/.zshrc";
		String bashrc = home + "/.bashrc";
		
		if (new File(zshrc).exists()) return zshrc;
		if (new File(bashrc).exists()) return bashrc;
		return null;
	}
	
	private static String whichAbsolute(String cmd) throws Exception {
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", "which " + cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line = reader.readLine();
			int code = p.waitFor();
			if (code != 0 || line == null || line.isEmpty()) {
				return null;
			}
			return line.trim();
		}
	}
}