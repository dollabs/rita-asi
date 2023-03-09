package utils;

import java.io.File;
import java.net.URL;
import java.util.*;

import org.apache.commons.io.FileUtils;

import genesis.Genesis;

/*
 * Created on Aug 2, 2012
 * @author phw
 */

public class Webstart {

	private static boolean webstart = false;

	public static boolean isWebStart() {
		return webstart;
	}

	public static void setWebStart(boolean b) {
		if (b == true) {
			Mark.say("Appear to be running in WebStart mode");
			return;
		}
		Mark.say("Appear to be running in development mode");
		webstart = b;
	}

	public static List<File> getTextFiles() {
		if (!isWebStart()) {
			// Ok, in that case, let's find the txt files
			URL url = Webstart.class.getResource("Anchor.txt");
			Mark.say(false, "Url is", url, url.getPath());
			File file = FileUtils.toFile(url);
			Mark.say(false, "File is", file.isFile(), file.exists(), file);
			File genesisSource = getGenesis(file);
			
			Mark.say(false, "Genesis is directory", genesisSource.isDirectory(), genesisSource);
//			ArrayList<File> textFiles = getTextDirectories(new File(genesisSource+"/GenesisCore")); // for RITA
			ArrayList<File> textFiles = getTextDirectories(genesisSource); // normal Genesis
			Mark.say(false, "File count", textFiles.size());
			return textFiles;
		}
		return null;
	}

	/**
	 * @param genesisSource
	 * @return
	 */
	private static ArrayList<File> getTextDirectories(File genesisSource) {
		ArrayList<File> result = new ArrayList<>();
		Mark.say(false, "Directory?", genesisSource.isDirectory());
		for (File file : genesisSource.listFiles()) {
			if (file.getName().equalsIgnoreCase("Students")) {
				Mark.say(false, "Top file is", file.getName());
				augmentResult(result, file);
			}
		}
		for (File file : genesisSource.listFiles()) {
			if (file.getName().equalsIgnoreCase("Corpora")) {
				Mark.say(false, "Top file is", file.getName());
				augmentResult(result, file);
			}
		}
		for (File file : genesisSource.listFiles()) {
			if (file.getName().equalsIgnoreCase("Source")) {
				Mark.say(false, "Top file is", file.getName());
				augmentResult(result, file);
			}
		}
		return result;
	}

	/**
	 * @param result
	 * @param file
	 */
	private static void augmentResult(ArrayList<File> result, File file) {
		if (file.isFile()) {
			if (file.getName().endsWith(".txt")) {
				Mark.say(false, "File name is", file.getName());
				result.add(file);
			}
		}
		else if (file.isDirectory()) {
			for (File directory : file.listFiles()) {
				augmentResult(result, directory);
			}
		}
		else {
			Mark.err("Ooops, argument is not a file or directory");
		}

	}

	/**
	 * @param file
	 * @return
	 */
	private static File getGenesis(File file) {
		File parent = file.getParentFile();
//		Mark.say(parent);
//		Mark.say(true, "Parent directory/file", parent.isDirectory(), parent.isFile());
		if (parent == null) {
			Mark.err("Could not find Genesis directory!");
			return null;
		}
		String name = parent.getName();
		Mark.say(false, "Parent", parent.getName());
		
		// for RITA system, added on 1 June 2020
		if (name.equalsIgnoreCase("genesis-components") || name.equalsIgnoreCase("GenesisCore")) {
			return parent;
		}
		else {
			return getGenesis(parent);
		}
	}

	// For development of file finding only
	public static void main(String[] args) {
		Genesis.main(args);
	}

	public static String readTextFile(String fileName) {
		List<File> files = getTextFile(fileName);
		if (files.size() == 0) {
			Mark.err("Ooops, could not find a file named", fileName, "to read");
			return "";
		}
		else if (files.size() > 1) {
			Mark.err("Ooops, more than one file, reading from the first:", files.get(0));
		}
		try {
			return TextIO.readStringFromFile(files.get(0));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return "";
		}
	}

	/**
	 * @param string
	 */
	public static List<File> getTextFile(String fileName) {
		List<File> files = new ArrayList<>();
		// Better end in .txt
		if (!fileName.endsWith(".txt")) {
			Mark.say(false, "Adding .txt extension");
			fileName += ".txt";
		}
		for (File file : Webstart.getTextFiles()) {
			String name = file.getName();
			// name = name.substring(0, name.indexOf('.'));
			if (name.equalsIgnoreCase(fileName)) {
				files.add(file);
				Mark.say(false, "Adding", file);
			}
		}
		return files;
	}
}
