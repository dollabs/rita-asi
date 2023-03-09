package gui;


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import start.Start;
import connections.Connections;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Thread;

/*
 * Created on May 17, 2007 @author phw
 */
public class ComparisonViewer extends NegatableJPanel {
	String x;

	String y;

	String xOwner;

	String yOwner;

	String comparitor;

	public List heightWords = Arrays.asList("taller", "shorter");

	public List widthWords = Arrays.asList("thicker", "thinner", "wider", "narrower");

	public List reversers = Arrays.asList("shorter", "thinner", "narrower", "smaller", "weaker", "softer");

	public ComparisonViewer() {
		setOpaque(false);
		Connections.getPorts(this).addSignalProcessor("view");
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		// System.out.println("Painting " + x + ", " + y + ", " + comparitor);
		Graphics2D g = (Graphics2D) graphics;
		int height = this.getHeight();
		int width = this.getWidth();
		g.drawRect(0, 0, width - 1, height - 1);
		int bigHeight = 50;
		int bigWidth = 50;
		int littleHeight = 50;
		int littleWidth = 50;
		int separation = 5;
		Color squareColor = new Color(150, 150, 150);
		int yCenter = 0;
		int xCenter = 0;
		if (width == 0 || height == 0) {
			return;
		}
		if (this.x == null || this.y == null || comparitor == null) {
			return;
		}
		String left = x;
		String right = y;
		if (heightWords.contains(comparitor)) {
			bigWidth = 25;
			littleWidth = 25;
			littleHeight = 25;
		}
		else if (widthWords.contains(comparitor)) {
			littleWidth = 25;
		}
		else {
			littleHeight = littleWidth = 25;
		}
		if (reversers.contains(comparitor)) {
			left = y;
			right = x;
		}
		
		FontMetrics fm = g.getFontMetrics();
		int totalHeight = bigHeight + 3 * fm.getHeight();
		int totalWidth = bigWidth + separation + littleWidth;

		double scale = (double) width / totalWidth;

		double scaleW = (double) height / totalHeight;

		if (scaleW < scale) {
			scale = scaleW;
		}
		scale *= 0.9;

		bigWidth *= scale;
		bigHeight *= scale;

		littleWidth *= scale;
		littleHeight *= scale;

		totalWidth *= scale;
		totalHeight *= scale;

		int xOffset = (width - totalWidth) / 2;
		int yOffset = (height - totalHeight) / 2;

		
		// Draw squares
		g.setColor(squareColor);
		g.fillRect(xOffset, yOffset, bigWidth, bigHeight);
		g.fillRect(xOffset + bigWidth + separation, yOffset + bigHeight - littleHeight, littleWidth, littleHeight);
		g.setColor(Color.BLACK);

		g.drawString(left, xOffset + bigWidth / 2 - fm.stringWidth(left) / 2, yOffset + bigHeight + 10 + fm.getDescent());
		g.drawString(right, xOffset + bigWidth + separation + littleWidth / 2 - fm.stringWidth(right) / 2, yOffset + bigHeight + 10
		                + fm.getDescent());
		if (xOwner != null) {
			g.drawString(xOwner, xOffset + bigWidth / 2 - fm.stringWidth(xOwner) / 2,
					fm.getHeight() + yOffset + bigHeight + 10 + fm.getDescent());
		}
		if (yOwner != null) {
			g.drawString(yOwner, xOffset + bigWidth + separation + littleWidth / 2 - fm.stringWidth(yOwner) / 2,
					fm.getHeight() + yOffset + bigHeight + 10 + fm.getDescent());
		}
	}

	private void setParameters(String comparitor, Entity tx, Entity ty) {

		this.x = tx.getType();
		this.y = ty.getType();
		xOwner = yOwner = null;
		Thread ox = tx.getThreadWith(Entity.MARKER_OWNERS);
		if (ox != null) {
			xOwner = Start.strip(ox.lastElement());
		}
		Thread oy = ty.getThreadWith(Entity.MARKER_OWNERS);
		if (oy != null) {
			yOwner = Start.strip(oy.lastElement());
		}
		this.comparitor = comparitor;
		this.repaint();
	}

	private void clearData() {
		this.x = null;
		this.y = null;
		this.comparitor = null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ComparisonViewer view = new ComparisonViewer();
		Relation relation = new Relation(Markers.COMPARISON_MARKER, new Entity("patrick"), new Entity("john"));
		relation.addType("taller");
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
		view.view(relation);
	}

	public void view(Object signal) {
		// System.out.println("Actuating");
		if (signal instanceof Relation) {
			Relation comparison = (Relation) signal;
			String name = comparison.getType();
			if (comparison.isAPrimed(Markers.COMPARISON_MARKER)) {
				ComparisonViewer.this.setParameters(name, comparison.getSubject(), comparison.getObject());
			}
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in the place viewer");
		}
		setTruthValue(signal);
	}
}
