package utils;

import java.awt.Color;
import java.awt.event.*;

import javax.swing.JTextField;

import connections.*;

@SuppressWarnings("serial")
public class WiredTextField extends JTextField implements WiredBox, KeyListener {

	String was = "";

	public WiredTextField() {
		super();
		addKeyListener(this);
	}

	public void keyPressed(final KeyEvent e) {
	}

	public void keyReleased(final KeyEvent e) {
		String is = this.getText();
		final char c = e.getKeyChar();
		if (stripPunctuation(was).equals(stripPunctuation(is))) {
			if (c == '.' || c == '!' || c == '?' || c == '\n') {
				// if (is.endsWith(".") || is.endsWith("!") || is.endsWith("?")) {
				Connections.getPorts(this).transmit(stripPunctuation(is));
			}
		}
	}

	public void keyTyped(final KeyEvent e) {
		final char c = e.getKeyChar();
		was = getText(); // this handler is called before c
		                 // is added to the field's text
		if (c == ' ') {
			// Connections.getPorts(this).transmit(text);

		}

		// if (c == '?' || c == '!' || KeyEvent.getKeyText(c).equalsIgnoreCase("enter")) {
		// Connections.getPorts(this).transmit(stripPunctuation(text));
		// }

		if (KeyEvent.getKeyText(c).equalsIgnoreCase("escape")) {
			setText("");

		}
	}

	private String stripPunctuation(String text) {
		if (text.isEmpty()) {
			return text;
		}
		else if (".?!*".indexOf(text.charAt(text.length() - 1)) >= 0) {
			return stripPunctuation(text.substring(0, text.length() - 1));
		}
		return text;
	}

	public void stimulate(String s) {
		setText(s);
		Connections.getPorts(this).transmit(stripPunctuation(s));
	}

}
