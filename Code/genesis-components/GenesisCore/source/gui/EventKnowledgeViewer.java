package gui;

import genesis.*;

import java.awt.*;
import java.io.File;

import javax.swing.*;

import connections.Connections;
import frames.classic.FrameViewer;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Jan 9, 2008 @author phw
 */

public class EventKnowledgeViewer extends NewFrameViewer {
	File file;

	FrameViewer fv = new FrameViewer();

	JLabel fileLabel = new JLabel();

	Entity input = null;

	public EventKnowledgeViewer() {
		Connections.getPorts(this).addSignalProcessor("process");
		this.setLayout(new BorderLayout());
		this.add(fv, BorderLayout.CENTER);
		this.add(fileLabel, BorderLayout.SOUTH);
		this.setBackground(Color.WHITE);
	}

	public void process(Object signal) {
		if (signal instanceof Quantum) {
			Quantum q = (Quantum) signal;
			this.fv.setInput(q.getThing());
			// this.setLabel(q.getDirectory().toString());
			// Gauntlet.getOutputTabbedPane().setSelectedComponent(Gauntlet.getKnowledgeWatcherBlinker());
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in NewFrameViewer");
		}
	}

	public void clear() {
		this.fv.clearData();
	}

	public Entity getInput() {
		return input;
	}

	public void setLabel(String text) {
		fileLabel.setText(text);
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		EventKnowledgeViewer view = new EventKnowledgeViewer();
		frame.getContentPane().add(view);
		Entity t = new Entity("Patrick");
		Function d = new Function(t);
		d.addType("Foo");
		d.addType("Bar");
		Relation r = new Relation(d, d);
		r.removeType("thing");
		r.addType("action");
		r.addType("X");
		r.addType("Y");
		r.addType("Z");
		Sequence s = new Sequence("hello");
		s.addType("big");
		s.addType("little");
		s.addType("red", "feature");
		s.addElement(r);
		view.process(new Quantum(r, s, true));
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}

}
