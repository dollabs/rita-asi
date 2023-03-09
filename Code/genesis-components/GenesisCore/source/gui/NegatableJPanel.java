package gui;

import java.awt.*;

import connections.WiredViewer;
import constants.Markers;
import frames.entities.Entity;

/*
 * Created on Jun 22, 2009
 * @author phw
 */

public  class NegatableJPanel extends WiredViewer {

	private boolean negated = false;

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	protected void setTruthValue(Object object) {
		// Mark.a(object);
		if (object instanceof Entity) {
			Entity signal = (Entity) object;
			if (signal.hasFeature(Markers.NOT)) {
				setNegated(true);
				return;
			}
		}
		// Mark.b("false");
		setNegated(false);
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		if (isNegated()) {
			Graphics2D g = (Graphics2D) graphics;
			int h = getHeight();
			int w = getWidth();
			int d = w / 10;
			// Offset from corner
			int offset = 3 * d / 2;
			// Offset from circumscribing square
			int delta = (int)((d * (1 - Math.cos(Math.PI / 4))) / 2);
			Color handle = g.getColor();
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(2f));
			g.drawLine(w - offset + d - delta, h - offset + delta, w  - offset + delta, h - offset + d - delta);
			g.setStroke(new BasicStroke(3f));
			g.drawOval(w - offset, h - offset, d, d);
			g.setColor(handle);
		}
	}

	@Override
    public void view(Object object) {
	   System.err.println("NegatableJPanel.view should not be called");
	    
    }

}
