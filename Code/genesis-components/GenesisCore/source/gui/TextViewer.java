package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;

import javax.swing.*;


import utils.*;
import connections.*;
import constants.Markers;

/*
 * Created on Jun 15, 2009
 * @author phw
 */

public class TextViewer extends JScrollPane implements WiredBox {

	public static final String CLEAR = "clear";

	public static final String REPLY = "reply";

	// public static final String TITLE = "title";

	// public static final String BOLD = "bold";

	public static final String TEXT = "text";

	// public static final String PARAGRAPH = "paragraph";

	private String text = "";

	private String header = "<html><body>";

	private String trailer = "</body></html>";

	private JTextPane label = new JTextPane();

	private String previousText;

	private TabbedTextViewer container;

	// private String mode = TEXT;

	public TextViewer() {
		label.setPreferredSize(new Dimension(300, 2000));
		label.setContentType("text/html");
		this.setViewportView(label);
		setName("Text viewer");
		setOpaque(true);
		Connections.getPorts(this).addSignalProcessor(this::process);
		Connections.getPorts(this).addSignalProcessor(Markers.RESET, this::clearText);
	}

	public void process(Object o) {
		// Mark.say("Processing", o, "in text viewer via call through direct wire", o.getClass());
		processViaDirectCall(o);
	}

	public void processViaDirectCall(Object o) {
		if (o == CLEAR) {
			clear();
			return;
		}
		// Mark.say("Adding", o);
		addText((String) o);
	}

	public TextViewer(TabbedTextViewer tabbedTextViewerX) {
		this();
		container = tabbedTextViewerX;
	}

	public void clearText(Object object) {
		clear();
	}

	public void replyText(Object object) {
		clear();
		Mark.say("Object is " + object);
		addText(object);
		label.setBackground(Colors.REPLY_COLOR);
	}

	public void addText(Object o) {
		label.setBackground(Color.WHITE);
		String s = o.toString();
		// Drip pan
		if (s.equals(previousText)) {
			// return;
		}
		previousText = s.toString();
		// text += Punctuator.addPeriod(s) + "\n";
		// text += Punctuator.addPeriod(s);
		
		// ---------------------------------------------------------
		// + "\n" and conditions added by Zhutian on 26 Mar 2019 for AAAI symposium demo
		// ---------------------------------------------------------
		if(zhutianYang.StoryAligner.storyNewLine) {
			String newText = Punctuator.addSpace(s);
			if(getName().contains("Commonsense knowledge")) {
				if(newText.contains("if ") || newText.contains("If "))
					text += newText + "\n";
			} else if (getName().contains("Story")) {
				if(!newText.contains(Markers.START_STORY_TEXT))
					text += newText + "\n";
			} else {
				text += newText + "\n";
			}
		} else {
			 text += Punctuator.addSpace(s); 
			// Mark.say("Setting text", text); 
		}	
		// ---------------------------------------------------------
		setText(text);
		scrollToEnd();
	}

	private void setText(String s) {
		String contents = header + Html.normal(s) + trailer;
		String stuff = Html.convertLf(contents);
		// Mark.say("Html text is", stuff);
		try {
			label.setText(stuff);
		}
		catch (Exception e) {
			e.printStackTrace();
			Mark.say("Error!!!\n>>", s, "\n>>", contents, "\n>>", stuff);
		}
	}

	// private void tickle() {
	// if (container != null) {
	// container.tickle(getName(), this);
	// }
	// }

	private void scrollToEnd() {
		label.selectAll();
		int x = label.getSelectionEnd();
		label.select(x, x);
	}

	public void clear() {
		text = "";
		setText(text);
	}

	public void addContentListener(MouseAdapter l) {
		label.addMouseListener(l);
	}

}
/*
 * Connections.getPorts(this).addSignalProcessor(TITLE, "addTitle");
 * Connections.getPorts(this).addSignalProcessor(PARAGRAPH, "addParagraph");
 * Connections.getPorts(this).addSignalProcessor(CLEAR, "clearText");
 * Connections.getPorts(this).addSignalProcessor(REPLY, "replyText");
 */