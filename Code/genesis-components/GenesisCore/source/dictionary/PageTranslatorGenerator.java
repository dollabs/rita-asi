package dictionary;

import generator.*;
import genesis.FileSourceReader;
import gui.FileReaderPanel;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;


import start.Start;
import translator.BasicTranslator;
import utils.*;
import zhutianYang.DirectPerceptionExpert;
import zhutianYang.ZPage;


import connections.*;
import constants.GenesisConstants;
import constants.Lockup;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Sequence;

@SuppressWarnings("serial")
public class PageTranslatorGenerator extends JPanel implements WiredBox {
	
	// --------------------------------------------
	// added by Zhutian for debugging stories
	// --------------------------------------------
	private FileReaderPanel fileReaderFrame;
	private File selectedFile;
	private static List<String> sentences = new ArrayList<>();
	private static int sentenceIndex = 0;
	private static String storyPath = "";
	private JButton next = new JButton("Next sentence");
	private JButton previous = new JButton("Previous sentence");
	private JButton updateLeft = new JButton("Update");
	private JTextArea sentencesDisplay = new WiredTextArea();
	private String backupTime = "";
	// --------------------------------------------
	
	
	private JPanel buttonsAndCheckBoxes = new JPanel();

	private JPanel buttons = new JPanel();

	private JPanel checkBoxes = new JPanel();

	private JPanel mainPanel = new JPanel();

	private JPanel viewerPanel = new JPanel();

	private ZNewFrameViewer viewer = new ZNewFrameViewer();

	private WiredTextArea fromStartDisplay = new WiredTextArea();

	private WiredTextArea toStartDisplay = new WiredTextArea();
	
	private WiredTextArea directedTriplesDisplay = new WiredTextArea();

	private WiredTextArea prettyDisplay = new WiredTextArea();


	private static WiredTextField wiredTextInputField = new WiredTextField();

	private WiredTextField wiredTextDireclyBackField = new WiredTextField();

	private WiredTextField wiredTextOutputField = new WiredTextField();

	private JPanel textFields = new JPanel();

	private final JButton stepButton = new JButton("Step forward");

	private final JButton retreatButton = new JButton("Retreat");

	private final static JButton runButton = new JButton("Run");

	private final JButton processStartTripplesButton = new JButton("Process");

	private final JButton processGenesisTripplesButton = new JButton("Process");

	private final static CheckBoxWithMemory autoRunButton = new CheckBoxWithMemory("Auto run");

	private int textFieldSize = 20;

	private int textFieldWeight = Font.BOLD;

	private int textAreaSize = 20;

	private int textAreaWeight = Font.PLAIN;

	public final static String FROM_TRANSLATOR = "from translator";

	public final static String TRIPLES_FROM_START = "triples from start";

	public final static String EDITED_PARSE = "edited parse";

	public final static String EDITED_TRIPLES = "edited triples";

	public final static String INPUT = "input";

	private Sequence sequenceFromStart;

	public PageTranslatorGenerator() {
		super();
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(textFields, BorderLayout.NORTH);
		
		ButtonActionListener l = new ButtonActionListener();
		stepButton.addActionListener(l);
		retreatButton.addActionListener(l);
		runButton.addActionListener(l);
		processStartTripplesButton.addActionListener(l);
		processGenesisTripplesButton.addActionListener(l);

		wiredTextInputField = new WiredTextField();
		wiredTextInputField.setFont(new Font("Dialog", textFieldWeight, textFieldSize));
		Mark.say("Setting text field to", Preferences.userRoot().get(this.getClass().getName(), ""));
		wiredTextInputField.setText(Preferences.userRoot().get(this.getClass().getName(), ""));
		wiredTextInputField.addKeyListener(new PunctuationListener());
		wiredTextInputField.requestFocusInWindow();
		

		// ------------------
		// mainPanel
		// ------------------
		mainPanel.setLayout(new GridLayout(1, 0));
		
		// --------------------------------------------
		// modified by Zhutian for debugging stories
		// --------------------------------------------
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout());
		left.add(sentencesDisplay, BorderLayout.CENTER);
		sentencesDisplay.setBackground(Z.LIGHT_PINK);
		sentencesDisplay.setFont(new Font("Dialog", textAreaWeight, ZPage.smallFontSize));
		
		updateLeft.setEnabled(false);
		updateLeft.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				String text = sentencesDisplay.getText();
				
			}
		});
		left.add(updateLeft, BorderLayout.SOUTH);
		mainPanel.add(left);
		// --------------------------------------------
		
		mainPanel.add(viewerPanel);
		viewerPanel.setLayout(new BorderLayout());
		
		viewerPanel.add(viewer, BorderLayout.CENTER);
		viewer.setPreferredSize(new Dimension(1000, 1000));
		viewer.clear();
		
		viewerPanel.add(buttonsAndCheckBoxes, BorderLayout.SOUTH);
		buttonsAndCheckBoxes.setLayout(new GridLayout(0, 1));
		buttonsAndCheckBoxes.add(checkBoxes);
		new Lockup(Switch.showTranslationDetails, Switch.showTranslationDetails2);
		new Lockup(Switch.useStartBeta, Switch.useStartBeta2);
		checkBoxes.setLayout(new GridLayout(1, 0));
		checkBoxes.add(autoRunButton);
		checkBoxes.add(Switch.showTranslationDetails2);
		checkBoxes.add(Switch.useStartBeta2);
		
		buttonsAndCheckBoxes.add(buttons);
		buttons.setLayout(new GridLayout(1, 0));
		buttons.add(stepButton);
		buttons.add(retreatButton);
		buttons.add(runButton);
		

		// --------------------------------------------
		// modified by Zhutian for debugging stories
		// --------------------------------------------
		JPanel triplesPanel = new JPanel();
		triplesPanel.setLayout(new GridLayout(4,1));
		mainPanel.add(triplesPanel);
		
		JPanel upper = new JPanel();
		upper.setLayout(new BorderLayout());
		upper.add(fromStartDisplay, BorderLayout.CENTER);
		upper.add(new JLabel("START triples"), BorderLayout.NORTH);
		fromStartDisplay.setBackground(Z.LIGHT_YELLOW);
		fromStartDisplay.setFont(new Font("Dialog", textAreaWeight, ZPage.smallFontSize));
		upper.add(processStartTripplesButton, BorderLayout.SOUTH);
		upper.add(new JScrollPane(fromStartDisplay));
		triplesPanel.add(upper);
		
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(toStartDisplay, BorderLayout.CENTER);
		right.add(new JLabel("Innerese triples"), BorderLayout.NORTH);
		toStartDisplay.setBackground(Z.LIGHT_CYAN);
		toStartDisplay.setFont(new Font("Dialog", textAreaWeight, ZPage.smallFontSize));
		right.add(processGenesisTripplesButton, BorderLayout.SOUTH);
		right.add(new JScrollPane(toStartDisplay));
		triplesPanel.add(right);
		
		JPanel directed = new JPanel();
		directed.setLayout(new BorderLayout());
		directed.add(directedTriplesDisplay, BorderLayout.CENTER);
		directed.add(new JLabel("Semantic triples"), BorderLayout.NORTH);
		directedTriplesDisplay.setBackground(Z.LIGHT_YELLOW);
		directedTriplesDisplay.setFont(new Font("Dialog", textAreaWeight, ZPage.smallFontSize));
		directed.add(new JScrollPane(directedTriplesDisplay));
		triplesPanel.add(directed);
		
		JPanel pretty = new JPanel();
		pretty.setLayout(new BorderLayout());
		pretty.add(prettyDisplay, BorderLayout.CENTER);
		pretty.add(new JLabel("Tree Structure"), BorderLayout.NORTH);
		prettyDisplay.setBackground(Z.LIGHT_CYAN);
		prettyDisplay.setFont(new Font("Dialog", textAreaWeight, ZPage.smallFontSize));
		pretty.add(new JScrollPane(prettyDisplay));
		triplesPanel.add(pretty);
		// --------------------------------------------
	
		// ------------------
		// textFields
		// ------------------
		textFields.setLayout(new GridLayout(0, 1));
		
		
		// --------------------------------------------
		// added by Zhutian for debugging stories
		// --------------------------------------------
		JPanel extraTop = new JPanel();
		extraTop.setLayout(new GridLayout(1, 0));
		
		next.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				runNextSentence();
			}
		});
		
		JButton update = new JButton("Update");
		update.setEnabled(false);
		update.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				String text = wiredTextInputField.getText();
				replaceNWrite(sentences.get(sentenceIndex-1), text);
			}
		});
		
		previous.setEnabled(false);
		previous.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				sentenceIndex = sentenceIndex - 2;
				runNextSentence();
			}
		});
		
		JButton story = new JButton("Choose story");
		story.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Boolean debug = false;
				Mark.say(debug, "Entering readStory listener");
				String defaultDirectory = Preferences.userRoot().get(GenesisConstants.STORY_ROOT, "C:/");
				String path = Preferences.userRoot().get(GenesisConstants.STORY_FILE, defaultDirectory);
				Mark.say(debug, "Preferred story is", path);
				
				FileReaderPanel fileReaderPanel = getFileReaderFrame();
				JFileChooser chooser = fileReaderPanel.getFileChooser(path);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = chooser.showOpenDialog(PageTranslatorGenerator.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					Mark.say("Selected file is", selected);
					selectedFile = selected;
					Mark.say("You chose to open this file: " + selected);
					Preferences.userRoot().put(GenesisConstants.STORY_FILE, selected.getAbsolutePath());
					if (selected.exists()) {
						FileSourceReader.fileChooserDirectory = selected.getParentFile();
						sentences = loadStories(selected.getName());
						runNextSentence();
						update.setEnabled(true);
						next.setEnabled(true);
						backupTime = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
						String display = "";
						for(String sentence: sentences) {
							display += sentence + "\n";
						}
						sentencesDisplay.setText(display);
					} else {
						System.err.println(selected + " does not exist!");
						fileReaderPanel.setState(FileReaderPanel.stopped);
					}
				} else {
					System.out.println("You did not chose to open a file");
				}
			}
		});
		
		extraTop.add(story);
		extraTop.add(next);
		extraTop.add(update);
		extraTop.add(previous);
		
		textFields.add(extraTop);
		// --------------------------------------------
		
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		JLabel input = new JLabel("Input:          ");
		input.setOpaque(true);
		input.setBackground(Color.WHITE);
		input.setFont(new Font("Courier", textFieldWeight, textFieldSize));
		top.add(input, BorderLayout.WEST);
		top.add(wiredTextInputField, BorderLayout.CENTER);
		String test = Preferences.userRoot().get(this.getClass().getName(), "Type test sentence here");
		wiredTextInputField.setText(test + ".");
		textFields.add(top);

		JPanel middle = new JPanel();
		middle.setLayout(new BorderLayout());
		JLabel direct = new JLabel("Short circuit:  ");
		direct.setOpaque(true);
		direct.setBackground(Color.WHITE);
		direct.setFont(new Font("Courier", textFieldWeight, textFieldSize));
		middle.add(direct, BorderLayout.WEST);
		middle.add(wiredTextDireclyBackField, BorderLayout.CENTER);
		wiredTextDireclyBackField.setFont(new Font("Dialog", textFieldWeight, textFieldSize));
		textFields.add(middle);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		JLabel output = new JLabel("Via Genesis:    ");
		output.setOpaque(true);
		output.setBackground(Color.WHITE);
		output.setFont(new Font("Courier", textFieldWeight, textFieldSize));
		bottom.add(output, BorderLayout.WEST);
		bottom.add(wiredTextOutputField, BorderLayout.CENTER);
		wiredTextOutputField.setFont(new Font("Dialog", textFieldWeight, textFieldSize));
		textFields.add(bottom);
		
		
		Connections.wire(wiredTextInputField, INPUT, this);
		Connections.wire(BasicTranslator.PROGRESS, BasicTranslator.getTranslator(), viewer);
		Connections.wire(BasicTranslator.DEBUGGING_RESULT, BasicTranslator.getTranslator(), FROM_TRANSLATOR, this);
		Connections.getPorts(this).addSignalProcessor(FROM_TRANSLATOR, this::processTranslatorInput);
		Connections.getPorts(this).addSignalProcessor(INPUT, this::processInput);


	}
	
	public void replaceNWrite(String oldLine, String newLine) {
		try {
			String backupFile = storyPath.replace(".txt", "-"+ backupTime + ".txt");
			Files.copy(Paths.get(storyPath), Paths.get(backupFile), 
					StandardCopyOption.REPLACE_EXISTING);
			List<String> newSentences = new ArrayList<>(Files.readAllLines(Paths.get(storyPath), StandardCharsets.UTF_8));
			FileWriter writer = new FileWriter(storyPath, false);
			for (String line: newSentences) {
				writer.write(line.replace(oldLine, newLine)+"\n");
			}
			sentences.set(sentenceIndex-1, newLine);
			writer.close();
			runNextSentence();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void runNextSentence() {
		Boolean debug = false;
		Mark.say(debug, "Parsers");
		BundleGenerator.purgeWordnetCache();
		Mark.show(sentenceIndex);
		String sentence = sentences.get(sentenceIndex++);
		
		if(sentenceIndex>=sentences.size()) {
			next.setEnabled(false); 
		} else {
			next.setEnabled(true); 
		}
		if(sentenceIndex<2) {
			previous.setEnabled(false); 
		} else {
			previous.setEnabled(true); 
		}
		
		wiredTextInputField.setText(sentence);
		String text = wiredTextInputField.getText();
		text = normalizePeriod(text);
		// getWiredTextInputField().setText(text);
		Mark.say(debug, "Storing at X", wiredTextInputField.getText());
		stimulate(text);
		if (autoRunButton.isSelected()) {
			Connections.getPorts(wiredTextInputField).transmit(wiredTextInputField.getText());
		}
	}
	
	public static List<String> loadStories(String story) {
		
		String storiesPath = "corpora/Ensembles/";
		storyPath = storiesPath+story;
		Mark.say("Loading sentences from" + storyPath);
		
		try {
			sentences = new ArrayList<>(Files.readAllLines(Paths.get(storyPath), StandardCharsets.UTF_8));
			sentences.removeAll(Arrays.asList("", null));
			sentences = Z.listStoryElements(sentences);
			sentences = Z.listRemoveComment(sentences);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(String sen: sentences) {
			Mark.night(sen);
			
		}
		
		return sentences;
	}
	
	public FileReaderPanel getFileReaderFrame() {
		if (fileReaderFrame == null) {
			fileReaderFrame = new FileReaderPanel();
		}
		return fileReaderFrame;
	}

	public void processInput(Object o) {
		if (o instanceof String) {
			String sentence = (String) o;
			// sequenceFromStart = Start.getStart().parse(sentence);

			sequenceFromStart = Start.getStart().processForTestor(sentence);

			// Mark.say("!!!!!!!!!!!!!!!!!", sequenceFromStart.getElements().size());

			String rawText = Start.getStart().getProcessedSentence();

			// Mark.say("Raw text:", rawText);

			String triples = extractTriples(rawText);

			// Mark.say("Triples from Start\n", triples);

			fromStartDisplay.setText(extractTriples(rawText));

			wiredTextDireclyBackField.setText(Start.getStart().generate(triples));
			
			directedTriplesDisplay.setText(DirectPerceptionExpert.printSelectedTriples(rawText));
			
			prettyDisplay.setText(Z.getPrettyTree(sentence));

			BasicTranslator.getTranslator().setInput(sequenceFromStart);

			if (autoRunButton.isSelected()) {
				BasicTranslator.getTranslator().go();
			}
			else {
				BasicTranslator.getTranslator().step();
			}

		}
	}

	public void processEditedTriplesFromStart() {
		// Just to be sure not carrying bad categories forward.
		String x = fromStartDisplay.getText();

		Mark.say("Edited Start triples:", x);

		wiredTextDireclyBackField.setText(Start.getStart().generate(x));

		Sequence sequenceFromTriples = Start.getStart().processTripples(x);

		Mark.say("!!!!!!!!!!!!!!!!!", sequenceFromTriples.getElements().size());
		BasicTranslator.getTranslator().setInput(sequenceFromTriples);
		if (autoRunButton.isSelected()) {
			BasicTranslator.getTranslator().go();
		}
		else {
			BasicTranslator.getTranslator().step();
		}

	}

	public void processEditedTriplesFromGenesis() {
		String x = toStartDisplay.getText();
		// Mark.say("Edited Genesis triples:", x);
		String sentence = Start.getStart().generate(x);
		// Mark.say("Sentence:", sentence);
		wiredTextOutputField.setText(sentence);
	}

	public String extractTriples(String s) {
		StringBuffer input = new StringBuffer(s);
		StringBuffer result = new StringBuffer();
		int start, end;
		while ((start = input.indexOf("[")) >= 0) {
			end = input.indexOf("]", start);
			result.append(input.substring(start, end + 1) + "\n");
			input.delete(0, end + 1);
		}
		return result.toString();
	}

	public void processTriplesFromStart(Object o) {
		String triplesAsString = "";
		if (o instanceof String) {
			triplesAsString = o.toString();
			String x = Start.getStart().generate(triplesAsString);
			wiredTextDireclyBackField.setText(x);
		}
	}

	public void processTranslatorInput(Object o) {
		if (o instanceof Sequence) {
			Sequence s = (Sequence) o;
			String text = "";
			toStartDisplay.setText("");
			for (Entity t : s.getElements()) {
				// Mark.say("Received entity", t.asString());
				RoleFrame roleFrame = Generator.getGenerator().generateFromEntity(t);
				if (roleFrame != null) {
					String triples = roleFrame.getRendering();
					toStartDisplay.setText(toStartDisplay.getText() + enumerate(triples));
					text += Generator.getGenerator().generate(roleFrame) + "  ";
					wiredTextOutputField.setText(text);
				}
				else {
					text += "Unable to generate text.";
				}
			}
			wiredTextOutputField.setText(text);
		}
	}

	// Adds line feeds between ][
	private String enumerate(String triples) {
		StringBuffer buffer = new StringBuffer(triples);
		int index;
		while ((index = buffer.indexOf("][")) > 0) {
			buffer.replace(index, index + 2, "]\n[");
		}
		buffer.append("\n\n");
		return buffer.toString();
	}
	
	private class ButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			if (e.getSource() == stepButton) {
				int steps = BasicTranslator.getTranslator().getTransformations().size();
				BasicTranslator.getTranslator().step();
			}
			// Modified by phw to make retreat work. Actually, it is not
			// retreating, just reanalyzing up to previous step.
			else if (e.getSource() == retreatButton) {
				Mark.say("Retreat");
				// Translator.getTranslator().setInput(sequenceFromStart);

				int steps = BasicTranslator.getTranslator().getTransformations().size();

				sequenceFromStart = Start.getStart().parse(wiredTextDireclyBackField.getText());

				BasicTranslator.getTranslator().setInput(sequenceFromStart);

				Mark.say("Steps", steps);
				--steps;
				--steps;

				// Connections.getPorts(getWiredTextInputField()).transmit(sentence);
				for (int i = 0; i < steps; ++i) {
					BasicTranslator.getTranslator().step();
				}
			}
			else if (e.getSource() == runButton) {
				BasicTranslator.getTranslator().go();
			}
			else if (e.getSource() == processStartTripplesButton) {
				processEditedTriplesFromStart();
			}
			else if (e.getSource() == processGenesisTripplesButton) {
				processEditedTriplesFromGenesis();
			}
		}

		private String removeStars(String sentence) {
			while (true) {
				int l = sentence.length();
				char c = sentence.charAt(l - 1);
				if (c == '*' || c == ' ') {
					sentence = sentence.substring(0, l - 1);
				}
				else {
					break;
				}
			}
			return sentence;
		}
	}

	protected class PunctuationListener implements KeyListener {

		boolean debug = false;

		String steppers = "*";

		String runners = "!&";

		String parsers = "\n";

		String period = ".";

		/** Handle the key typed event from the text field. */
		public void keyTyped(KeyEvent e) {

			Mark.say(debug, "Key typed", wiredTextInputField.getText());
			String sent = null;
			char key = e.getKeyChar();
			if (parsers.indexOf(key) >= 0) {
				Mark.say(debug, "Parsers");
				BundleGenerator.purgeWordnetCache();
				String text = wiredTextInputField.getText();
				text = normalizePeriod(text);
				// getWiredTextInputField().setText(text);
				Mark.say(debug, "Storing at X", wiredTextInputField.getText());
				Preferences.userRoot().put(this.getClass().getName(), wiredTextInputField.getText());
				stimulate(text);
				if (autoRunButton.isSelected()) {
					runButton.doClick();
				}
			}
			else if (runners.indexOf(key) >= 0) {
				Mark.say(debug, "Runners");
				BasicTranslator.getTranslator().go();
				e.consume();
			}
			else if (steppers.indexOf(key) >= 0) {
				Mark.say(debug, "Steppers");
				if (countSteppers(wiredTextInputField.getText()) == 0) {
					Connections.getPorts(wiredTextInputField).transmit(wiredTextInputField.getText());
				}
				BasicTranslator.getTranslator().step();
			}
			else if (period.indexOf(key) >= 0) {
				Mark.say(debug, "Storing at Y", this.getClass().getName(), wiredTextInputField.getText());
				Preferences.userRoot().put(PageTranslatorGenerator.this.getClass().getName(), wiredTextInputField.getText());
			}
		}

		private int countSteppers(String text) {
			int count = 0;
			for (int s = 0; s < steppers.length(); ++s) {
				char c = steppers.charAt(s);
				for (int i = 0; i < text.length(); ++i) {
					if (c == text.charAt(i)) {
						++count;
					}
				}
			}
			return count;
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
		}
	}

	public static void stimulate(String s) {
		Switch.useStartCache.setSelected(false);
	}
	
	private static String normalizePeriod(String text) {
		if (text.isEmpty()) {
			return text;
		}
		while (text.lastIndexOf('.') == text.length() - 1) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	public static void main(final String[] args) {
		PageTranslatorGenerator page = new PageTranslatorGenerator();
		page.stimulate("A hawk flew to a tree.");
		JFrame frame = new JFrame();
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.getContentPane().add(page);
		 frame.setBounds(0, 0, 1400, 1000);
		 frame.setVisible(true);
	}

}
