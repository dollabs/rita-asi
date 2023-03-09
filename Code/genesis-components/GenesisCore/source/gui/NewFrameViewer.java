package gui;

import java.awt.GridLayout;

import javax.swing.JFrame;


import utils.tools.BFactory;
import utils.tools.JFactory;
import connections.*;
import connections.Ports;
import constants.Markers;
import frames.classic.FrameViewer;
import frames.entities.AFactory;
import frames.entities.EFactory;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Jul 14, 2007 @author phw
 */
public class NewFrameViewer extends NegatableJPanel {
	Ports ports;

	FrameViewer fv = new FrameViewer();

	Entity input = null;

	public NewFrameViewer() {
		this.setLayout(new GridLayout(1, 1));
		this.add(this.fv);
		this.setOpaque(false);
		Connections.getPorts(this).addSignalProcessor("view");
	}

	public void view(Object signal) {
		// Mark.a("New frame viewer signal: " + signal);
		if (signal != null && signal instanceof Entity) {
			input = (Entity) signal;
			// System.out.println("Everything viewer input: \n" + input);
			this.fv.setInput(input);
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in NewFrameViewer");
		}
		// this.fv.getBodyPanel().setNegated(true);
		this.fv.getBodyPanel().setTruthValue(signal);
	}

	public void clear() {
		this.fv.clearData();
	}

	public Ports getPorts() {
		if (this.ports == null) {
			this.ports = new Ports();
		}
		return this.ports;
	}

	public Entity getInput() {
		return input;
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		NewFrameViewer view = new NewFrameViewer();
		frame.getContentPane().add(view);
		// Thing t = new Thing("Patrick");
		// Derivative d = new Derivative(t);
		// d.addType("Foo");
		// d.addType("Bar");
		// Relation r = new Relation(d, d);
		// r.removeType("thing");
		// r.addType("action");
		// r.addType("X");
		// r.addType("Y");
		// r.addType("Z");
		// Sequence s = new Sequence("hello");
		// s.addType("big");
		// s.addType("little");
		// s.addType("red", "feature");
		// s.addElement(r);
		// Thing t = new Thing("ball");
		// t.addType("baseball");

		Entity rock = new Entity("Rock");
		Function place2 = JFactory.createPlace("at", rock);

		// Derivative pathElement1 = JFactory.createPathElement("via", place1);
		Function pathElement2 = JFactory.createPathElement("to", place2);

		// path.addElement(pathElement1);
		// path.addElement(pathElement2);

		// Thing speed = new Thing("speed");
		// Derivative increase = BFactory.createTransitionElement("increase",
		// speed);

		Entity ball = new Entity("ball");
		ball.addFeature("red");
		ball.addProperty(Markers.OWNER_MARKER, "Patrick");
		Entity table = new Entity("table");
		Function place = JFactory.createPlace("below", table);
		Function destination = JFactory.createPathElement("to", place);
		Sequence path = JFactory.createPath();
		path.addElement(destination);
		Relation roll = new Relation(ball, path);
		roll.addType("roll");

		Entity cat = new Entity("cat");
		Entity bird = new Entity("bird");
		Function appear = BFactory.createTransitionElement("appear", cat);
		Function disappear = BFactory.createTransitionElement("disappear", bird);
		Relation r12 = AFactory.createTimeRelation("before", appear, disappear);

		Sequence ladder1 = EFactory.createEventLadder();
		ladder1.addElement(appear);
		ladder1.addElement(roll);

		Sequence ladder2 = EFactory.createEventLadder();
		ladder2.addElement(disappear);

		Sequence space = EFactory.createEventSpace();
		space.addElement(ladder1);
		space.addElement(ladder2);
		space.addFeature(Markers.NOT);

		System.out.println(r12);
		view.view(space);
		frame.setBounds(0, 0, 300, 800);
		frame.setVisible(true);
	}

}
