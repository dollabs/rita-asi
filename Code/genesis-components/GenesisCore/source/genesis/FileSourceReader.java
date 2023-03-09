package genesis;

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;

import org.apache.commons.io.IOUtils;

import com.ascent.gui.frame.alternative.*;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import constants.Switch;
import gui.PopupBox;

import start.Start;
import utils.*;

/**
 * <p>
 * Created on Mar 27, 2009
 *
 * @author phw
 */
public class FileSourceReader extends AbstractWiredBox {

	// Controls whether using fancy url based reader or plain reader from local directories
	public static boolean fileChooserReader = false;

	public static File fileChooserDirectory = null;
	
	public static boolean debug = false;

	private ArrayList<String> sentenceQueue;

	public static final int INCREMENTAL = 0;

	public static final int TOTAL = 1;

	private int mode = TOTAL;

	public static final String STATE = "state";

	public static final String PAUSE = "pause";

	public static final String HAS_QUEUE = "hasQueue";

	public static final String STATUS = "status port";

	private static FileSourceReader reader;

	private int sentenceCount;

	private int processedSentenceCount;

	private ArrayList<String> experiments;

	private String experiment;

	private ProcessSentences theReaderThread;


	private PopupBox popupBox;

	public PopupBox getPopupBox() {
		if (popupBox == null) {
			popupBox = new PopupBox(this);
		}
		return popupBox;
	}

	public ProcessSentences getTheReaderThread() {
		return theReaderThread;
	}

	public static FileSourceReader getFileSourceReader() {
		if (reader == null) {
			reader = new FileSourceReader();
		}
		return reader;
	}

	public FileSourceReader() {
		// Preserved as public because mental models want their own
		// copy.
		super("File source reader");
		// Set rerun button
		getLastStory();
		Connections.getPorts(this).addSignalProcessor(PAUSE, "pause");
	}

	// //////////////////////////////////////////////////////////
	// Story Locating Functions //
	// //////////////////////////////////////////////////////////

	public void readStoryWithoutNewThread(InputStream storyStream, URL url) {
		getSentenceQueue().addAll(0, getStoryLines(storyStream, url));
		initiateReadingWithoutNewThread();
	}

	public void readTheWholeStoryWithThread(String file, GenesisGetters panel) {
		readTheWholeStoryWithThread(file);
	}

	class AssemblyCompletable extends Completable {
		String name;

		boolean completed = false;

		public AssemblyCompletable(String name) {
			this.name = name;
		}

		@Override
		public boolean cleanup() {
			return false;
		}

		@Override
		public boolean isDone() {
			return completed;
		}

		public void run() {
			setSentenceQueue(readTheWholeStory(name));
			Mark.say(false, "Sentence queue set", getSentenceQueue());
			completed = true;
		}
	}

	class InsertionCompletable extends Completable {
		String name;

		boolean completed = false;

		public InsertionCompletable(String name) {
			this.name = name;
		}

		@Override
		public boolean cleanup() {
			return false;
		}

		@Override
		public boolean isDone() {
			return completed;
		}

		public void run() {
			getSentenceQueue().addAll(0, readTheWholeStory(name));
			completed = true;
		}
	}

	private File getTopDirectory() {
		File result = new File(Preferences.userRoot().get("File top directory", "c://"));
		Mark.say("Top directory is", result);
		return result;
	}

	public void readTheWholeStoryLocally(String name) {
		setLastStory(name);

		File fileTopDirectory = findTopDirectory(FileSourceReader.fileChooserDirectory);

		Mark.say("fileTopDirectory/fileChooserdirectory", fileTopDirectory, fileChooserDirectory);

		Preferences.userRoot().put("File top directory", fileTopDirectory.getPath());

		FileSourceReader.fileChooserReader = true;
		try {
			Mark.say("Reading from", name);
			readTheWholeStoryWithThread(name);
		}
		catch (Exception e) {
			Mark.err("Strange error!");
		}
		finally {
			// Mark.say("Resetting!!!");
			FileSourceReader.fileChooserReader = false;
		}
		// setLastStory(name);
	}

	public void readTheWholeStoryWithThread(String name) {
		Mark.say(debug, "FileSourceReader starts reading " + name);
		if (!FileSourceReader.fileChooserReader) {
			setLastStory(name);
		}
		// setSentenceQueue(readTheWholeStory(name));
		AssemblyCompletable loader = new AssemblyCompletable(name);
		boolean completed = ALauncher.launchDataLoader(loader, "Gathering data");
		if (completed) {
			initiateReading();
		}
	}

	// public void insertTheWholeStoryWithoutTread(String name) {
	// if (!FileSourceReader.fileChooserReader) {
	// // setLastStory(name);
	// }
	//
	// Mark.say("Before reading");
	//
	// getSentenceQueue().stream().forEachOrdered(e -> {
	// Mark.say("Element", e);
	// });
	//
	// ArrayList<String> tail = getSentenceQueue();
	//
	// ArrayList<String> head = readTheWholeStory(name);
	//
	// head.addAll(tail);
	//
	// setSentenceQueue(head);
	//
	// Mark.say("After reading");
	//
	// getSentenceQueue().stream().forEachOrdered(e -> {
	// Mark.say("Element", e);
	// });
	//
	// AssemblyCompletable loader = new AssemblyCompletable(name);
	// boolean completed = ALauncher.launchDataLoader(loader, "Gathering data");
	// if (completed) {
	// initiateReading();
	// }
	// }

	public void readTheWholeStoryWithoutTread(String name) {
		if (!FileSourceReader.fileChooserReader) {
			// setLastStory(name);
		}

		setSentenceQueue(readTheWholeStory(name));

		AssemblyCompletable loader = new AssemblyCompletable(name);
		boolean completed = ALauncher.launchDataLoader(loader, "Gathering data");
		if (completed) {
			initiateReadingWithoutNewThread();
		}
	}

	public ArrayList<String> readTheWholeStory(String name) {
		Boolean debug = false;
		// Try this first, newly written 11 Nov 2018
		if (!Webstart.isWebStart()) {
			Mark.say(debug, "Looking for", name);
			List<File> files = Webstart.getTextFile(name);
			Mark.night(debug,files);
			files.stream().forEachOrdered(f -> {
				Mark.say(debug, "Found", f);
			});
			if (files.size() == 0) {
				Mark.err("Could not find the file");
			}
			else if (files.size() > 1) {
				Mark.err("Found duplicate files, using first found");
				File f = files.get(0);
				files = new ArrayList<File>();
				files.add(f);
			}
			try {
				URL storyURL = files.get(0).toURI().toURL();
				if (storyURL != null) {
					InputStream inputStream = storyURL.openStream();
					ArrayList<String> sentences = getStoryLines(inputStream, storyURL);
					return replaceFileReferences(sentences);
				}
			}
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Mark.say("*************************** Entering legacy code");
		// Rest is a mess, legacy
		try {
			URL storyURL;
			if (FileSourceReader.fileChooserReader) {
				String fileName = FileSourceReader.fileChooserDirectory.getPath() + File.separator + name;

				// First, look under chooser directory
				File file = findFile(FileSourceReader.fileChooserDirectory, name);
				if (file == null) {
					// Mark.say("Could not find file", fileName, "under chooser directory");

					file = findFile(getTopDirectory(), name);
				}
				if (file == null) {
					Mark.err("Could not find file", fileName, "under either chooser directory or top directory either");
					return new ArrayList<String>();
				}
				storyURL = file.toURI().toURL();
			}
			else {
				Mark.say("***** name", name);
				storyURL = PathFinder.storyURL(name);

			}
			if (storyURL != null) {
			InputStream inputStream = storyURL.openStream();
			ArrayList<String> sentences = getStoryLines(inputStream, storyURL);
			return replaceFileReferences(sentences);
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File findTopDirectory(File fileChooserDirectory) {
		Mark.purple(fileChooserDirectory);
		if (fileChooserDirectory.getName().equalsIgnoreCase("GenesisCore")) {
			return fileChooserDirectory;
		}
		else {
			try {
				return findTopDirectory(fileChooserDirectory.getParentFile());
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	int fileCount;

	boolean checkFileCount;

	private File findFile(File directory, String name) {
		Mark.say(false, "Looking for", name, "in", directory);
		if (checkFileCount && fileCount > 1000) {
			checkFileCount = false;
			Mark.say("Take care, I am looking at > 1000 files");
		}
		File[] files = directory.listFiles();
		if (files == null) {
			return null;
		}
		for (File file : files) {
			if (file.isFile()) {
				fileCount++;
				if (file.getName().equalsIgnoreCase(name)) {
					return file;
				}
			}
		}
		for (File file : files) {
			if (file.isDirectory()) {
				File result = findFile(file, name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private ArrayList<String> replaceFileReferences(ArrayList<String> sentences) {
		ArrayList<String> result = new ArrayList<>();
		sentences.stream().forEachOrdered(s -> {
			if (s.startsWith(Markers.INSERT_FILE) && !s.startsWith(Markers.INSERT_START_EXPERIMENT_FILE)) {
				String file = s.substring(Markers.INSERT_FILE.length()).trim();
				result.addAll(replaceFileReferences(readTheWholeStory(file + "txt")));
			}
			// Super special case for Suri. Identifies special case construction:
			// Identify modulation for xxx, adds Pause. and question insertion,
			// Which is caught by more super special case code in the question expert
			else if (s.startsWith(Markers.IDENTIFY_MODULATION_FOR)) {
				String file = s.substring(Markers.IDENTIFY_MODULATION_FOR.length()).trim();
				ArrayList<String> replacement = replaceFileReferences(readTheWholeStory(file + "txt"));
				result.add("Pause.");
				result.addAll(replacement);
				result.add("Insert question into text box: " + s);
			}
			else {
				result.add(s);
			}
		});
		return result;
	}

	// public void readStoryWithoutNewThread(String storyReference) {
	// try {
	// URL storyURL = PathFinder.storyURL(storyReference);
	// this.lastStory = storyURL;
	// InputStream storyStream = storyURL.openStream();
	// readStoryWithoutNewThread(storyStream, storyURL);
	// }
	// catch (IOException e) {
	// Mark.err("Could not find story : " + storyReference);
	// e.printStackTrace();
	// }
	// }

	public String readFileText(String storyReference) {
		try {
			Mark.say("storyReference", storyReference);
			URL storyURL = PathFinder.storyURL(storyReference);
			InputStream storyStream = storyURL.openStream();

			String text = IOUtils.toString(storyStream);

			// Added by phw
			storyStream.close();
			// Mark.say("Closed story stream for", url.getPath());

			// text = removeComments(text);

			text = Comments.dike(text);

			return text;
		}
		catch (IOException e) {
			Mark.err("Could not find file: " + storyReference);
			e.printStackTrace();
		}
		return null;
	}

	// public void readStory(String storyReference) {
	// try {
	// URL storyURL = PathFinder.storyURL(storyReference);
	// this.lastStory = storyURL;
	// InputStream storyStream = storyURL.openStream();
	// readStory(storyStream, storyURL);
	// }
	// catch (IOException e) {
	// Mark.err("Could not find story : " + storyReference);
	// e.printStackTrace();
	// }
	// }
	//
	// public void readStory(File story) {
	// try {
	// lastStory = story.toURI().toURL();
	// readStory(new FileInputStream(story), lastStory);
	// }
	// catch (FileNotFoundException | MalformedURLException e) {
	// Mark.err("Could not find : " + story);
	// e.printStackTrace();
	// }
	// }

	private ArrayList<String> getStoryLines(InputStream storyStream, URL url) {
		try {
			String storyString = IOUtils.toString(storyStream);

			// Added by phw
			storyStream.close();
			// Mark.say("Closed story stream for", url.getPath());

			// storyString = removeComments(storyString);
			storyString = Comments.dike(storyString);

			ArrayList<String> sentences = splitText(storyString);

			if (getSentenceQueue().isEmpty()) {
				Connections.getPorts(this).transmit(FileSourceReader.HAS_QUEUE, Boolean.FALSE);
			}
			else {
				Connections.getPorts(this).transmit(FileSourceReader.HAS_QUEUE, Boolean.TRUE);
			}

			if (Switch.stepThroughNextStory.isSelected()) {
				int i;
				boolean found = false;
				for (i = 0; i < sentences.size(); ++i) {
					String sentence = sentences.get(i);
					if (sentence.startsWith(Markers.START_STORY_TEXT)) {
						found = true;
						break;
					}
				}
				if (found) {
					sentences.add(i + 1, Markers.PAUSE_MARKER + ".");
					Mark.say("Added pause sentence after story start");
					Switch.stepThroughNextStory.setSelected(false);
				}
			}
			return sentences;
		}
		catch (Exception e) {
			System.err.println("Unable to read sentences from Story.");
			e.printStackTrace();
			return null;
		}
	}

	// //////////////////////////////////////////////////////////
	// Text Processing Functions //
	// //////////////////////////////////////////////////////////

	public ArrayList<String> getSentenceQueue() {
		if (sentenceQueue == null) {
			sentenceQueue = new ArrayList<String>();
		}
		return sentenceQueue;
	}

	public void setSentenceQueue(ArrayList<String> sentenceQueue) {
		this.sentenceQueue = sentenceQueue;
		if (sentenceQueue != null) {
			sentenceCount = sentenceQueue.size();
		}
		processedSentenceCount = 0;
	}

	public void rerun() {
		if (getLastStory() == null) {
			Mark.err("No previous story has been read.");
		}
		else {
			this.readTheWholeStoryWithThread(getLastStory());
		}
	}

	public boolean hasQueue() {
		return !getSentenceQueue().isEmpty();
	}

	public void pause(Object object) {
		if (object == Start.PAUSE) {
			mode = INCREMENTAL;
		}
	}

	public void stop(Object object) {
		if (object == Start.STOP) {
			mode = INCREMENTAL;
		}
	}

	public void readNextSentence() {
		mode = INCREMENTAL;
		// GenesisControls.getNextButton().setEnabled(false);
		// GenesisControls.getRunButton().setEnabled(false);
		new ProcessSentences().start();

	}

	public void readRemainingSentences() {
		mode = TOTAL;
		GenesisControls.getNextButton().setEnabled(false);
		GenesisControls.getRunButton().setEnabled(false);
		GenesisGetters.getContinueButton().setEnabled(false);
		GenesisGetters.getContinueButton().setOpaque(false);
		new ProcessSentences().start();

	}

	public void initiateReadingWithoutNewThread() {
		// Mark.say("Sentence queue", getSentenceQueue().size());
		setExperiment(null);
		GenesisGetters.getNextButton().setEnabled(false);
		GenesisGetters.getRunButton().setEnabled(false);
		GenesisGetters.getContinueButton().setEnabled(false);
		GenesisGetters.getContinueButton().setOpaque(false);
		if (getSentenceQueue().isEmpty()) {
			return;
		}
		setExperiments(null);
		processSentences();
		// String sentence = getSentenceQueue().get(0);
		// if (sentence.startsWith(Markers.RUN_EXPERIMENTS)) {
		// getSentenceQueue().remove(0);
		// new ProcessExperiment(sentence).start();
		// }
		// else {
		// readRemainingSentences();
		// }
	}

	public void initiateReading() {
		setExperiment(null);
		GenesisGetters.getNextButton().setEnabled(false);
		GenesisGetters.getRunButton().setEnabled(false);
		GenesisGetters.getContinueButton().setEnabled(false);
		GenesisGetters.getContinueButton().setOpaque(false);
		if (getSentenceQueue().isEmpty()) {
			return;
		}
		setExperiments(null);
		String sentence = getSentenceQueue().get(0);
		if (sentence.startsWith(Markers.RUN_EXPERIMENTS)) {
			getSentenceQueue().remove(0);
			new ProcessExperiment(sentence).start();
		}
		else {
			readRemainingSentences();
		}
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	class ProcessExperiment extends Thread {
		String firstSentence;

		public ProcessExperiment(String starter) {
			firstSentence = starter;
		}

		public void run() {
			boolean debug = false;
			// This is where what-if experiments are controlled
			getExperiments().addAll(extractExperiments(firstSentence.substring(Markers.RUN_EXPERIMENTS.length())));
			Mark.say(debug, "Running experiments!!!!!", getExperiments());
			ArrayList<String> handle = new ArrayList<String>();
			handle.addAll(getSentenceQueue());
			for (String experiment : getExperiments()) {
				Mark.say(debug, "Working on experiment", experiment);
				setExperiment(experiment);
				setSentenceQueue(null);
				getSentenceQueue().addAll(handle);
				processedSentenceCount = 0;
				sentenceCount = getSentenceQueue().size();
				Mark.say(debug, "There are", sentenceCount, "sentences to process");
				processSentences();
				try {
					int sec = 5;
					Mark.say(debug, "Pausing at end of experiment for", 5, "seconds.");
					Thread.sleep(sec * 1000);
					// Mark.say("Pause concluded.");
				}
				catch (InterruptedException e) {
					Mark.err("Blew out of sleep in FileSourceReader.startProcessing");
				}
			}
			Mark.say("Experiments complete");
		}
	}

	class ProcessSentences extends Thread {
		public void run() {

			NewTimer.startDirectTimer.initialize();
			NewTimer.startBetaTimer.initialize();
			NewTimer.generatorTimer.initialize();


			Mark.yellow(debug, "FileSourceReader starts sending sentences to StoryProcessor");
			processSentences();

			NewTimer.startDirectTimer.summarize();
			NewTimer.startBetaTimer.summarize();
			NewTimer.generatorTimer.summarize();

			if (restore) {
				restoreSwitches();
			}

		}
	}

	public ArrayList<String> extractExperiments(String remainder) {
		ArrayList<String> result = new ArrayList<String>();
		for (String experiment : remainder.split(" ")) {
			experiment = experiment.trim();
			if (".,:;".indexOf(experiment.charAt(experiment.length() - 1)) >= 0) {
				experiment = experiment.substring(0, experiment.length() - 1);
			}
			if (experiment.equals("and") || experiment.equals("or")) {
				continue;
			}
			result.add(experiment);
		}
		return result;
	}

	private void processSentences() {

		int totalSentences = 0;
		int totalTime = 0;
		int maximumTime = 0;

		// Mark.say("Processed/Total:", processedSentenceCount, sentenceCount);
		// Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.HAS_QUEUE,
		// Boolean.FALSE);
		Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.STATE, GenesisGetters.CLOSE);
		Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.STATUS, new BetterSignal(processedSentenceCount, sentenceCount));

		NewTimer.totalProcessingTimer.reset();

		while (!getSentenceQueue().isEmpty()) {
			// This not only gets it, it deletes it from queue
			String sentence = getNextSentence().trim();

			Mark.say(false, "Next sentence:", sentence);

			Mark.say(debug, "Processing:  ", sentence);
			if (sentence.startsWith(Markers.RUN_EXPERIMENTS)) {
				Mark.err("Ooops, the expression\"", Markers.RUN_EXPERIMENTS, "\" can only appear as the first expression read.");
			}
			if (sentence.startsWith(Markers.CONDITION_ON_EXPERIMENT)) {
				Mark.say(debug, "Noted experiment", getExperiment());
				if (experiment != null) {
					boolean included = false;
					int colonIndex = sentence.indexOf(":");
					if (colonIndex < 0) {
						Mark.err("No colon in", Markers.CONDITION_ON_EXPERIMENT, "expression:", sentence);
						throw new RuntimeException();
					}
					for (String x : extractExperiments(sentence.substring(Markers.CONDITION_ON_EXPERIMENT.length(), colonIndex))) {
						Mark.say(debug, "Working on|", x, "|", getExperiment(), "|");
						if (getExperiment().equalsIgnoreCase(x)) {
							included = true;
							// Mark.say("Found conditioning on", experiment);
							break;
						}
					}
					if (included) {
						sentence = sentence.substring(colonIndex + 1).trim();
						Mark.say(debug, "Experiment", getExperiment(), "included, reading", sentence);
					}
					else {
						continue;
					}
				}
			}
			// if, not else if!
			// This one for problem solving knowledge 1 Apr 2018
			if (sentence.startsWith(Markers.INSERT_KNOWLEDGE)) {
				Mark.say("Inserting problem solving knowledge:", sentence);
				String storyName = sentence.substring(Markers.INSERT_KNOWLEDGE.length()).trim() + "txt";

				File file = findFile(getTopDirectory(), storyName);
				Mark.say("File to be read is", storyName, file);
				GenesisGetters.getMentalModel1().getProblemSolver().readData(file);
				continue;
			}
			else if (sentence.startsWith(Markers.INSERT_HELPER_KNOWLEDGE)) {
				Mark.say("Inserting problem solving helper knowledge");
				String storyName = sentence.substring(Markers.INSERT_HELPER_KNOWLEDGE.length()).trim() + "txt";

				File file = findFile(getTopDirectory(), storyName);
				GenesisGetters.getMentalModel2().getProblemSolver().readData(file);
				continue;
			}
			else if (sentence.startsWith(Markers.INSERT_FILE) && !sentence.startsWith(Markers.INSERT_START_EXPERIMENT_FILE)) {
				Mark.say(debug, "INSERTING FILE!!!!!!!!!!!!!!!!!!!!!!!!\nSentence starts with prefix", Markers.INSERT_FILE, sentence);
				String storyName = sentence.substring(Markers.INSERT_FILE.length()).trim() + "txt";
				Mark.say(debug, "Name |", storyName, "|");
				try {
					InsertionCompletable loader = new InsertionCompletable(storyName);
					boolean completed = ALauncher.launchDataLoader(loader, "Gathering data");
					sentenceCount = getSentenceQueue().size();
					continue;
				}
				catch (Exception e) {
					Mark.err("Could not find referenced story : " + storyName);
					e.printStackTrace();
				}
			}
			// Mark.say("Processed", next);
			else if (sentence.equalsIgnoreCase("Pause.") || sentence.equalsIgnoreCase("Pause?")) {
				if (experiment != null) {
					Mark.say("\"Pause.\" not recognized when running what-if mode experiment", experiment, ".  Delaying a few seconds instead.");
					sentence = "Delay.";
					// Mark.say("\"Pause.\" not recognized when running what-if mode experiment", experiment,
					// ". Skipping.");
				}
				else {
					GenesisControls.getNextButton().setEnabled(true);
					GenesisControls.getRunButton().setEnabled(true);
					GenesisGetters.getContinueButton().setEnabled(true);
					GenesisGetters.getContinueButton().setBackground(Color.YELLOW);
					GenesisGetters.getContinueButton().setOpaque(true);


					// Mark.say("Breaking on pause");
					break;
				}
			}
			else if (sentence.equalsIgnoreCase("Delay.")) {
				int sec = 3;
				// Mark.say("Delaying by", sec, "seconds");
				try {
					Thread.sleep(sec * 1000);
				}
				catch (InterruptedException e) {
				}
				// Mark.say("Delaying over");
				continue;
			}

			else if (sentence.startsWith(Markers.POPUP)) {
				String file = sentence.substring(Markers.POPUP.length()).trim();
				getPopupBox().processPopup(new BetterSignal(file));

				if (!getPopupBox().isResult()) {
					getSentenceQueue().clear();
					sentence = "Start experiment.";
				}
				continue;
			}

			if (sentence != null && !sentence.trim().isEmpty()) {
			}
//			Mark.night("Transmitting", sentence);
			Connections.getPorts(FileSourceReader.this).transmit(sentence);
			Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.STATUS, new BetterSignal(processedSentenceCount, sentenceCount));
			if (mode == INCREMENTAL) {
				break;
			}
		}
		if (getSentenceQueue().isEmpty()) {
			Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.HAS_QUEUE, Boolean.FALSE);
		}
		else {
			Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.HAS_QUEUE, Boolean.TRUE);
		}
		Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.STATE, GenesisGetters.OPEN);

		if (getSentenceQueue().isEmpty()) {
			Connections.getPorts(FileSourceReader.this).transmit(FileSourceReader.HAS_QUEUE, Boolean.FALSE);
		}
		NewTimer.totalProcessingTimer.report(false);
	}

	private String getNextSentence() {
		String sentence = getSentenceQueue().get(0);
		ArrayList<String> newSentenceQueue = new ArrayList<String>();
		for (int i = 1; i < getSentenceQueue().size(); ++i) {
			newSentenceQueue.add(getSentenceQueue().get(i));
		}
		++processedSentenceCount;
		sentenceQueue = newSentenceQueue;
		return sentence;
	}

	// private String removeComments(String string) {
	// int extra = "\n".length();
	// if (string == null) {
	//
	// return null;
	// }
	// StringBuffer sb = new StringBuffer(string);
	// int index1;
	// int index2;
	// while ((index1 = sb.indexOf("/*")) >= 0) {
	// index2 = findMatchingDelimiter(sb, index1);
	// // Mark.say("Removing:", sb.substring(index1, index2+2));
	// sb.delete(index1, index2 + 2);
	// }
	// while ((index1 = sb.indexOf("//")) >= 0) {
	// index2 = sb.indexOf("\n", index1);
	// if (index2 < 0) {
	// sb.delete(index1, sb.length() + 1);
	// }
	// else {
	// // Mark.say("Removing:", sb.substring(index1, index2+1));
	// sb.delete(index1, index2 + extra);
	// }
	// }
	// return sb.toString();
	// }

	private int findMatchingDelimiter(StringBuffer sb, int index1) {
		return sb.indexOf("*/");
	}

	/*
	 * Search for first period, not followed by a number, and first question mark. Split on which comes first.
	 */
	private ArrayList<String> splitText(String string) {
		ArrayList<String> result = new ArrayList<String>();

		// // added by Zhutian Yang for Recipe expert to hear long paragraphs from experts
		// if(!Radio.qToZTY.isSelected()) {
		// ArrayList<String> results = new ArrayList<>(Arrays.asList(string.split("\n")));
		// for(String rr: results) {
		// if(rr.length()>3) {
		// result.add(rr);
		// }
		// }
		// Z.printList(result);
		// return result;
		// }

		while (true) {
			int terminator = findSentenceTerminator(string);
			if (terminator >= 0) {
				// Must be something there
				// Mark.say("String", string, string.length());
				String sentence = string.substring(0, terminator + 1).trim();
				string = string.substring(terminator + 1);
				result.add(sentence);
			}
			else {
				// Nothing there, quit
				break;
			}
		}
		// result.stream().forEach(f -> Mark.say("Sentence", f));
		return result;
	}

	private int findSentenceTerminator(String string) {
		int questionMarkIndex = string.indexOf('?');

		int start = 0;
		int periodIndex = -1;

		while (true) {
			periodIndex = string.indexOf('.', start);
			if (periodIndex < string.length() - 1) {
				// Ok, there must be another character
				if (Character.isLetterOrDigit(string.charAt(periodIndex + 1))) {
					// Cannot be a sentence terminator, so keep going.
					start = periodIndex + 1;
					continue;
				}
				else {
					// Evidently, a sentence terminator, break
					break;
				}
			}
			// Period is final character, break
			break;

		}
		// Now, see which is both found and least
		if (questionMarkIndex >= 0 && periodIndex >= 0) {
			return Math.min(questionMarkIndex, periodIndex);
		}
		else if (periodIndex >= 0) {
			return periodIndex;
		}
		else if (questionMarkIndex >= 0) {
			return questionMarkIndex;
		}
		// Nothing found
		return -1;

	}

	public ArrayList<String> getExperiments() {
		if (experiments == null) {
			experiments = new ArrayList<String>();
		}
		return experiments;
	}

	public void setExperiments(ArrayList<String> experiments) {
		this.experiments = experiments;
	}

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	private static boolean restore = false;

	private static boolean normalModeButtonMemory;

	private static boolean alignmentButtonMemory;

	private static boolean showOnsetSwitchMemory;

	private static boolean showTextEntryBoxMemory;

	private static boolean showDisconnectedSwitchMemory;

	private static boolean summarizeViaPHWMemory;

	private static boolean persuadeViaSSMemory;

	public static void clickToValue(AbstractButton button, boolean value) {
		button.setSelected(!value);
		button.doClick();
	}

	public static void setToValue(AbstractButton button, boolean value) {
		button.setSelected(value);
	}

	public static void rememberSwitches() {
		restore = true;

		normalModeButtonMemory = Radio.normalModeButton.isSelected();
		alignmentButtonMemory = Radio.alignmentButton.isSelected();
		showOnsetSwitchMemory = Switch.showOnsetSwitch.isSelected();
		showTextEntryBoxMemory = Switch.showTextEntryBox.isSelected();
		showDisconnectedSwitchMemory = Switch.showDisconnectedSwitch.isSelected();

		clickToValue(Radio.normalModeButton, true);
		clickToValue(Switch.showOnsetSwitch, true);
		clickToValue(Switch.showTextEntryBox, false);
		clickToValue(Switch.showDisconnectedSwitch, false);
	}

	public static void restoreSwitches() {
		// Mark.say("Restoring");
		restore = false;

		clickToValue(Radio.normalModeButton, normalModeButtonMemory);
		clickToValue(Switch.showOnsetSwitch, showOnsetSwitchMemory);
		clickToValue(Switch.showTextEntryBox, showTextEntryBoxMemory);
		clickToValue(Switch.showDisconnectedSwitch, showDisconnectedSwitchMemory);
	}

	public String getLastStory() {
		String result = Preferences.userRoot().get("the last story read", null);
		// Mark.say("The last story now to be read", result);
		if (result == null) {
			GenesisGetters.getRerunButton().setEnabled(false);
		}
		else {
			GenesisGetters.getRerunButton().setEnabled(true);
		}
		return result;
	}

	public void setLastStory(String lastStory) {
		// Mark.say("The last story set is", lastStory);
		Preferences.userRoot().put("the last story read", lastStory);
		GenesisGetters.getRerunButton().setEnabled(true);
	}

}
