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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MizTranslatorApp extends Application {
	
	private TextField folderField;
	private PasswordField apiKeyHidden;
	private TextArea logArea;
	private RadioButton keepOriginalYes;
	private RadioButton keepOriginalNo;
	private Button patchBtn;
	private Button upgradeBtn;
	private Button translateBtn;
	private GitHubReleaseUpdater updater = new GitHubReleaseUpdater();
	
	@Override
	public void start(Stage stage) {
		folderField = new TextField();
		folderField.setPrefWidth(400);
		
		Button browseBtn = new Button("浏览...");
		browseBtn.setOnAction(e -> onBrowse(stage));
		
		HBox folderBox = new HBox(10, folderField, browseBtn);
		
		folderField.textProperty().addListener((obs, oldVal, newVal) -> {
			runAsync(
					() -> validateFolder(folderField.getText()),
					result -> {
						patchBtn.setDisable(!result.valid);
						log(result.message);
					}
			);
		});
		
		TextField apiKeyVisible = new TextField();
		apiKeyHidden = new PasswordField();
		
		apiKeyVisible.setVisible(false);
		apiKeyVisible.setManaged(false);
		apiKeyHidden.setMaxWidth(400);
		apiKeyVisible.setMaxWidth(400);
		
		HBox.setHgrow(apiKeyHidden, Priority.ALWAYS);
		HBox.setHgrow(apiKeyVisible, Priority.ALWAYS);
		
		apiKeyVisible.textProperty().bindBidirectional(apiKeyHidden.textProperty());
		
		ToggleButton toggleBtn = new ToggleButton("👁");
		
		toggleBtn.setOnAction(e -> {
			boolean show = toggleBtn.isSelected();
			
			apiKeyVisible.setVisible(show);
			apiKeyVisible.setManaged(show);
			
			apiKeyHidden.setVisible(!show);
			apiKeyHidden.setManaged(!show);
		});
		
		HBox apiBox = new HBox(10, apiKeyHidden, apiKeyVisible, toggleBtn);
		
		ToggleGroup keepOriginalGroup = new ToggleGroup();
		
		keepOriginalYes = new RadioButton("保留原文");
		keepOriginalYes.setToggleGroup(keepOriginalGroup);
		
		keepOriginalNo = new RadioButton("不保留原文");
		keepOriginalNo.setToggleGroup(keepOriginalGroup);
		keepOriginalNo.setSelected(true);
		
		HBox keepBox = new HBox(15, keepOriginalYes, keepOriginalNo);
		
		translateBtn = new Button("翻译");
		patchBtn = new Button("中文补丁");
		upgradeBtn = new Button("更新版本");
		upgradeBtn.setDisable(true);
		
		translateBtn.setPrefWidth(120);
		patchBtn.setPrefWidth(120);
		upgradeBtn.setPrefWidth(120);
		
		HBox buttonBox = new HBox(20, translateBtn, patchBtn, upgradeBtn);
		
		logArea = new TextArea();
		logArea.setEditable(false);
		logArea.setPrefHeight(300);
		
		initSystemRedirect();
		
		VBox root = new VBox(15,
				new Label("MIZ 文件夹"),
				folderBox,
				new Label("API Key:"),
				apiBox,
				keepBox,
				buttonBox,
				new Label("日志:"),
				logArea
		);
		root.setPadding(new Insets(15));
		stage.setTitle("miz-translator GUI");
		stage.setScene(new Scene(root, 900, 600));
		stage.show();
		
		translateBtn.setOnAction(e -> {
			runAsync(
					() -> onTranslate(folderField.getText(), apiKeyHidden.getText(), keepOriginalYes.isSelected()),
					res -> log("Translate done")
			);
		});
		patchBtn.setOnAction(e -> {
			runAsync(
					() -> onPatch(folderField.getText(), apiKeyHidden.getText(), keepOriginalYes.isSelected()),
					res -> log("Patch done")
			);
		});
		upgradeBtn.setOnAction(e -> {
			upgradeBtn.setText("更新中...");
			upgradeBtn.setDisable(true);
			root.setDisable(true);
			runAsync(() -> {
				try {
					boolean upgrade = onUpgrade();
					if(upgrade) {
						javafx.application.Platform.runLater(() -> {
							log("Upgrade done");
							upgradeBtn.setDisable(true);
						});
					} else {
						javafx.application.Platform.runLater(() -> {
							log("Upgrade failed.");
							upgradeBtn.setDisable(false);
						});
					}
					return upgrade;
				} finally {
					javafx.application.Platform.runLater(() -> {
						upgradeBtn.setText("更新版本");
						root.setDisable(false);
					});
				}
			}, res -> {});
		});
		runAsync(
				() -> validateFolder(folderField.getText()),
				result -> {
					patchBtn.setDisable(!result.valid);
					log(result.message);
				}
		);
		
		runAsync(
				() -> validateUpgrade(),
				result -> {
					upgradeBtn.setDisable(!result.valid);
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
		if (api != null && !api.isBlank()) {
			configure.setApiKey(api);
		}
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
	
	private boolean onUpgrade() {
		String version = updater.latestVersion();
		if (version == null) {
			return false;
		}
		try {
			updater.update(version);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	private boolean onPatch(String folderStr, String api, boolean original) {
		log("[Patch]");
		log("Folder: " + folderStr);
		log("API Key: " + mask(api));
		
		Path targetDir = Path.of(folderStr).normalize();
		
		String home = System.getProperty("trans.home");
		Path sourceDir = Path.of(home).resolve("campaigns").resolve(targetDir.getFileName().toString());
		
		if (!Files.exists(sourceDir)) {
			List<String> segments = extractParentSegments(targetDir, 3);
			sourceDir = Path.of(home).resolve("campaigns");
			for (String s : segments) sourceDir = sourceDir.resolve(s);
		}
		
		if (!Files.exists(sourceDir)) {
			List<String> segments = extractParentSegments(targetDir, 4);
			sourceDir = Path.of(home).resolve("campaigns");
			for (String s : segments) sourceDir = sourceDir.resolve(s);
		}
		
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
		if (api != null && !api.isBlank()) {
			configure.setApiKey(api);
		} else {
			configure.setPatch(true);
		}
		configure.setOriginal(original);
		try (Mission mission = new Mission(configure, targetDir.toFile())) {
			step1(mission, targetDir.toFile(), configure);
			step2(mission, targetDir.toFile(), configure);
			step3(mission, targetDir.toFile(), configure);
			step4(mission, targetDir.toFile(), configure);
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
	
	private PathValidateResult validateUpgrade() {
		try {
			String latestVersion = updater.latestVersion();
			if (latestVersion == null) {
				return new PathValidateResult(false, "No Upgrade file found.");
			}
			String path = System.getProperty("trans.home");
			String localVersion = Files.readString(Path.of(path).resolve("conf").resolve(".version"));
			
			if (latestVersion.equals(localVersion)) {
				return new PathValidateResult(false, "No Upgrade file found.");
			}
			return new PathValidateResult(true, "Upgrade available");
		} catch (IOException e) {
			return new PathValidateResult(false, "No Upgrade file found.");
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
		Configure configure = Configure.bind();
		String fileName = fileName(configure);
		boolean localPatch = new File(file, fileName).exists();
		
		Path dir = Path.of(path).normalize();
		Path namePath = dir.getFileName();
		String folderName = namePath != null ? namePath.toString() : "";
		
		String home = System.getProperty("trans.home");
		Path p = Path.of(home).resolve("campaigns").resolve(folderName).resolve(fileName);
		
		// 训练任务或自带战役
		List<String> segments1 = extractParentSegments(dir, 3);
		Path p1 = Path.of(home).resolve("campaigns");
		for (String s : segments1) p1 = p1.resolve(s);
		p1 = p1.resolve(fileName);
		
		// WWII战役, SuperCarrier战役等
		List<String> segments2 = extractParentSegments(dir, 4);
		Path p2 = Path.of(home).resolve("campaigns");
		for (String s : segments2) p2 = p2.resolve(s);
		p2 = p2.resolve(fileName);
		
		boolean foundPatch = localPatch || Files.exists(p) || Files.exists(p1) || Files.exists(p2);
		
		if (!foundPatch) {
			return new PathValidateResult(false, "No " + fileName + " found (local or classpath)");
		}
		
		return new PathValidateResult(true, "Patch available");
	}
	
	private static String fileName(Configure configure) {
		String locale = configure.getLanguageCode();
		return "translated_map." + locale + ".json";
	}
	
	private static List<String> extractParentSegments(Path path, int depth) {
		List<String> r = new ArrayList<>();
		Path temp = path;
		for (int i = 0; i < depth; i++) {
			if (temp != null && temp.getFileName() != null) {
				r.add(0, temp.getFileName().toString());
			}
			temp = temp.getParent();
		}
		return r;
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
				log(x);
			}
			
			@Override
			public void print(String s) {
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