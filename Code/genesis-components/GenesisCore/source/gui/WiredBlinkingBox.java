package gui;

import expert.AgentExpert;

import javax.swing.*;

import utils.Mark;

import connections.*;
import constants.Markers;

/*
 * Connection with viewer may be either by direct call or through a wire, depending on whether the viewer is a WiredJPanel or a WiredBox
 * Created on Aug 5, 2006 @author phw
 */
public class WiredBlinkingBox extends BlinkingBox implements WiredBox {

	WiredViewer viewer;

	WiredJPanel box;

	public WiredBlinkingBox(WiredJPanel box) {
		this();
		this.box = box;
	}

	public WiredBlinkingBox() {
		super();
		Connections.getPorts(this).addSignalProcessor("process");
	}
	
//	public WiredBlinkingBox(String string, AbstractWiredBox expert, WiredJPanel viewer, BlinkingBoxPanel blinkingBoxPanel) {
//		this(viewer);
//		setTitle(string);
//		setGraphic(viewer);
//		setName(string + " blinker");
//		viewer.setName(string + " viewer");
//		blinkingBoxPanel.add(this);
//		Connections.wire(Markers.VIEWER, expert, this);
//	}

	public WiredBlinkingBox(String string) {
		this();
		setTitle(string);
	}

	public WiredBlinkingBox(String string, AbstractWiredBox expert, WiredJPanel viewer, BlinkingBoxPanel blinkingBoxPanel) {
		this(viewer);
		if (viewer instanceof WiredViewer) {
		this.viewer = (WiredViewer) viewer;
		}
		setTitle(string);
		setGraphic(viewer);
		setName(string + " blinker");
		viewer.setName(string + " viewer");
		blinkingBoxPanel.add(this);
		Connections.wire(Markers.VIEWER, expert, this);
	}

	public void process(Object signal) {
		setInput(signal);
		// If direct call available, do that; otherwize communicate via wire
		if (viewer != null) {
			viewer.view(signal);
		}
		else {
			
			Connections.getPorts(this).transmit(signal);
		}
	}
	
	public void setInput(Object input) {
		blink();
	}

	public void setInput(Object input, Object port) {
		blink();
	}

	public static void main(String[] args) {
		WiredBlinkingBox box = new WiredBlinkingBox();
		box.setTitle("Sample title");
		box.setMemory(new JLabel("Memory"));
		box.setGraphic(new JLabel("Graphic"));
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(box);
		frame.setBounds(100, 100, 400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		box.setInput(new Object());
	}

}
