package org.example;

import static java.util.zip.Deflater.NO_COMPRESSION;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * @author Baoyi Chen
 */
public class Compressor {
	public static void unzip(Path zip, Path destDir) throws IOException {
		try (ZipFile zipFile = new ZipFile(zip)) {
			zipFile.getEntries().asIterator().forEachRemaining(entry -> {
				Path outputPath = destDir.resolve(entry.getName());
				try {
					if (entry.isDirectory()) {
						Files.createDirectories(outputPath);
					} else {
						Files.createDirectories(outputPath.getParent());
						try (InputStream inputStream = zipFile.getInputStream(entry)) {
							Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}
	
	public static void zip(Path sourceDir, Path zip) throws IOException {
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
}
