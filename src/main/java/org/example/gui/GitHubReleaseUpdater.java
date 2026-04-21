package org.example.gui;

import static org.apache.commons.io.file.PathUtils.deleteDirectory;
import static org.example.Compressor.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Baoyi Chen
 */
public class GitHubReleaseUpdater {
	
	private final HttpClient client;
	private final ObjectMapper mapper = new ObjectMapper();
	
	private static final String DOWNLOAD_URL = "https://gh-proxy.org/github.com/leonchen83/miz-translator/releases/download/{version}/miz-translator-update.zip";
	private static final String LATEST_VERSION_URL = "https://gh-proxy.org/https://api.github.com/repos/leonchen83/miz-translator/releases/latest";
	
	public GitHubReleaseUpdater() {
		client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS)
//				.proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 8080)))
				.build();
	}
	
	public String latestVersion() {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(LATEST_VERSION_URL))
					.header("Accept", "application/vnd.github+json")
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			JsonNode root = mapper.readTree(response.body());
			return root.get("tag_name").asText();
		} catch (Throwable e) {
			return null;
		}
	}
	
	public void update(String version) throws IOException {
		Path tempDir = Files.createTempDirectory("DCS_TEMP_");
		Path upgrade = tempDir.resolve("miz-translator-update.zip");
		try {
			// download
			HttpRequest downloadReq = HttpRequest.newBuilder()
					.uri(URI.create(DOWNLOAD_URL.replace("{version}", version)))
					.build();
			HttpResponse<InputStream> zipResp = client.send(downloadReq, HttpResponse.BodyHandlers.ofInputStream());
			Files.copy(zipResp.body(), upgrade, StandardCopyOption.REPLACE_EXISTING);
		} catch (Throwable e) {
			System.out.println("failed to download upgrade package.");
			return;
		}
		
		try {
			String home = System.getProperty("trans.home");
			unzip(upgrade, tempDir);
			Path source = Files.list(tempDir)
					.filter(Files::isDirectory)
					.findFirst()
					.orElseThrow(() -> new IOException("invalid release structure"));
			copyFolder(source, Path.of(home));
		} finally {
			try {
				deleteDirectory(tempDir);
			} catch (IOException ignore) {
				// 
			}
		}
	}
	
	private static void copyFolder(Path source, Path target) throws IOException {
		
		Files.walkFileTree(source, new SimpleFileVisitor<>() {
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetDir = target.resolve(source.relativize(dir));
				Files.createDirectories(targetDir);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path targetFile = target.resolve(source.relativize(file));
				Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
