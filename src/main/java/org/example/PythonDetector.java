package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Baoyi Chen
 */
public class PythonDetector {
	
	/**
	 * 检测当前系统 Python 可执行文件的绝对路径
	 */
	public static String detectPython() throws Exception {
		boolean win = System.getProperty("os.name").toLowerCase().contains("win");
		if (win) {
			return detectPythonWindows();
		} else {
			return detectPythonUnix();
		}
	}
	
	// ----------------- Windows -----------------
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
	
	/**
	 * 使用非交互 shell 解析 alias 并返回绝对路径
	 */
	private static String resolveAliasWithShell(String cmd) throws Exception {
		// 加载用户 shell 配置并尝试 alias
		String shellConfig = getShellConfigFile();
		String bashCmd = shellConfig == null ?
				String.format("%s", cmd) : // 没找到配置直接尝试命令
				String.format("source %s >/dev/null 2>&1; alias %s 2>/dev/null || echo %s", shellConfig, cmd, cmd);
		
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
					// 直接返回路径
					return whichAbsolute(line);
				}
			}
		}
		p.waitFor();
		return null;
	}
	
	/**
	 * 获取用户 shell 配置文件路径
	 */
	private static String getShellConfigFile() {
		String home = System.getProperty("user.home");
		String zshrc = home + "/.zshrc";
		String bashrc = home + "/.bashrc";
		
		if (new File(zshrc).exists()) return zshrc;
		if (new File(bashrc).exists()) return bashrc;
		return null;
	}
	
	/**
	 * 使用 which 获取绝对路径
	 */
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