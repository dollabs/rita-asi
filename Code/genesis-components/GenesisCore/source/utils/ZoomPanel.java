package utils;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;

/*
 * Created on Dec 10, 2011
 * @author phw
 */

public class ZoomPanel extends JPanel implements MouseListener, MouseMotionListener {

	Component component;

	public Component getComponent() {
		return component;
	}

	double magnification = 1.0;

	double magStep = 1.5;

	double xOffset = 0;

	double yOffset = 0;

	Dimension preferredDimension;

	boolean drawCenterSpot = false;

	boolean clicking = false;

	public ZoomPanel(Component component) {
		this.component = component;
		this.add(this.component);
		this.setLayout(new ZoomLayout());
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		// int cWidth = (int) (magnification * preferredDimension.width);
		// int cHeight = (int) (magnification * preferredDimension.height);
		repaint();
		// int width = getWidth();
		// int height = getHeight();
		// xOffset = (width - cWidth) / 2;
		// yOffset = (height - cHeight) / 2;
		this.setBackground(Color.YELLOW);
		repaint();
	}

	public Dimension getPreferredSize() {
		return component.getPreferredSize();
	}

	public void paint(Graphics g) {

		Color handle = g.getColor();
		g.setColor(Color.YELLOW);
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2 = (Graphics2D) g;

		AffineTransform t = g2.getTransform();
		AffineTransform t2 = (AffineTransform) (t.clone());
		t2.translate(xOffset, yOffset);
		t2.scale(magnification, magnification);
		g2.setTransform(t2);
		paintComponents(g2);
		g2.setTransform(t);
		if (drawCenterSpot) {
			int w = getWidth();
			int h = getHeight();
			int r = 5;
			g.setColor(Color.red);
			g.fillOval(w / 2 - r, h / 2 - r, 2 * r, 2 * r);
			g.setColor(handle);
		}

	}

	private class ZoomLayout implements LayoutManager {

		int referenceWidth = 0;

		int referenceHeight = 0;

		public void layoutContainer(Container parent) {
			synchronized (parent.getTreeLock()) {
				if (parent.getComponents().length == 0) {
					return;
				}

				int width = getWidth();
				int height = getHeight();

				if (getWidth() == 0 || getHeight() == 0) {
					return;
				}
				if (preferredDimension == null) {

					Dimension p = component.getPreferredSize();

					if (p.width != 0 && p.height != 0) {

						if (p.width * height > p.height * width) {
							preferredDimension = new Dimension(width, width * p.height / p.width);
						}
						else {
							preferredDimension = new Dimension(height * p.width / p.height, height);
						}
					}
					else {
						preferredDimension = new Dimension(50, 50);
					}
				}

				// int cWidth = (int) (magnification *
				// preferredDimension.width);
				// int cHeight = (int) (magnification *
				// preferredDimension.height);

				int cWidth = preferredDimension.width;
				int cHeight = preferredDimension.height;

				if (component != null) {
					if (referenceWidth != width || referenceHeight != height) {
						referenceWidth = width;
						referenceHeight = height;
						Mark.say("Boom");

						int xOffset = (width - cWidth) / 2;
						int yOffset = (height - cHeight) / 2;

						Mark.say("Rebounding");

						component.setBounds(xOffset, yOffset, cWidth, cHeight);
					}
				}

			}
		}

		public void removeLayoutComponent(Component component) {

		}

		public void addLayoutComponent(String arg0, Component component) {

		}

		public Dimension minimumLayoutSize(Container parent) {
			if (parent.getComponents().length == 0) {
				return null;
			}
			return parent.getComponent(0).getMinimumSize();
		}

		public Dimension preferredLayoutSize(Container parent) {
			if (parent.getComponents().length == 0) {
				return null;
			}
			return parent.getComponent(0).getPreferredSize();
		}
	}

	public static void main(String[] ignore) {
		ZoomPanel z = new ZoomPanel(new JButton("Hello World"));
		JFrame frame = new JFrame();
		frame.getContentPane().add(z);
		frame.setBounds(0, 0, 400, 300);
		frame.setVisible(true);
		Mark.say("x, y ", z.getWidth(), z.getHeight());
	}

	/*
	 * Shrink or enlarge, keeping point at the center still at the center.
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */

	public void mouseClicked(MouseEvent e) {
		clicking = true;
		int oldWidth = (int) (magnification * preferredDimension.width);
		int oldHeight = (int) (magnification * preferredDimension.height);

		int xCenter = getWidth() / 2;
		int yCenter = getHeight() / 2;

		double oldFractionLeftOfCenter = (double) (xCenter - xOffset) / oldWidth;
		double oldFractionAboveCenter = (double) (yCenter - yOffset) / oldHeight;

		if (e.getButton() == MouseEvent.BUTTON1) {
			magnification *= magStep;
		}
		else {
			magnification /= magStep;
		}
		int newWidth = (int) (magnification * preferredDimension.width);
		int newHeight = (int) (magnification * preferredDimension.height);

		xOffset = xCenter - newWidth * oldFractionLeftOfCenter;
		yOffset = yCenter - newHeight * oldFractionAboveCenter;
		revalidate();
		clicking = false;
	}

	double xAnchor, yAnchor;

	double xOffsetReference, yOffsetReference;

	@Override
	public void mousePressed(MouseEvent e) {
		xAnchor = e.getX();
		yAnchor = e.getY();
		xOffsetReference = xOffset;
		yOffsetReference = yOffset;
		drawCenterSpot = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		drawCenterSpot = false;
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		double xNow = e.getX();
		double yNow = e.getY();
		double deltaX = xNow - xAnchor;
		double deltaY = yNow - yAnchor;
		xOffset = xOffsetReference + deltaX;
		yOffset = yOffsetReference + deltaY;
		// revalidate();
		repaint();
		// Mark.say(deltaX, deltaY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
