package gui;

import frames.PathElementFrame;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;

import java.awt.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;

import utils.Mark;

import connections.*;
import connections.Ports;

/*
 * Created on May 17, 2007 @author phw
 */
public class PathElementViewer extends NegatableJPanel {
	String role;

	String name;

	String reference;

	private Ports ports;

	public PathElementViewer() {
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		int height = this.getHeight();
		int width = this.getWidth();
		int thickness = 10;
		int length = 100;
		int headLength = 10;
		int headDelta = 5;
		int square = 6 * width / 10;
		int diameter = 4 * thickness / 2;
		int radius = diameter / 2;
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
		// Draw string before changing color and using transform
		FontMetrics fm = g.getFontMetrics();
		g.drawString(this.reference, 10, height - 5 - fm.getDescent());
		// Draw arrow
		g.setColor(shadowColor);
		// System.err.println("Painting path element " + preposition + ", " +
		// width + ", " + height);
		int xOffset = (width - square) / 2;
		int yOffset = height / 2;
		int[] x = { 0, length - headLength, length - headLength, length, length - headLength, length - headLength, 0 };
		int[] y = { 0, 0, -headDelta, thickness / 2, thickness + headDelta, thickness, thickness };
		double multiplier = (double) square / length;
		int tOffset = (int) ((height - thickness * multiplier) / 2);
		AffineTransform transform = g.getTransform();
		transform.translate(xOffset, tOffset);
		transform.scale(multiplier, multiplier);
		g.setTransform(transform);
		// for (int i = 0; i < x.length; ++i) {
		// x[i] *= multiplier;
		// y[i] *= multiplier;
		// x[i] += xOffset;
		// // y[i] += height - 2 * (headDelta + thickness) * multiplier;
		// y[i] += (height - (thickness * multiplier)) / 2;
		// }
		// System.out.println("Multiplier = " + multiplier);
		// Draw arrow
		g.setColor(squareColor);
		g.fillPolygon(x, y, 7);
		g.setColor(ballColor);
		// Draw ball
		// System.out.println("Role in path element viewer is " + role);
		if (this.role.equalsIgnoreCase("over") || this.role.equalsIgnoreCase("above")) {
			yCenter = -2 * diameter;
			xCenter = (int) (-radius + 0.5 * length);
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("under") || this.role.equalsIgnoreCase("below")) {
			yCenter = 2 * diameter;
			xCenter = (int) (-radius + 0.5 * length);
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("toward")) {
			// Toward
			yCenter = thickness / 2 - radius;
			xCenter = (length + diameter);
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("to") || this.role.equalsIgnoreCase("in")) {
			// To
			yCenter = thickness / 2 - radius;
			xCenter = length;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("from")) {
			// From
			yCenter = thickness / 2 - radius;
			xCenter = -diameter;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else if (this.role.equalsIgnoreCase("awayFrom")) {
			// Away from
			yCenter = thickness / 2 - radius;
			xCenter = -2 * diameter;
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		else {
			// Default is via
			yCenter = thickness / 2 - radius;
			xCenter = (int) (-radius + 0.5 * length);
			g.fillOval(xCenter, yCenter, diameter, diameter);
		}
		if (this.role.equalsIgnoreCase("behind")) {
			g.setColor(squareColor);
			g.fillPolygon(x, y, 7);
			g.setColor(ballColor);
		}
	}

	private String findThing(Entity t) {
		if (t instanceof Function) {
			return this.findThing(t.getSubject());
		}
		else if (t instanceof Sequence) {
			return "?";
		}
		else {
			return t.getType();
		}
	}

	private void setParameters(String preposition, String reference) {
		this.role = preposition;
		this.reference = reference;
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
		PathElementViewer view = new PathElementViewer();
		Entity thing = new Entity();
		thing.addType("dog");
		PathElementFrame pathElement = new PathElementFrame("toward", thing);
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}

	public Ports getPorts() {
		if (this.ports == null) {
			this.ports = new Ports();
		}
		return this.ports;
	}

	public void view(Object signal) {
		// Mark.a(signal);
		if (signal instanceof Function || signal == null) {
			Function derivative = (Function) signal;
			String role = derivative.getType();
			PathElementViewer.this.setParameters(role, this.findThing(derivative));
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in PathElementViewer");
		}
		setTruthValue(signal);
	}
}
