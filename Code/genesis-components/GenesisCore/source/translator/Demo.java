package translator;

import gui.NewFrameViewer;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import utils.WiredTextField;

import connections.Connections;
import frames.entities.Entity;
import frames.entities.Function;

@SuppressWarnings("serial")
public class Demo extends JPanel {
	private final JButton stepButton = new JButton("Step forward");

	private final JButton retreatButton = new JButton("Retreat");

	private final JButton runButton = new JButton("Run");

	final HardWiredTranslator hardWiredTranslator = new HardWiredTranslator();

	private String sentence = "";

	public static final WiredTextField textField = new WiredTextField();

	public Demo() {
		stepButton.addActionListener(new ButtonActionListener());
		retreatButton.addActionListener(new ButtonActionListener());
		runButton.addActionListener(new ButtonActionListener());

		setLayout(new BorderLayout());
		textField.addKeyListener(new PunctuationListener());
		final NewFrameViewer viewer = new NewFrameViewer();
		// final LinkParser p = new LinkParser();

		// Connections.wire(textField, p);
		// Connections.wire(p, HardWiredTranslator.PARSE, hardWiredTranslator);
		Connections.wire(HardWiredTranslator.PROGRESS, hardWiredTranslator, viewer);
		// Connections.wire(p, viewer);

		this.add(viewer, BorderLayout.CENTER);
		this.add(textField, BorderLayout.SOUTH);
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 0));
		buttonPanel.add(stepButton);
		buttonPanel.add(retreatButton);
		buttonPanel.add(runButton);
		this.add(buttonPanel, BorderLayout.NORTH);
		textField.requestFocusInWindow();
	}

	private class ButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			if (e.getSource() == stepButton) {
				hardWiredTranslator.step();
			}
			// Modified by phw to make retreat work. Actually, it is not
			// retreating, just reanalyzing up to previous step.
			else if (e.getSource() == retreatButton) {
				int steps = hardWiredTranslator.getTransformations().size();
				--steps;
				--steps;
				String sentence = Demo.this.textField.getText();
				sentence = removeStars(sentence);
				Connections.getPorts(Demo.this.textField).transmit(sentence);
				for (int i = 0; i < steps; ++i) {
					hardWiredTranslator.step();
				}
			}
			else if (e.getSource() == runButton) {
				hardWiredTranslator.go();
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

		String steppers = "*";

		String runners = "!&";

		/** Handle the key typed event from the text field. */
		public void keyTyped(KeyEvent e) {
			String sent = null;
			char key = e.getKeyChar();
			if (steppers.indexOf(key) >= 0) {
				// System.out.println("Steppers: " +
				// countSteppers(textField.getText()));
				if (countSteppers(textField.getText()) == 0) {

					Connections.getPorts(textField).transmit(textField.getText());
				}
				hardWiredTranslator.step();
			}
			else if (runners.indexOf(key) >= 0) {
				hardWiredTranslator.go();
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

	public static void main(final String[] args) {
		final Demo d = new Demo();
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(d);
		frame.setSize(800, 600);
		frame.setVisible(true);

		Entity t = new Entity();
		System.out.println(new Function("foo", t));
	}

}
