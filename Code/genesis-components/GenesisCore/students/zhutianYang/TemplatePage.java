package zhutianYang;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import utils.Html;
import utils.Colors;
import utils.Mark;
import connections.Connections;
import connections.TextEntryBox;
import dictionary.WordNet;
import frames.entities.Bundle;

/*
 * Created on Oct 10, 2018 @author zty
 */

public class TemplatePage extends JPanel {
	
	// top bar: option buttons
	JPanel top = new JPanel();
	JTextArea definition = new JTextArea();
	JScrollPane scroller;
	
	String defaultFont = "Lucida Console";
	int defaultFontSize = 20;

	public TemplatePage() {
		super();
		this.setLayout(new BorderLayout());
		scroller = new JScrollPane(definition);
		this.add(scroller, BorderLayout.CENTER);
		this.add(top, BorderLayout.NORTH);

	}
	
	public static void main(String[] ignore) {
		PageStoryAligner page = new PageStoryAligner();
		  JFrame frame = new JFrame();
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  frame.getContentPane().add(page);
		  frame.setBounds(ZPage.windowSize);
		  frame.setVisible(true);
	}

}
