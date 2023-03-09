package gui;

import frames.PathElementFrame;
import frames.entities.Entity;

import java.awt.*;

import javax.swing.*;

import connections.*;

/*
 * Created on May 17, 2007 @author phw
 */
public class CauseViewer extends JPanel implements WiredBox {

	public CauseViewer() {
		Connections.getPorts(this).addSignalProcessor("process");
		// setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	public void process(Object signal) {
		System.out.println("Cause observed!");
	}

	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		int height = getHeight();
		int width = getWidth();
	}

	private void setParameters(String preposition) {
		System.err.println("Cause word is " + preposition);
		repaint();
	}

	private void clearData() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CauseViewer view = new CauseViewer();
		Entity thing = new Entity();
		thing.addType("dog");
		PathElementFrame pathElement = new PathElementFrame("toward", thing);
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}
}
