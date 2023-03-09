package gui;

import expert.SimpleGenerator;
import frames.classic.FrameViewer;
import frames.entities.Entity;
import genesis.GenesisGetters;
import utils.tools.JFactory;

import java.awt.*;

import javax.swing.*;

import connections.*;
import connections.Ports;

/*
 * Created on Jul 14, 2007 @author phw
 */
public class TalkingFrameViewer extends JPanel implements WiredBox {
	Ports ports;

	FrameViewer fv = new FrameViewer();

	Entity input = null;

	GenesisGetters genesisGetters;

	public TalkingFrameViewer(GenesisGetters genesisGetters) {
		this.genesisGetters = genesisGetters;
		Connections.getPorts(this).addSignalProcessor("process");
		this.setLayout(new BorderLayout());
		WiredLabelPanel textBox = new WiredLabelPanel();
		textBox.setName("Text box");
		this.add(this.fv, BorderLayout.CENTER);
		this.add(textBox, BorderLayout.SOUTH);
		this.setBackground(Color.WHITE);
		SimpleGenerator translator = new SimpleGenerator();
		translator.setName("Talker translator");
		Connections.wire(this, translator);
		Connections.wire(translator, textBox);
	}

	public void process(Object signal) {
		if (signal instanceof Entity || signal == null) {
			input = (Entity) signal;
			// System.out.println("Everything viewer input: \n" + input);
			genesisGetters.getWindowGroupManager().setGuts(genesisGetters.getRightPanel(), this);
			// getters.getOutputTabbedPane().setSelectedComponent(this);
			this.fv.setInput(input);
			Connections.getPorts(this).transmit(input);
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in NewFrameViewer");
		}
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

	// public static void main(String[] ignore) {
	// JFrame frame = new JFrame();
	// TalkingFrameViewer view = new TalkingFrameViewer(null);
	// frame.getContentPane().add(view);
	// Entity rock = new Entity("Rock");
	// Function place2 = JFactory.createPlace("at", rock);
	//
	// // Derivative pathElement1 = JFactory.createPathElement("via", place1);
	// Function pathElement2 = JFactory.createPathElement("to", place2);
	//
	// // path.addElement(pathElement1);
	// // path.addElement(pathElement2);
	//
	// // Thing speed = new Thing("speed");
	// // Derivative increase = BFactory.createTransitionElement("increase",
	// // speed);
	//
	// Entity ball = new Entity("ball");
	// Entity table = new Entity("table");
	// Function place = JFactory.createPlace("below", table);
	// Function destination = JFactory.createPathElement("to", place);
	// Sequence path = JFactory.createPath();
	// path.addElement(destination);
	// Relation roll = JFactory.createGo(ball, path);
	// roll.addType("roll");
	//
	// Sequence ladder1 = JFactory.createTrajectoryLadder();
	// ladder1.addElement(roll);
	//
	// Sequence space = JFactory.createEventSpace();
	// space.addElement(ladder1);
	//
	// view.process(space);
	// frame.setBounds(0, 0, 200, 200);
	// frame.setVisible(true);
	// }

}
