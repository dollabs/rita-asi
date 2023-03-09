package gui;

import gui.WiredPanel;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.*;

import translator.Distributor;
import connections.Connections;
import frames.entities.Entity;

/*
 * The Panel that is displayed in the gui for talking back.
 */
public class TalkBackViewer extends WiredPanel {

	private JTextArea textArea = new JTextArea();

	private JScrollPane scroller;

	private Vector<String> sentences = new Vector<String>();

	public TalkBackViewer() {
		this.setLayout(new BorderLayout());
		scroller = new JScrollPane(textArea);
		this.add(scroller);
		// now connect to the translator
		Connections.getPorts(this).addSignalProcessor("add");
	}
	
	public void add(Object input) {
		// System.out.println("TalkBackViewer receiving sentence");
		String sentence = (String) input;
		sentences.add(sentence);
		this.display();
	}

	private void display() {
		String text = "Sentences: \n";
		for (int i = 0; i < sentences.size(); i++) {
			text += sentences.get(i);
			text += "\n";
		}
		textArea.setText(text);
	}

}
