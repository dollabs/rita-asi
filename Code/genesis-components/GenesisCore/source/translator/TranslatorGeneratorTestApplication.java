package translator;

import generator.*;
import gui.NewFrameViewer;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

import javax.swing.*;


import start.Start;
import utils.*;


import com.ascent.gui.frame.WFrameApplication;

import connections.*;
import constants.Lockup;
import constants.Switch;
import dictionary.BundleGenerator;
import frames.entities.Entity;
import frames.entities.Sequence;

@SuppressWarnings("serial")
public class TranslatorGeneratorTestApplication extends WFrameApplication implements WiredBox {

	private JPanel view;

	private JPanel buttonsAndCheckBoxes;

	private JPanel buttons;

	private JPanel checkBoxes;

	private JPanel mainPanel;

	private JPanel viewerPanel;

	private NewFrameViewer viewer;

	private WiredTextArea fromStartDisplay;

	private WiredTextArea toStartDisplay;

	private WiredTextField wiredTextInputField;

	private WiredTextField wiredTextDireclyBackField;

	private WiredTextField wiredTextOutputField;

	private JPanel textFields;

	private final JButton stepButton = new JButton("Step forward");

	private final JButton retreatButton = new JButton("Retreat");

	private final JButton runButton = new JButton("Run");

	private final JButton processStartTripplesButton = new JButton("Process");

	private final JButton processGenesisTripplesButton = new JButton("Process");

	private final CheckBoxWithMemory autoRunButton = new CheckBoxWithMemory("Auto run");

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

	public TranslatorGeneratorTestApplication() {
		ButtonActionListener l = new ButtonActionListener();
		stepButton.addActionListener(l);
		retreatButton.addActionListener(l);
		runButton.addActionListener(l);
		processStartTripplesButton.addActionListener(l);
		processGenesisTripplesButton.addActionListener(l);

		getWiredTextInputField().addKeyListener(new PunctuationListener());

		getView().add(getTextFields(), BorderLayout.NORTH);

		getWiredTextInputField().requestFocusInWindow();

		Connections.wire(getWiredTextInputField(), INPUT, this);

		// Connections.wire(getWiredTextInputField(), Start.SENTENCE, Start.getStart());
		//
		// Connections.wire(Start.PARSE, Start.getStart(), Translator.PARSE, Translator.getTranslator());
		//
		// Connections.wire(EDITED_TRIPLES, this, Translator.PARSE, Translator.getTranslator());
		//
		// Connections.wire(Start.RAW_TRIPLES, Start.getStart(), getFromStartDisplay());
		//
		Connections.wire(BasicTranslator.PROGRESS, BasicTranslator.getTranslator(), getViewer());
		//
		// Connections.wire(Start.PARSE, Start.getStart(), getViewer());
		//
		// Connections.wire(Start.RAW_TRIPLES, Start.getStart(), TRIPLES_FROM_START, this);
		//
		// // Connections.wire(EDITED_PARSE, this, TRIPLES_FROM_START, this);
		//
		// Connections.getPorts(this).addSignalProcessor(TRIPLES_FROM_START, "processTriplesFromStart");
		//
		Connections.wire(BasicTranslator.DEBUGGING_RESULT, BasicTranslator.getTranslator(), FROM_TRANSLATOR, this);
		//
		Connections.getPorts(this).addSignalProcessor(FROM_TRANSLATOR, this::processTranslatorInput);

		Connections.getPorts(this).addSignalProcessor(INPUT, this::processInput);

		String test = Preferences.userRoot().get(this.getClass().getName(), "Type test sentence here");

		// Mark.say("Remembered via", this.getClass().getName(), Preferences.userRoot().get(this.getClass().getName(),
		// "Type test sentence here"));

		getWiredTextInputField().setText(test + ".");

		getViewer().clear();

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

			getFromStartDisplay().setText(extractTriples(rawText));

			getWiredTextDireclyBackField().setText(Start.getStart().generate(triples));

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
		String x = getFromStartDisplay().getText();

		Mark.say("Edited Start triples:", x);

		getWiredTextDireclyBackField().setText(Start.getStart().generate(x));

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
		String x = getToStartDisplay().getText();
		// Mark.say("Edited Genesis triples:", x);
		String sentence = Start.getStart().generate(x);
		// Mark.say("Sentence:", sentence);
		getWiredTextOutputField().setText(sentence);
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
			getWiredTextDireclyBackField().setText(x);
		}
	}

	public void processTranslatorInput(Object o) {
		if (o instanceof Sequence) {
			Sequence s = (Sequence) o;
			String text = "";
			getToStartDisplay().setText("");
			for (Entity t : s.getElements()) {
				// Mark.say("Received entity", t.asString());
				RoleFrame roleFrame = Generator.getGenerator().generateFromEntity(t);
				if (roleFrame != null) {
					String triples = roleFrame.getRendering();
					getToStartDisplay().setText(getToStartDisplay().getText() + enumerate(triples));
					text += Generator.getGenerator().generate(roleFrame) + "  ";
					getWiredTextOutputField().setText(text);

				}
				else {
					text += "Unable to generate text.";
				}
			}
			getWiredTextOutputField().setText(text);
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

	public JPanel getButtonsAndCheckBoxes() {
		if (buttonsAndCheckBoxes == null) {
			buttonsAndCheckBoxes = new JPanel();
			buttonsAndCheckBoxes.setLayout(new GridLayout(0, 1));
			buttonsAndCheckBoxes.add(getCheckBoxes());
			buttonsAndCheckBoxes.add(getButtons());
		}
		return buttonsAndCheckBoxes;
	}

	public JPanel getButtons() {
		if (buttons == null) {
			buttons = new JPanel();
			buttons.setLayout(new GridLayout(1, 0));
			buttons.add(stepButton);
			buttons.add(retreatButton);
			buttons.add(runButton);
		}
		return buttons;
	}

	public JPanel getCheckBoxes() {
		if (checkBoxes == null) {
			new Lockup(Switch.showTranslationDetails, Switch.showTranslationDetails2);
			new Lockup(Switch.useStartBeta, Switch.useStartBeta2);
			checkBoxes = new JPanel();
			checkBoxes.setLayout(new GridLayout(1, 0));
			checkBoxes.add(autoRunButton);
			checkBoxes.add(Switch.showTranslationDetails2);
			checkBoxes.add(Switch.useStartBeta2);
		}
		return checkBoxes;
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

			Mark.say(debug, "Key typed", getWiredTextInputField().getText());
			String sent = null;
			char key = e.getKeyChar();
			if (parsers.indexOf(key) >= 0) {
				Mark.say(debug, "Parsers");
				BundleGenerator.purgeWordnetCache();
				String text = getWiredTextInputField().getText();
				text = normalizePeriod(text);
				// getWiredTextInputField().setText(text);
				Mark.say(debug, "Storing at X", getWiredTextInputField().getText());
				Preferences.userRoot().put(this.getClass().getName(), getWiredTextInputField().getText());
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
				if (countSteppers(getWiredTextInputField().getText()) == 0) {
					Connections.getPorts(getWiredTextInputField()).transmit(getWiredTextInputField().getText());
				}
				BasicTranslator.getTranslator().step();
			}
			else if (period.indexOf(key) >= 0) {
				Mark.say(debug, "Storing at Y", this.getClass().getName(), getWiredTextInputField().getText());
				Preferences.userRoot().put(TranslatorGeneratorTestApplication.this.getClass().getName(), getWiredTextInputField().getText());
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

	public void stimulate(String s) {
		Switch.useStartCache.setSelected(false);
	}

	public JPanel getView() {
		if (view == null) {
			view = new JPanel();
			view.setLayout(new BorderLayout());
			view.add(getMainPanel(), BorderLayout.CENTER);
		}
		return view;
	}

	public JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridLayout(1, 0));

			JPanel left = new JPanel();
			left.setLayout(new BorderLayout());
			left.add(getFromStartDisplay(), BorderLayout.CENTER);
			left.add(processStartTripplesButton, BorderLayout.SOUTH);

			JPanel right = new JPanel();
			right.setLayout(new BorderLayout());
			right.add(getToStartDisplay(), BorderLayout.CENTER);
			right.add(processGenesisTripplesButton, BorderLayout.SOUTH);

			mainPanel.add(left);
			mainPanel.add(getViewerPanel());
			mainPanel.add(right);
		}
		return mainPanel;
	}

	public WiredTextArea getFromStartDisplay() {
		if (fromStartDisplay == null) {
			fromStartDisplay = new WiredTextArea();
			fromStartDisplay.setBackground(Color.YELLOW);
			fromStartDisplay.setFont(new Font("Dialog", textAreaWeight, textAreaSize));
		}
		return fromStartDisplay;
	}

	public WiredTextArea getToStartDisplay() {
		if (toStartDisplay == null) {
			toStartDisplay = new WiredTextArea();
			toStartDisplay.setBackground(Color.CYAN);
			toStartDisplay.setFont(new Font("Dialog", textAreaWeight, textAreaSize));
		}
		return toStartDisplay;
	}

	public NewFrameViewer getViewer() {
		if (viewer == null) {
			viewer = new NewFrameViewer();
			viewer.setPreferredSize(new Dimension(1000, 1000));
		}
		return viewer;
	}

	public JPanel getTextFields() {
		if (textFields == null) {

			JLabel input = new JLabel("Input:          ");
			JLabel direct = new JLabel("Short circuit:  ");
			JLabel output = new JLabel("Via Genesis:    ");

			textFields = new JPanel();
			textFields.setLayout(new GridLayout(0, 1));
			JPanel top = new JPanel();
			top.setLayout(new BorderLayout());

			input.setOpaque(true);
			input.setBackground(Color.WHITE);
			input.setFont(new Font("Courier", textFieldWeight, textFieldSize));
			top.add(input, BorderLayout.WEST);
			top.add(getWiredTextInputField(), BorderLayout.CENTER);
			textFields.add(top);

			JPanel middle = new JPanel();
			middle.setLayout(new BorderLayout());

			direct.setOpaque(true);
			direct.setBackground(Color.WHITE);
			direct.setFont(new Font("Courier", textFieldWeight, textFieldSize));
			middle.add(direct, BorderLayout.WEST);
			middle.add(getWiredTextDireclyBackField(), BorderLayout.CENTER);
			textFields.add(middle);

			JPanel bottom = new JPanel();
			bottom.setLayout(new BorderLayout());

			output.setOpaque(true);
			output.setBackground(Color.WHITE);
			output.setFont(new Font("Courier", textFieldWeight, textFieldSize));
			bottom.add(output, BorderLayout.WEST);
			bottom.add(getWiredTextOutputField(), BorderLayout.CENTER);
			textFields.add(bottom);
		}
		return textFields;
	}

	public WiredTextField getWiredTextInputField() {
		if (wiredTextInputField == null) {
			wiredTextInputField = new WiredTextField();
			wiredTextInputField.setFont(new Font("Dialog", textFieldWeight, textFieldSize));
			Mark.say("Setting text field to", Preferences.userRoot().get(this.getClass().getName(), ""));
			wiredTextInputField.setText(Preferences.userRoot().get(this.getClass().getName(), ""));
		}
		return wiredTextInputField;
	}

	public WiredTextField getWiredTextDireclyBackField() {
		if (wiredTextDireclyBackField == null) {
			wiredTextDireclyBackField = new WiredTextField();
			wiredTextDireclyBackField.setFont(new Font("Dialog", textFieldWeight, textFieldSize));
		}
		return wiredTextDireclyBackField;
	}

	public WiredTextField getWiredTextOutputField() {
		if (wiredTextOutputField == null) {
			wiredTextOutputField = new WiredTextField();
			wiredTextOutputField.setFont(new Font("Dialog", textFieldWeight, textFieldSize));
		}
		return wiredTextOutputField;
	}

	public JPanel getViewerPanel() {
		if (viewerPanel == null) {
			viewerPanel = new JPanel();
			viewerPanel.setLayout(new BorderLayout());
			viewerPanel.add(getViewer(), BorderLayout.CENTER);
			viewerPanel.add(getButtonsAndCheckBoxes(), BorderLayout.SOUTH);
		}
		return viewerPanel;
	}

	@Override
	public String getAccessType() {
		return null;
	}

	@Override
	public void restoreTaskBarImage() {

	}

	@Override
	public void restoreTaskBarTitle() {

	}

	@Override
	public String getNavigationBarItem() {
		return "Test translator/generator";
	}

	@Override
	public String getNavigationBarItemHelp() {
		return "Test Start parser, Genesis translator, Genesis generator, Start generator";
	}

	@Override
	public String getName() {
		return "Parser/Generator test";
	}

	private String normalizePeriod(String text) {
		if (text.isEmpty()) {
			return text;
		}
		while (text.lastIndexOf('.') == text.length() - 1) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	public static void main(final String[] args) {
		// TranslatorGeneratorTestApplication d = new TranslatorGeneratorTestApplication();
		// // d.stimulate("A hawk flew to a tree.");
		// final JFrame frame = new JFrame();
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.getContentPane().add(d.getView());
		// frame.setSize(800, 600);
		// frame.setVisible(true);
		Mark.say("Normalized", new TranslatorGeneratorTestApplication().normalizePeriod("Hello world"));
	}

}
