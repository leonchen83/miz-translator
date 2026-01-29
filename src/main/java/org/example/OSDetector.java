package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * @author Baoyi Chen
 */
public final class OSDetector {
	
	private OSDetector() {}
	
	public static RuntimeOS detect() {
		String osName = System.getProperty("os.name", "unknown")
				.toLowerCase(Locale.ROOT);
		
		if (osName.contains("win")) {
			return RuntimeOS.WINDOWS;
		}
		
		if (osName.contains("mac") || osName.contains("darwin")) {
			return RuntimeOS.MAC;
		}
		
		if (osName.contains("nux") || osName.contains("linux")) {
			if (isWSL()) {
				return RuntimeOS.WSL;
			}
			return RuntimeOS.LINUX;
		}
		
		return RuntimeOS.UNKNOWN;
	}
	
	/**
	 * WSL1 / WSL2 检测
	 */
	private static boolean isWSL() {
		if (System.getenv("WSL_DISTRO_NAME") != null) {
			return true;
		}
		
		try {
			String version = Files.readString(Path.of("/proc/version"))
					.toLowerCase(Locale.ROOT);
			if (version.contains("microsoft")) {
				return true;
			}
		} catch (IOException ignored) {
		}
		
		try {
			String osrelease = Files.readString(
							Path.of("/proc/sys/kernel/osrelease"))
					.toLowerCase(Locale.ROOT);
			if (osrelease.contains("microsoft")) {
				return true;
			}
		} catch (IOException ignored) {
		}
		
		return false;
	}
}