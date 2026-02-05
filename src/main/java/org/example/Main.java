package org.example;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.Callable;

import org.example.version.XVersionProvider;

import picocli.CommandLine;

/**
 * @author Baoyi Chen
 */
@CommandLine.Command(name = "trans",
		separator = " ",
		synopsisHeading = "",
		mixinStandardHelpOptions = true,
		optionListHeading = "%nOptions:%n",
		versionProvider = XVersionProvider.class,
		customSynopsis = {
				"Usage: trans [-hV] -f <folder> [-s <file>] [-dtco]"
		},
		description = "%nDescription: Translate DCS world miz mission to chinese.",
		footer = {"%nExamples:",
				"  trans -f /path/to",
				"  trans -f /path/to -d",
				"  trans -f /path/to -t",
				"  trans -f /path/to -t -o",
				"  trans -f /path/to -c"})
public class Main implements Callable<Integer> {
	
	@CommandLine.Option(names = {"-f", "--folder"}, required = true, paramLabel = "<folder>", description = "miz mission folder", type = File.class)
	private File folder;
	
	@CommandLine.Option(names = {"-d", "--decompress"}, description = {"decompress miz mission to json file."})
	private boolean decompress;
	
	@CommandLine.Option(names = {"-t", "--translate"}, description = {"translate json file"})
	private boolean translate;
	
	@CommandLine.Option(names = {"-o", "--original"}, description = {"with original language"})
	private boolean original;
	
	@CommandLine.Option(names = {"-c", "--compress"}, description = {"compress json file to miz file"})
	private boolean compress;
	
	@CommandLine.Option(names = {"-r", "--reformat"}, description = {"reformat translated file."})
	private boolean reformat;
	
	@CommandLine.Option(names = {"-s", "--setting"}, description = "trans.conf setting file", paramLabel = "<file>", type = File.class)
	private File settingFile;
	
	@Override
	public Integer call() throws Exception {
		Configure configure;
		if (settingFile != null && settingFile.exists()) {
			configure = Configure.bind(settingFile);
		} else {
			configure = Configure.bind();
		}
		configure.setOriginal(original);
		try(Mission mission = new Mission(configure, folder)) {
			if (decompress) {
				step1(mission, folder, configure);
			}
			if (translate) {
				step2(mission, folder, configure);
				step3(mission, folder, configure);
			}
			if (compress) {
				step4(mission, folder, configure);
			}
			if (reformat) {
				step1(mission, folder, configure);
				mission.reformatJsonFiles();
				return 0;
			}
			if (!decompress && !translate && !compress) {
				step1(mission, folder, configure);
				step2(mission, folder, configure);
				step3(mission, folder, configure);
				step4(mission, folder, configure);
			}
		}
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		int r = new CommandLine(new Main()).execute(args);
		if (r != 0) System.exit(r);
	}
	
	public static void step1(Mission mission, File folder, Configure configure) throws Exception {
		File[] mizFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".miz");
			}
		});
		
		if (mizFiles != null && mizFiles.length > 0) {
			for (File file : mizFiles) {
				mission.convertMizToJson(file);
			}
		} else {
			System.out.println("miz files not found");
		}
	}
	
	public static void step2(Mission mission, File folder, Configure configure) throws Exception {
		File[] mizFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".miz");
			}
		});
		
		if (mizFiles != null && mizFiles.length > 0) {
			for (File file : mizFiles) {
				mission.createProperNounsSet(file);
			}
		} else {
			System.out.println("miz files not found");
		}
	}
	
	public static void step3(Mission mission, File folder, Configure configure) throws Exception {
		File[] mizFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".miz");
			}
		});
		
		if (mizFiles != null && mizFiles.length > 0) {
			for (File file : mizFiles) {
				mission.convertJsonToChinese(file);
			}
		} else {
			System.out.println("miz files not found");
		}
	}
	
	public static void step4(Mission mission, File folder, Configure configure) throws Exception {
		File[] mizFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".miz");
			}
		});
		
		if (mizFiles != null && mizFiles.length > 0) {
			for (File file : mizFiles) {
				mission.convertChineseToMiz(file);
			}
		} else {
			System.out.println("miz files not found");
		}
	}
}
