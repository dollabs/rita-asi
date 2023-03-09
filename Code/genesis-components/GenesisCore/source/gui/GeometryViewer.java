package gui;
import frames.GeometryFrame;
import frames.entities.Entity;
import frames.entities.Relation;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;


import connections.*;
public class GeometryViewer extends WiredJPanel {
	public static Color	figureColor	= Color.blue;
	public static Color	groundColor	= Color.gray;
	private boolean		viewable	= true;
	private String		figure		= "";
	private String		ground		= "";
	boolean				motional	= false;
	public void setMotional() {
		this.motional = true;
	}
	public void setLocative() {
		this.motional = false;
	}
	public void setFigure(String geometry) {
		this.figure = geometry;
	}
	public void setGround(String geometry) {
		this.ground = geometry;
	}
	public GeometryViewer() {
		this.setOpaque(false);
		Connections.getPorts(this).addSignalProcessor("setParameters");
	}
	@Override
	public void paintComponent(Graphics x) {
		super.paintComponent(x);
		Graphics2D g = (Graphics2D) x;
		int width = this.getWidth();
		int height = this.getHeight();
		int figureX = width / 2;
		int figureY = height / 2 - width / 24;
		int groundX = figureX + width / 50;
		int groundY = figureY + width / 50;
		if (width == 0 || height == 0) {
			return;
		}
		g.drawRect(0, 0, width - 1, height - 1);
		if (!this.isViewable()) {
			return;
		}
		g.setColor(GeometryViewer.groundColor);
		if (this.ground.equals("point")) {
			this.drawDot(g, 5 * groundX, 5 * groundY);
		} else if (this.ground.equals("line")) {
			this.drawLine(g, groundX, groundY);
		} else if (this.ground.equals("plane")) {
			g.fillRect(groundX - width / 4, groundY - height / 4, width / 2, height / 2);
		} else if (this.ground.equals("point-pair")) {
			int deviation = width / 5;
			this.drawDot(g, figureX, figureY + deviation);
			this.drawDot(g, figureX, groundY - deviation);
		} else if (this.ground.equals("point-set")) {
			int deviation = width / 5;
			this.drawDot(g, groundX + deviation, groundY + deviation);
			this.drawDot(g, groundX - deviation, groundY - deviation);
			this.drawDot(g, groundX - deviation, groundY + deviation);
			this.drawDot(g, groundX + deviation, groundY - deviation);
		} else if (this.ground.equals("aggregate")) {
			int radius = this.getHeight() / 30;
			for (int i = -40; i < 40; i++) {
				g.fillOval(figureX + i * 643201334 / 49887259, figureY + i * 924374328 / 49471903, radius, radius);
			}
		} else if (this.ground.equals("tube")) {
			g.fill(new RoundRectangle2D.Double(figureX - 3 * width / 10, figureY - height / 10, 3 * width / 5,
					height / 5, width / 10, height / 4));
		} else if (this.ground.equals("cylinder")) {
			// g.
		} else if (this.ground.equals("distributed")) {
			int radius = this.getHeight() / 50;
			for (int i = -500; i < 500; i++) {
				g.fillOval(figureX + i * 643201334 / 49887259, figureY + i * 924374328 / 49471903, radius, radius);
			}
		}
		g.setColor(GeometryViewer.figureColor);
		if (this.figure.equals("point")) {
			this.drawDot(g, figureX, figureY);
		}
		else if (this.figure.equals("line")) {
			this.drawLine(g, figureX, figureY);
		}
		else if (this.figure.equals("distributed")) {
			int radius = this.getHeight() / 50;
			for (int i = -100; i < 100; i++) {
				g.fillOval(figureX + i * 673201334 / 55887259, figureY + i * 946374328 / 89371903, radius, radius);
			}
		}
		if (this.motional) {
			int arrowWidth = width / 3;
			if (this.figure == "line") {
				g.drawLine(figureX - width / 4, figureY - height / 30, figureX + width / 4, figureY - height / 30);
				g.drawLine(figureX + width / 4 - width / 40, figureY - height / 30 - height / 40, figureX + width / 4,
						figureY - height / 30);
			} else {
				g.drawLine(figureX, figureY, figureX + arrowWidth, figureY);
				g.drawLine(figureX + arrowWidth - width / 40, figureY - height / 40, figureX + arrowWidth, figureY);
				g.drawLine(figureX + arrowWidth - width / 40, figureY + height / 40, figureX + arrowWidth, figureY);
			}
		}
	}
	private void drawDot(Graphics g, int x, int y) {
		int radius = this.getHeight() / 38;
		g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
	}
	private void drawLine(Graphics g, int x, int y) {
		int width = 3 * this.getWidth() / 4;
		int height = this.getHeight() / 60;
		g.fillRect(x - width / 2, y - height / 2, width, height);
	}
	public boolean isViewable() {
		return this.viewable;
	}
	public void setViewable(boolean b) {
		this.viewable = b;
		this.setVisible(b);
	}
	public void clearData() {
		this.setViewable(false);
	}
	private Ports	ports;
	public Ports getPorts() {
		if (this.ports == null) {
			this.ports = new Ports();
		}
		return this.ports;
	}
	public void setParameters(Object o) {
		if (o instanceof GeometryFrame) {
			Relation frame = (Relation) ((GeometryFrame) o).getThing();
			this.setParameters(frame);
		} else if (o instanceof Relation) {
			Relation frame = (Relation) o;
			if (frame.isA(GeometryFrame.FRAMETYPE)) {
				this.setFigure(GeometryFrame.getFigureGeometry(frame));
				this.setGround(GeometryFrame.getGroundGeometry(frame));
				if (GeometryFrame.getRelationship(frame).equals("motional")) {
					this.setMotional();
				} else {
					this.setLocative();
				}
				this.setViewable(true);
				this.repaint();
			}
		}
	}
	public static void main(String[] args) {
		GeometryViewer view = new GeometryViewer();
		GeometryFrame gFrame = new GeometryFrame(GeometryFrame.makeSchema(new Entity("bike"), "point", new Entity(
				"walkway"), "line", "motional"));
		// view.getPlug().setInput(gFrame);
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}
}
