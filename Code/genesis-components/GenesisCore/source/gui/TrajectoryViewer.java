package gui;

import frames.PathElementFrame;
import frames.entities.Relation;
import frames.entities.Sequence;

import java.awt.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;

import connections.*;
import connections.Ports;

/*
 * Created on May 17, 2007 @author phw
 */
public class TrajectoryViewer extends NegatableJPanel {
	String role;

	String name;

	private Ports ports;

	public TrajectoryViewer() {
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// this.setBackground(Color.WHITE);
		setOpaque(false);
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		Graphics2D g = (Graphics2D) graphics;
		int height = this.getHeight();
		int width = this.getWidth();
		int thickness = 10;
		int length = 100;
		int headLength = 10;
		int headDelta = 5;
		int square = 6 * width / 10;
		int diameter = 3 * thickness / 2;
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
		g.drawString(this.role, 10, height - 5 - fm.getDescent());
		// Draw arrow
		g.setColor(shadowColor);
		// System.err.println("Painting trajectory element " + role + ", " +
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
		// Draw box
		int size = 15;
		g.fillRect(size, -size, size, size);
		// if (preposition.equalsIgnoreCase("via")) {
		//
		// // Via
		// yCenter = - diameter;
		// xCenter = (int) (- radius + 0.5 * length);
		// g.fillOval(xCenter, yCenter, diameter, diameter);
		// }
	}

	private void setParameters(String role) {
		// System.err.println("Trajectory word is " + role);
		this.role = role;
		this.repaint();
	}

	private void clearData() {
		this.role = null;
		this.name = null;
	}

	public Ports getPorts() {
		if (this.ports == null) {
			this.ports = new Ports();
		}
		return this.ports;
	}

	public void view (Object signal) {
		if (signal instanceof Sequence) {
			Sequence space = (Sequence) signal;
			if (space.isA("eventSpace")) {
				Sequence ladder = (Sequence) space.getElement(0);
				Relation trajectory = (Relation) ladder.getElement(0);
				setParameters(trajectory.getType());
			}
		}
		else if (signal instanceof Relation) {
			Relation trajectory = (Relation) signal;
			setParameters(trajectory.getType());
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in TrajectoryViewer");
		}
		setTruthValue(signal);
	}
}
