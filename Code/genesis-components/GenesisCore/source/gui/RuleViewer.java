package gui;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JFrame;

import connections.*;
import frames.classic.FrameViewer;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import genesis.GenesisGetters;
import matchers.Substitutor;
import utils.Mark;

/*
 * Created on Mar 8, 2009
 * @author phw
 */

public class RuleViewer extends FrameViewer implements WiredBox {

	// public final static String DISPLAY = "Stop";

	// GenesisGetters genesisGetters;

	public static final String FINAL_INFERENCE = "final-inference";

	public static final String FINAL_STORY = "final-story";

	public RuleViewer() {
		super(null, FrameViewer.SCROLL_AS_NEEDED);
		this.setBackground(Color.WHITE);
		Connections.getPorts(this).addSignalProcessor(this::process);
		Connections.getPorts(this).addSignalProcessor(FINAL_INFERENCE, this::processFinalInference);
		// this.genesisGetters = genesisGetters;
	}

	public void process(Object signal) {

		// Mark.say("Received rule for display", signal);
		Sequence sequence = new Sequence();

		if (signal != null && signal instanceof Sequence) {
			sequence = ((Sequence) signal);
		}
		else if (signal != null && signal instanceof Entity) {
			ArrayList<Entity> rules = new ArrayList();
			rules.add((Entity) signal);
		}
		else if (signal != null && signal instanceof ArrayList) {
			ArrayList<Entity> rules = (ArrayList<Entity>) signal;
			for (Entity t : rules) {
				sequence.addElement(t);
			}
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in RuleViewer");
		}
		// System.out.println("Rule viewer length is a" + sequence.getElements().size());
		setStory(sequence);
		int width = this.bodyPanel.getWidth();
		width = 5000;
		// System.out.println("Panels " + getPanels().size() + " width " +
		// width);
		bodyPanel.scrollRectToVisible(new Rectangle(width - 2, 0, 1, 1));
		Connections.getPorts(this).transmit(sequence);
	}

	public void processFinalInference(Object signal) {
		Sequence sequence = new Sequence();

		if (signal != null && signal instanceof Sequence) {
			sequence = ((Sequence) signal);
		}
		else if (signal != null && signal instanceof Entity) {
			ArrayList<Entity> rules = new ArrayList();
			rules.add((Entity) signal);
		}
		else if (signal != null && signal instanceof ArrayList) {
			ArrayList<Entity> rules = (ArrayList<Entity>) signal;
			for (Entity t : rules) {
				sequence.addElement(t);
			}
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in RuleViewer");
		}
		// System.out.println("Rule viewer length is a" + sequence.getElements().size());
		setStory(sequence);
		int width = this.bodyPanel.getWidth();
		width = 5000;
		// System.out.println("Panels " + getPanels().size() + " width " +
		// width);
		bodyPanel.scrollRectToVisible(new Rectangle(width - 2, 0, 1, 1));
		Connections.getPorts(this).transmit(FINAL_STORY, sequence);
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
