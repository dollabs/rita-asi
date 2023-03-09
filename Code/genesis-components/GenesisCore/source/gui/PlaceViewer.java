package gui;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import translator.RuleSet;


import connections.*;
import connections.Ports;
import frames.PlaceFrame;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on May 17, 2007 @author phw
 */
public class PlaceViewer extends NegatableJPanel{
	String role;

	String name;

	public PlaceViewer() {
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		int height = this.getHeight();
		int width = this.getWidth();
		int square = 5 * Math.min(height, width) / 10;
		int radius = 5 * Math.min(height, width) / 80;
		int diameter = radius * 2;
		Color squareColor = new Color(150, 150, 150);
		Color shadowColor = Color.LIGHT_GRAY;
		Color ballColor = Color.BLUE;
		int yCenter = 0;
		int xCenter = 0;
		if (width == 0 || height == 0) {
			return;
		}
		g.drawRect(0, 0, width - 1, height - 1);
		if (this.role == null) {
			return;
		}
		// System.err.println("Painting place " + preposition + ", " + width +
		// ", " + height);
		// Draw string before changing color and using transform
		FontMetrics fm = g.getFontMetrics();
		g.drawString(this.name, 10, height - 5 - fm.getDescent());
		int xOffset = (width - square) / 2;
		int yOffset = (height - square) / 2;
		int alternativeYOffset = height - square - xOffset;
		// yOffset = Math.max(yOffset, alternativeYOffset);
		// Draw shadow
		g.setColor(shadowColor);
		int[] x = new int[3];
		int[] y = new int[3];
		y[0] = y[1] = yOffset + square / 2;
		y[2] = yOffset + square;
		x[0] = x[2] = xOffset + square;
		x[1] = x[0] + square / 3;
		g.fillPolygon(x, y, 3);
		// Draw ball
		if (this.role.equalsIgnoreCase("back")) {
			g.setColor(ballColor);
			// Behind
			yCenter = yOffset + -diameter + 2 * square / 3;
			xCenter = xOffset + square - radius;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		// Draw a square
		g.setColor(squareColor);
		g.fillRect(xOffset, yOffset, square, square);
		g.setColor(ballColor);
		// Draw ball
		if (this.role.equalsIgnoreCase("at") || this.role.equalsIgnoreCase("bottom") || this.role.equalsIgnoreCase("front")) {
			// In front of
			yCenter = yOffset + square - diameter + radius;
			xCenter = xOffset + square / 2 - radius;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("top")) {
			// On
			yCenter = yOffset - diameter;
			xCenter = xOffset + square / 2 - radius;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
//		else if (this.role.equalsIgnoreCase("above") || this.role.equalsIgnoreCase("over")) {
//			// Above
//			yCenter = yOffset - 2 * diameter;
//			xCenter = xOffset + square / 2 - radius;
//			g.fillOval(xCenter, yCenter, diameter, diameter);
//		}
		// else if (this.role.equalsIgnoreCase("under")) {
		// // Under
		// yCenter = yOffset + square + diameter;
		// xCenter = xOffset + square / 2 - radius;
		// g.fillOval(xCenter, yCenter, diameter, diameter);
		// }
		else if (this.role.equalsIgnoreCase("left")) {
			// Left
			yCenter = yOffset + square - diameter + radius;
			xCenter = xOffset - diameter - radius;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("right") || this.role.equalsIgnoreCase("side")) {
			// Right
			yCenter = yOffset + square - diameter + radius;
			xCenter = xOffset + square + radius;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("inside")) {
			// In
			yCenter = yOffset + square - diameter - radius;
			xCenter = xOffset + square / 2 - radius;
			Stroke drawingStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 4 }, 0);
			g.setStroke(drawingStroke);
			g.drawOval(xCenter, yCenter, diameter, diameter);
		}
//		else if (!this.role.equalsIgnoreCase("behind") && RuleSet.getPathPrepositions().contains(this.role)) {
//			// In front of
//			yCenter = yOffset + square - diameter + radius;
//			xCenter = xOffset + square / 2 - radius;
//			g.fillOval(xCenter, yCenter, diameter, diameter);
//		}
//		else if (this.role.equalsIgnoreCase("behind")) {
//		}
		else if (this.role.equalsIgnoreCase("back")) {
			// Do nothing, but certainly no question mark, because drawn before square
		}
		else {
			yCenter = yOffset - 2 * diameter;
			xCenter = xOffset + square / 2 - radius;
			g.drawString("?", xCenter, yCenter);
		}

	}

	private void setParameters(String preposition, String name) {
		this.role = preposition;
		this.name = name;
		// System.out.println("PlaceViewer displaying " + preposition + ", " + name);
		this.repaint();
	}

	private void clearData() {
		this.role = null;
		this.name = null;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PlaceViewer view = new PlaceViewer();
		Entity thing = new Entity();
		thing.addType("dog");
		PlaceFrame place = new PlaceFrame("behind", thing);
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
		view.view(place.getPlace());
	}

	public void view(Object signal) {
		if (signal instanceof Function) {
			Function place = (Function) signal;
			String placeName = place.getType();
			// System.out.println("Place viewer working on " + placeName);
			if (RuleSet.placePrepositions.contains(placeName)) {
				Entity reference = place.getSubject();
				String referenceName = reference.getType();
				if ("at".equalsIgnoreCase(placeName)) {
					referenceName = placeName;
				}
				else {
					referenceName = placeName;
				}
				PlaceViewer.this.setParameters(placeName, referenceName);
			}
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in the place viewer");
		}
	}
}
