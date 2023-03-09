package utils;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.*;

import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Entity;

/**
 * Copyright 2003 Sun Microsystems, Inc. See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */

import generator.Generator;
import genesis.GenesisGetters;
import gui.*;
import utils.Html;
import translator.Translator;
import utils.*;

// import de.humatic.dsj.*;

public class Talker extends JPanel implements WiredBox {

	private boolean processWithDragon = false;

	public static final String PREDICTION = "prediction";

	public static final String CLEAR = "clear";

	public static final String SPEAK = "speak";

	private JLabel label;

	private static int fileNumber = 0;

	private static Talker talker;

	// DSMovie player;

	private long previousInput;

	public static Talker getTalker() {
		if (talker == null) {
			talker = new Talker();
		}
		return talker;
	}

	public Talker() {
	}

	public Talker(JCheckBox checkBox) {
		// setup();
		this.setLayout(new BorderLayout());
		this.checkBox = checkBox;
		add(getLabel(), BorderLayout.CENTER);
		Connections.getPorts(this).addSignalProcessor(PREDICTION, this::predict);
		Connections.getPorts(this).addSignalProcessor(CLEAR, this::clear);
		Connections.getPorts(this).addSignalProcessor(SPEAK, this::speakOnly);
		Connections.getPorts(this).addSignalProcessor(this::speak);
	}

	// private Voice voice;

	GenesisGetters tabs;

	private JCheckBox checkBox;

	public GenesisGetters getTabs() {
		return tabs;
	}

	public void setTabs(GenesisGetters tabs) {
		this.tabs = tabs;
	}

	public JLabel getLabel() {
		if (label == null) {
			label = new JLabel("", SwingConstants.LEFT);
			// label.setBackground(Color.YELLOW);
			label.setBackground(Color.PINK);
			label.setOpaque(true);
			Font font = label.getFont();
			label.setFont(new Font(font.getFamily(), font.getStyle(), 25));
		}
		return label;
	}

	private void useless(Vector<String> v) {
		for (String string : v) {
			// System.out.println(string);
		}
	}

	// public void listAllVoices() {
	// System.out.println();
	// System.out.println("All voices available:");
	// VoiceManager voiceManager = VoiceManager.getInstance();
	// Voice[] voices = voiceManager.getVoices();
	// for (int i = 0; i < voices.length; i++) {
	// System.out.println("    " + voices[i].getName() + " (" +
	// voices[i].getDomain() + " domain)");
	// }
	// }

	public void clear(Object object) {
		Connections.getPorts(this).transmit(new BetterSignal("Speech", TextViewer.CLEAR));
	}

	public void predict(Object text) {
		Mark.say("Receiving", text, "in predict");
		speakAux(text, true);
	}

	public synchronized void speak(Object object) {
		speakAux(object, true);

	}

	public synchronized void speakOnly(Object object) {
		Mark.say("Receiving", object, "in speakOnly");
		speakAux(object, false);
	}

	public synchronized void speakAux(Object object, boolean write) {

		boolean debug = false;

		long currentInput = System.currentTimeMillis();
		// If less than 1 second, ignore
		if (false && currentInput - previousInput < 1000) {
			Mark.say("Too close for more English");
			return;
		}
		previousInput = currentInput;

		double offset = 0;
		if (processWithDragon) {
			newSpeak(object);
			return;
		}
		if (getTabs() != null) {
			// getTabs().getWindowGroupManager().setGuts(getTabs().getLeftPanel(), getTabs().getLightBulbViewer());
			// getTabs().getInputTabbedPane().setSelectedComponent(getTabs().getTalker());
		}
		String text = "";
		// This piece deprecated; now handled by DisgustingMoebiousTranslator
		if (object instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) object;
			// Second argument may be String or thing
			Object firstArgument = signal.get(0, Object.class);
			Object secondArgument = signal.get(1, Object.class);
			if (secondArgument instanceof String) {
				if (secondArgument == CLEAR) {
					clear(secondArgument);
					return;
				}
				text = signal.get(1, String.class);
			}
			else if (secondArgument instanceof Entity) {
				// Mark.say("First argument class is",
				// firstArgument.getClass());
				try {
					if (firstArgument instanceof Double) {
						offset = (Double) firstArgument;
					}
					text = Generator.getGenerator().generate((Entity) secondArgument);
					text = Html.strip(text);
				}
				catch (Exception e) {
					Mark.say("Unable to generate English from", ((Entity) secondArgument).asString());
				}
			}
		}
		else if (object instanceof Entity) {
			try {
				text = Generator.getGenerator().generate((Entity) object);
				text = Html.strip(text);
			}
			catch (Exception e) {
				Mark.say("Unable to generate English from", ((Entity) object).asString());
			}
		}
		else if (object instanceof String) {
			text = Html.strip(text);
			text = compress((String) object);
		}
		else {
			Mark.err("Argument", object, "to Talker.speak is not a Thing or a String");
			return;
		}
		if (write) {

			Connections.getPorts(this).transmit(new BetterSignal("Speech", (offset > 0 ? String.format("%.1f ", offset) : "") + text));
		}

		if (offset > 0 && !(text.indexOf("give") > 0)) {
			return;
		}

		if (checkBox == null || checkBox.isSelected()) {

			// Thread.sleep(500);
			// File file = new File(System.getProperty("user.home") +
			// "/textToSpeechFile" + fileNumber + ".txt");
			File file = new File(System.getProperty("user.home") + "/textToSpeechFile" + ".txt");
			if (file.exists()) {
				file.delete();
			}

			String strippedText = Html.strip(text);

			// Drip pan, prevents two-times saying.

			if (strippedText.trim().equals("clear")) {
				return;
			}

			TextIO.writeStringToFile(strippedText, file);
			// PrintWriter writer = new PrintWriter(file);
			// writer.println(strippedText);
			// writer.close();

			// ProcessBuilder pb = new
			// ProcessBuilder("\"c:\\program files\\jampal\\ptts\" -voice \"Microsoft Mike\" -u "
			// + file.getPath());

			String e = "\"c:\\program files\\jampal\\ptts\" -u " + file.getPath();

			Mark.say(debug, "Text:", strippedText);
			Mark.say(debug, "Command:", e);

			// if (Switch.useFestival.isSelected()) {
			// e = "echo \"" + strippedText + "\" | C:\\festival\\src\\main\\festival --tts";
			// }

			boolean won = WindowsConnection.run(e);

			// ProcessExternally.processFileExternally(e);
			// pb.start();
			if (!won) {
				// e.printStackTrace();
				String message = "If you want to hear spoken text, install Jampal. ";
				message += "\nYou can find it on the web by searching for Jampal. ";
				message += "\nLoad it in the recommended directory.";
				message += "\nOtherwise, you should probably shut off speech output.";
				System.err.println(message);
			}
		}

		// PopupFactory factory = PopupFactory.getSharedInstance();
		// JLabel anotherLabel= new JLabel(format(text));
		// Popup popup = factory.getPopup(getTabs(), getLabel(), x, y);
		// popup.show();
		// voice.speak(text);
		// try {
		// java.lang.Thread.sleep(0);
		// }
		// catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// getLabel().setText("");
		// popup.hide();

	}

	// private Cons formatForText(String text) {
	// return L.list(TextViewer.PARAGRAPH, addPeriod(text));
	// }

	private String formatForSpeech(String text) {
		return addPeriod(text);
	}

	public static String addPeriod(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		else if (".!?".indexOf(s.charAt(s.length() - 1)) < 0) {
			return s + '.';
		}
		return s;
	}

	private String compress(String s) {
		StringBuffer b = new StringBuffer(s);
		int i;
		while ((i = b.indexOf("  ")) > 0) {
			b.deleteCharAt(i);
		}
		return b.toString().trim();
	}

	// private void setup() {
	// listAllVoices();
	// String voiceName = "kevin16";
	// System.out.println("Using voice: " + voiceName);
	// VoiceManager voiceManager = VoiceManager.getInstance();
	// voice = voiceManager.getVoice(voiceName);
	// if (voice == null) {
	// System.err.println("Cannot find a voice named " + voiceName +
	// ".  Please specify a different voice.");
	// return;
	// }
	// voice.allocate();
	//
	// }

	class TestBox extends AbstractWiredBox {
		public TestBox() {
			super("Test box");
		}

		public void test() {
			Connections.getPorts(this).transmit("I expect that a bird flew");
			Connections.getPorts(this).transmit("Hello world");
		}
	}

	public void setFrame(JFrame frame) {

	}

	public void sleep(int i) {
		try {
			Thread.sleep(i);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sleep(String minis) {
		try {
			int time = Integer.parseInt(minis);
			sleep(time);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public synchronized void newSpeak(Object object) {
		Mark.say("Entering newSpeak");
		if (getTabs() != null) {
			// getTabs().getWindowGroupManager().setGuts(getTabs().getLeftPanel(), getTabs().getLightBulbViewer());
			// getTabs().getInputTabbedPane().setSelectedComponent(getTabs().getTalker());
		}
		String text = "";
		if (object instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) object;
			text = signal.get(1, String.class);
		}
		else if (object instanceof Entity) {
			try {
				text = Generator.getGenerator().generate((Entity) object);
			}
			catch (Exception e) {
				Mark.say("Unable to generate English from", ((Entity) object).asString());
			}
		}
		else if (object instanceof String) {
			text = (String) object;
		}
		else {
			Mark.err("Argument", object, "to Talker.speak is not a Thing or a String");
			return;
		}
		Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Speech");
		Connections.getPorts(this).transmit(text);
		if (checkBox == null || checkBox.isSelected()) {
			text = Html.strip(text);
			text = compress((String) text);
			try {
				// Mark.say("The text for Dragon Systems is", text);
				processWaveRequest(text);
			}
			catch (Exception e) {
				e.printStackTrace();
				String message = "If you want to hear spoken text, install Jampal. ";
				message += "\nYou can find it on the web by searching for Jampal. ";
				message += "\nLoad it in the recommended directory.";
				message += "\nOtherwise, you should probably shut off voice output.";
				System.err.println(message);
			}
		}

	}

	protected void processWaveRequest(String probe) {
		// if (player != null) {
		// player.stop();
		// player.dispose();
		// }

		// Mark.say(P.getP().showStartProcessingDetails.get(), "Sending \n" +
		// probe + "\nto Start");
		String urlString = "http://people.csail.mit.edu/cyphers/cgi/zed.cgi";
		try {

			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			String command = "synth_string=" + URLEncoder.encode(probe, "UTF-8");
			// Mark.say("Output:", command);
			out.write(command);

			out.close();
			// Mark.say("Output closed");

			InputStream in = connection.getInputStream();

			int input;

			File file = new File(System.getProperty("user.home") + "/textToSpeechFile" + ".wav");

			// File file = new File("c:/output/speech" + ".wav");

			FileOutputStream outputFile = new FileOutputStream(file);

			while ((input = in.read()) != -1) {
				outputFile.write(input);
			}

			in.close();
			outputFile.flush();
			outputFile.close();

			// Desktop.getDesktop().open(file);

			// player = new DSMovie(file.getPath(), DSFiltergraph.RENDER_NATIVE, null);
			//
			// new SwingMovieController(player);

			// ProcessExternally.processFileExternally(file.getPath());
			// Mark.say("Flushed, closed, and spoken");
			// Mark.say("Returned from Start", buffer.toString());
			// return buffer;

		}
		catch (MalformedURLException e) {
			Mark.err("Evidently bad url");
		}
		catch (IOException e) {
			Mark.err("Evidently not connected to web or START is down");
			e.printStackTrace();
		}
		catch (Exception e) {
			Mark.err("Evidently unable to process '" + probe + "'");
		}
	}

	public static void main(String[] args) throws Exception {
		Entity t = Translator.getTranslator().translate("A dog gave a ball to a cat");
		Entity t2 = t.getElements().get(0);
		Mark.say("Argument is", t2.asString());
		String s = Generator.getGenerator().generate(t2);
		Mark.say("English is", s);
		Talker.getTalker().speak(t2);
		// Talker.getTalker().newSpeak(t2);
		// Thread.sleep(3000);
		// Talker.getTalker().newSpeak(t2);

	}
}
