package org.example.voice;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.Callable;

import org.example.Configure;
import org.example.version.XVersionProvider;

import picocli.CommandLine;

/**
 * @author Baoyi Chen
 */
@CommandLine.Command(name = "trans-voice",
		separator = " ",
		synopsisHeading = "",
		mixinStandardHelpOptions = true,
		optionListHeading = "%nOptions:%n",
		versionProvider = XVersionProvider.class,
		customSynopsis = {
				"Usage: trans-voice [-hV] -f <folder> [-tco]"
		},
		description = "%nDescription: Translate DCS world miz mission to chinese.",
		footer = {"%nExamples:",
				"  trans -f /path/to",
				"  trans -f /path/to -t",
				"  trans -f /path/to -c"})
public class MainVoice implements Callable<Integer> {
	
	@CommandLine.Option(names = {"-f", "--folder"}, required = true, paramLabel = "<folder>", description = "miz mission folder", type = File.class)
	private File folder;
	
	@CommandLine.Option(names = {"-t", "--translate"}, description = {"translate text to voice file"})
	private boolean translate;
	
	@CommandLine.Option(names = {"-c", "--compress"}, description = {"compress voice file to miz file"})
	private boolean compress;
	
	@Override
	public Integer call() throws Exception {
		Configure configure = Configure.bind();
		try(MissionVoice mission = new MissionVoice(configure, folder)) {
			if (translate) {
				step1(mission, folder, configure);
			}
			if (compress) {
				step2(mission, folder, configure);
			}
			if (!translate && !compress) {
				step1(mission, folder, configure);
				step2(mission, folder, configure);
			}
		}
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		int r = new CommandLine(new MainVoice()).execute(args);
		if (r != 0) System.exit(r);
	}
	
	public static void step1(MissionVoice mission, File folder, Configure configure) throws Exception {
		File[] mizFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".miz");
			}
		});
		
		if (mizFiles != null && mizFiles.length > 0) {
			for (File file : mizFiles) {
				mission.translateTextToVoice(file);
			}
		} else {
			System.out.println("miz files not found");
		}
	}
	
	public static void step2(MissionVoice mission, File folder, Configure configure) throws Exception {
		File[] mizFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".miz");
			}
		});
		
		if (mizFiles != null && mizFiles.length > 0) {
			for (File file : mizFiles) {
				mission.convertVoiceToMiz(file);
			}
		} else {
			System.out.println("miz files not found");
		}
	}
}
