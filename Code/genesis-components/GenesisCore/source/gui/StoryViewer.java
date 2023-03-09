package gui;

import genesis.GenesisGetters;

import java.awt.Rectangle;

import javax.swing.JFrame;

import storyProcessor.StoryProcessor;
import utils.Mark;

import matchers.Substitutor;
import connections.*;
import constants.Markers;
import frames.classic.FrameViewer;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Mar 8, 2009
 * @author phw
 */

public class StoryViewer extends FrameViewer implements WiredBox {

	public final static String DISPLAY = "Stop";

	Sequence buffer = new Sequence();

	// GenesisGetters genesisGetters;

	public StoryViewer() {
		super(null, FrameViewer.SCROLL_AS_NEEDED);
		this.setName("Story viewer");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object x) {
		// Mark.say("Entering StoryViewer.process", x);
		if (x instanceof String) {
			if (x == Markers.RESET) {
				clearData();
				return;
			}
		}
		else if (x instanceof Entity) {
			Entity signal = (Entity) x;
			if (signal.sequenceP()) {
				Sequence sequence = (Sequence) signal;
				// Seems like it is always receiving a collection of events on this port
				if (sequence.isAPrimed(StoryProcessor.STORY) || sequence.isAPrimed(Markers.CONCEPT_MARKER)
				// || sequence.isAPrimed(StoryProcessor.ONSET)
				        || (!sequence.getElements().isEmpty() && sequence.getElements().get(0).isA(Markers.CONCEPT_MARKER))) {
					// System.out.println("Vector length is " +
					// sequence.getElements().size());

					try {
						setStory(sequence);
					}
					catch (Exception e) {
						Mark.err("StoryViewer.process blew out"); // , sequence);
					}
					int width = this.bodyPanel.getWidth();
					width = 5000;
					// System.out.println("Panels " + getPanels().size() +
					// " width " + width);
					bodyPanel.scrollRectToVisible(new Rectangle(width - 2, 0, 1, 1));
					return;
				}
			}
			buffer.addElement(signal);
			if (buffer.getElements().size() > 5) {
				buffer.getElements().remove(0);
			}
			setStory(buffer);
		}
	}

	public void clear() {
		clearData();
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		StoryViewer view = new StoryViewer();
		frame.getContentPane().add(view);
		Entity bird1 = new Entity("bird");
		Entity bird2 = new Entity("bird");
		Entity tree1 = new Entity("tree");
		Entity tree2 = new Entity("tree");
		Entity elephant = new Entity("elephant");

		Function at1 = new Function("at", tree1);
		Function at2 = new Function("at", bird2);
		at2 = new Function("at", tree2);

		Function to1 = new Function("to", at1);
		Function to2 = new Function("to", at2);

		Sequence path1 = new Sequence("path");
		Sequence path2 = new Sequence("path");

		path1.addElement(to1);
		path2.addElement(to2);

		Relation fly = new Relation("flew", bird1, path1);
		Relation walk = new Relation("walked", elephant, path2);

		Sequence v = new Sequence("story");
		v.addElement(fly);
		v.addElement(walk);

		Entity substitution = Substitutor.dereference(v);
		// substitution = v;

		view.process(substitution);
		frame.setBounds(0, 0, 600, 600);
		frame.setVisible(true);
	}

}
