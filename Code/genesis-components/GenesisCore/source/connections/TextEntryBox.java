package connections;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import utils.*;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class TextEntryBox extends JTextField implements WiredBox {

	public static final String CLEAR = "clear";

	public static final String PRIMER = "primer";

	public static final String REPLY = "reply";

	private static final int desiredFontSize = 35;

	public TextEntryBox() {
		setFont(new Font(getFont().getFamily(), Font.BOLD, desiredFontSize));
		// setEditable(true);
		addKeyListener(new PunctuationListener());
		setBackground(Colors.TEXT_COLOR);
		setOpaque(true);
		normal();
		Connections.getPorts(this).addSignalProcessor(CLEAR, "clear");
		Connections.getPorts(this).addSignalProcessor(PRIMER, "prime");
		Connections.getPorts(this).addSignalProcessor(REPLY, "reply");
	}

	public void clear(Object ignore) {
		setText("");
	}

	public void paintComponent(Graphics g) {
		String text = getText();
		int boxWidth = getWidth();
		if (boxWidth == 0) {
			return;
		}
		Font font = getFont();
		String family = font.getFamily();
		while (true) {
			int stringWidth = g.getFontMetrics().stringWidth(text);
			int fontSize = g.getFont().getSize();
			// System.out.println("Hello: " + stringWidth + ", " + boxWidth +
			// ", " + fontSize);
			if (stringWidth < 7 * boxWidth / 8) {
				if (fontSize > desiredFontSize - 2) {
					break;
				}
				fontSize += 2;
				font = new Font(family, Font.BOLD, fontSize);

			}
			else if (stringWidth > boxWidth) {
				if (fontSize <= 5) {
					break;
				}
				fontSize -= 2;
				font = new Font(family, Font.BOLD, fontSize);
			}
			else {
				break;
			}
			setFont(font);
			g.setFont(font);
		}
		super.paintComponent(g);
	}

	public void zero() {
		setPreferredSize(new Dimension(0, 0));
		setMaximumSize(new Dimension(0, 0));

	}

	public void normal() {
		setPreferredSize(new Dimension(100, 60));
		setMaximumSize(new Dimension(1000, 60));
	}

	public void reply(Object o) {
		this.setBackground(Colors.REPLY_COLOR);
		if (o != null && o instanceof String) {
			String message = (String) o;
			this.setText(message);
			// Connections.getPorts(TextEntryBox.this).transmit(CLEAR, message);
			// Connections.getPorts(TextEntryBox.this).transmit(message);
		}
	}

	public void setText(String message) {
		// Mark.say("Message is " + message);
		super.setText(correctSeparatedPunctuation(message));
	}

	public void prime(Object o) {

		this.setBackground(Colors.TEXT_COLOR);
		if (o instanceof String) {
			String message = (String) o;
			if (message.trim().isEmpty()) {
				return;
			}
			// Mark.say("Message is", message);
			message = correctSeparatedPunctuation(message);
			// Mark.say("Message is", message);
			Connections.getPorts(this).transmit(CLEAR, message);
			Connections.getPorts(this).transmit(message);
			try {
				this.setText(message);
			}
			catch (Exception e) {
				System.err.println("TextEntryBox.prime unable to set text '" + o + "'");
				// e.printStackTrace();
			}
		}
		int x = 0;
	}

	private String correctSeparatedPunctuation(String message) {
		// terminators = "?.!\n";
		StringBuffer buffer = new StringBuffer(message);
		int index;
		while ((index = buffer.indexOf(" .")) >= 0) {
			buffer.deleteCharAt(index);
		}
		while ((index = buffer.indexOf("..")) >= 0) {
			buffer.deleteCharAt(index);
		}
		while ((index = buffer.indexOf(" ?")) >= 0) {
			buffer.deleteCharAt(index);
		}
		while ((index = buffer.indexOf(" ,")) >= 0) {
			buffer.deleteCharAt(index);
		}
		while ((index = buffer.indexOf(" ;")) >= 0) {
			buffer.deleteCharAt(index);
		}
		while ((index = buffer.indexOf(" \n")) >= 0) {
			buffer.deleteCharAt(index);
		}
		return buffer.toString();
	}

	protected class PunctuationListener implements KeyListener {

		boolean debug = true;

		String terminators = "?.!\n";

		String spaces = " \n\t";

		/** Handle the key-pressed event from the text field. */
		public void keyPressed(KeyEvent e) {
		}

		/** Handle the key-released event from the text field. */
		public void keyReleased(KeyEvent e) {
		}

		/** Handle the key typed event from the text field. */
		public void keyTyped(KeyEvent e) {

			char key = e.getKeyChar();
			// Mark.say("Key typed", key);
			TextEntryBox.this.setBackground(Colors.TEXT_COLOR);
			if (terminators.indexOf(key) >= 0) {
				// String message = stripPunctuation(getText().toLowerCase());
				String message = getText() + key;
				Connections.getPorts(TextEntryBox.this).transmit(CLEAR, message);
				Connections.getPorts(TextEntryBox.this).launch(message);
			}
			else if (KeyEvent.getKeyText(e.getKeyChar()).equalsIgnoreCase("Escape")) {
				TextEntryBox.this.setText("");
			}
		}
	}

	public static void main(String[] ignore) {
		TextEntryBox field = new TextEntryBox();
		// JLabel field = new JLabel();
		// JTextArea field = new JTextArea();
		// field.setText("<html><i>Hello world</i></html>");
		field.setText("Hello world!  How are you today?");
		JFrame frame = new JFrame();
		frame.getContentPane().add(field);
		frame.setBounds(0, 0, 400, 90);
		frame.setVisible(true);
		//

		System.out.println("Correction " + new TextEntryBox().correctSeparatedPunctuation("Hello world ."));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -842253960229812262L;

}
