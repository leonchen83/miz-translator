package org.example.gui;

import static org.example.Main.step1;
import static org.example.Main.step2;
import static org.example.Main.step3;
import static org.example.Main.step4;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.example.Configure;
import org.example.Mission;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MizTranslatorApp extends Application {
	
	private TextField folderField;
	private TextField apiKeyField;
	private TextArea logArea;
	private RadioButton keepOriginalYes;
	private RadioButton keepOriginalNo;
	private Button patchBtn;
	private Button translateBtn;
	
	static final String FIWOS = "F-16C First in Weasels Over Syria";
	
	@Override
	public void start(Stage stage) {
		folderField = new TextField();
		folderField.setPrefWidth(400);
		
		Button browseBtn = new Button("Browse...");
		browseBtn.setOnAction(e -> onBrowse(stage));
		
		HBox folderBox = new HBox(10, folderField, browseBtn);
		
		folderField.textProperty().addListener((obs, oldVal, newVal) -> {
			runAsync(
					() -> validateFolder(folderField.getText()),
					result -> {
						patchBtn.setDisable(!result.valid);
						translateBtn.setDisable(result.valid);
						log(result.message);
					}
			);
		});
		
		apiKeyField = new TextField();
		apiKeyField.setPromptText("Required API Key");
		
		ToggleGroup keepOriginalGroup = new ToggleGroup();
		
		keepOriginalYes = new RadioButton("Keep original text");
		keepOriginalYes.setToggleGroup(keepOriginalGroup);
		keepOriginalYes.setSelected(true);
		
		keepOriginalNo = new RadioButton("Do not keep original");
		
		keepOriginalNo.setToggleGroup(keepOriginalGroup);
		
		HBox keepBox = new HBox(15, keepOriginalYes, keepOriginalNo);
		
		translateBtn = new Button("Translate");
		patchBtn = new Button("Patch");
		
		translateBtn.setPrefWidth(120);
		patchBtn.setPrefWidth(120);
		
		translateBtn.setOnAction(e -> {
			runAsync(
					() -> onTranslate(folderField.getText(), apiKeyField.getText(), keepOriginalYes.isSelected()),
					res -> log("Translate done")
			);
		});
		patchBtn.setOnAction(e -> {
			runAsync(
					() -> onPatch(folderField.getText(), apiKeyField.getText(), keepOriginalYes.isSelected()),
					res -> log("Patch done")
			);
		});
		
		HBox buttonBox = new HBox(20, translateBtn, patchBtn);
		
		logArea = new TextArea();
		logArea.setEditable(false);
		logArea.setPrefHeight(300);
		
		initSystemRedirect();
		
		VBox root = new VBox(15,
				new Label("MIZ Translator GUI"),
				folderBox,
				new Label("API Key (required):"),
				apiKeyField,
				keepBox,
				buttonBox,
				new Label("Log:"),
				logArea
		);
		root.setPadding(new Insets(15));
		stage.setTitle("miz-translator GUI");
		stage.setScene(new Scene(root, 650, 500));
		stage.show();
		runAsync(
				() -> validateFolder(folderField.getText()),
				result -> {
					patchBtn.setDisable(!result.valid);
					translateBtn.setDisable(result.valid);
					log(result.message);
				}
		);
	}
	
	private void onBrowse(Stage stage) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select MIZ Folder");
		File file = chooser.showDialog(stage);
		if (file != null) {
			folderField.setText(file.getAbsolutePath());
			log("Selected folder: " + file.getAbsolutePath());
		}
	}
	
	private boolean onTranslate(String folderStr, String api, boolean original) {
		log("[Translate]");
		log("Folder: " + folderStr);
		log("API Key: " + mask(api));
		var folder = new File(folderStr);
		
		Configure configure = Configure.bind();
		configure.setApiKey(api);
		configure.setOriginal(original);
		
		try (Mission mission = new Mission(configure, folder)) {
			step1(mission, folder, configure);
			step2(mission, folder, configure);
			step3(mission, folder, configure);
			step4(mission, folder, configure);
		} catch (Exception e) {
			logException(e);
		}
		return true;
	}
	
	private boolean onPatch(String folderStr, String api, boolean original) {
		log("[Patch]");
		log("Folder: " + folderStr);
		log("API Key: " + mask(api));
		
		Path targetDir = Paths.get(folderStr).toAbsolutePath().normalize();
		
		String home = System.getProperty("trans.home");
		Path sourceDir = Paths.get(home).resolve("campaigns").resolve(targetDir.getFileName().toString());
		
		if (Files.isDirectory(sourceDir)) {
			try {
				log("[Patch] applying resources...");
				
				Set<Path> files = listResourceFiles(sourceDir);
				
				Files.createDirectories(targetDir);
				
				for (Path sourceFile : files) {
					Path targetFile = targetDir.resolve(sourceFile.getFileName());
					Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
				}
				
			} catch (IOException e) {
				logException(e);
			}
		}
		
		Path path = targetDir.resolve("trans.conf");
		Configure configure = Configure.bind(path);
		configure.setApiKey(api);
		configure.setOriginal(original);
		try (Mission mission = new Mission(configure, targetDir.toFile())) {
			if (targetDir.getFileName().equals(FIWOS)) {
				step4(mission, targetDir.toFile(), configure);
			} else {
				step1(mission, targetDir.toFile(), configure);
				step2(mission, targetDir.toFile(), configure);
				step3(mission, targetDir.toFile(), configure);
				step4(mission, targetDir.toFile(), configure);
			}
		} catch (Exception e) {
			logException(e);
		}
		return true;
	}
	
	private <T> void runAsync(Supplier<T> job, Consumer<T> onSuccess) {
		Task<T> task = new Task<>() {
			@Override
			protected T call() {
				return job.get();
			}
		};
		
		task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
		task.setOnFailed(e -> log("[ERROR] " + task.getException()));
		
		new Thread(task).start();
	}
	
	private static class PathValidateResult {
		boolean valid;
		String message;
		
		PathValidateResult(boolean valid, String message) {
			this.valid = valid;
			this.message = message;
		}
	}
	
	private PathValidateResult validateFolder(String path) {
		if (path == null || path.isBlank()) {
			return new PathValidateResult(false, "Path is empty");
		}
		
		File file = new File(path);
		
		if (!file.exists()) {
			return new PathValidateResult(false, "Folder not exists");
		}
		
		if (!file.isDirectory()) {
			return new PathValidateResult(false, "Not a directory");
		}
		
		File[] mizFiles = file.listFiles((dir, name) -> name.endsWith(".miz"));
		
		if (mizFiles == null || mizFiles.length == 0) {
			return new PathValidateResult(false, "No .miz files found");
		}
		
		boolean localPatch = new File(file, "translated_map.zh.json").exists();
		
		Path dir = Paths.get(path).normalize();
		String folderName = dir.getFileName().toString();
		
		String home = System.getProperty("trans.home");
		Path p = Path.of(home).resolve("campaigns").resolve(folderName).resolve("translated_map.zh.json");
		
		if (!localPatch && !Files.exists(p) && !folderName.equals(FIWOS)) {
			return new PathValidateResult(false, "No translated_map.zh.json found (local or classpath)");
		}
		
		return new PathValidateResult(true, "Patch available");
	}
	
	private void updateValidateState(String path) {
		PathValidateResult result = validateFolder(path);
		
		patchBtn.setDisable(!result.valid);
		
		if (!result.valid) {
			log("[Invalid] " + result.message);
		} else {
			log("[Valid folder ready for patch]");
		}
	}
	
	private void logException(Exception e) {
		log("[ERROR] " + e.toString());
		
		for (StackTraceElement ste : e.getStackTrace()) {
			log("    at " + ste.toString());
		}
	}
	
	private static final int MAX_LINES = 1000;
	
	private void log(String msg) {
		Platform.runLater(() -> {
			logArea.appendText(msg + "\n");
			
			if (logArea.getParagraphs().size() > MAX_LINES) {
				logArea.deleteText(0, logArea.getText().indexOf("\n") + 1);
			}
			
			logArea.positionCaret(logArea.getText().length());
		});
	}
	
	private void initSystemRedirect() {
		
		PrintStream ps = new PrintStream(System.out) {
			
			@Override
			public void println(String x) {
				super.println(x);
				log(x);
			}
			
			@Override
			public void print(String s) {
				super.print(s);
				log(s);
			}
		};
		
		System.setOut(ps);
		System.setErr(ps);
	}
	
	private String mask(String key) {
		if (key == null || key.isEmpty()) return "(empty)";
		return key.substring(0, Math.min(4, key.length())) + "****";
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private Set<Path> listResourceFiles(Path path) throws IOException {
		Set<Path> files = new LinkedHashSet<>();
		
		try (Stream<Path> stream = Files.list(path)) {
			stream.filter(Files::isRegularFile)
					.forEach(p -> {
						files.add(p);
						log("resource file: " + p);
					});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return files;
	}
	
	private void copyResourceFile(Path sourcePath, Path targetPath) throws IOException {
		Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
	}
}