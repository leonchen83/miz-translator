package org.example.version;

import org.example.Strings;

import picocli.CommandLine;

/**
 * @author Baoyi Chen
 */
public class XVersionProvider implements CommandLine.IVersionProvider {
	
	@Override
	public String[] getVersion() throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("miz translator: ");
		if (Version.INSTANCE.version() != null) {
			builder.append(Version.INSTANCE.version());
		}
		builder.append("\n");
		if (!Strings.isEmpty(Version.INSTANCE.home())) {
			builder.append("home: ").append(Version.INSTANCE.home()).append("\n");
		}
		builder.append("java version: ").append(System.getProperty("java.version")).append(", ");
		builder.append("vendor: ").append(System.getProperty("java.vendor")).append("\n");
		
		// native image
		String home = System.getProperty("java.home");
		if (home != null) {
			builder.append("java home: ").append(home).append("\n");
		}
		
		// native image
		String locale = System.getProperty("user.language");
		if (locale != null) {
			builder.append("default locale: ").append(locale).append(", ");
		}
		
		builder.append("platform encoding: ").append(System.getProperty("file.encoding")).append("\n");
		builder.append("os name: ").append(System.getProperty("os.name")).append(", ");
		builder.append("version: ").append(System.getProperty("os.version")).append(", ");
		builder.append("arch: ").append(System.getProperty("os.arch"));
		return new String[]{builder.toString()};
	}
}
