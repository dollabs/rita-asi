package gui;

import java.awt.*;

import javax.swing.*;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

public class TimeViewer extends NegatableJPanel {
	public final int LEFT = 1, CENTER = 2, RIGHT = 3;

	private boolean viewable = false;

	int firstSize = 1;

	int secondSize = 1;

	int lAlign = LEFT;

	int rAlign = RIGHT;

	public TimeViewer() {
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setOpaque(false);
		Connections.getPorts(this).addSignalProcessor("view");
	}

	public void setFirstSize(int size) {
		firstSize = size;
	}

	public void setSecondSize(int size) {
		secondSize = size;
	}

	public void setRAlign(int alignment) {
		rAlign = alignment;
	}

	public void setViewable(boolean b) {
		viewable = b;
	}

	public boolean isViewable() {
		return viewable;
	}

	public void paintComponent(Graphics x) {
		super.paintComponent(x);
		Graphics2D g = (Graphics2D) x;
		int width = getWidth();
		int height = getHeight();
		if (width == 0 || height == 0) {
			return;
		}
		g.drawRect(0, 0, width - 1, height - 1);
		if (!isViewable()) {
			return;
		}
		g.drawRect(0, 0, width - 1, height - 1);
		drawBar(g, true, lAlign, firstSize);
		drawBar(g, false, rAlign, secondSize);
	}

	public void drawBar(Graphics g, boolean top, int alignment, int size) {
		int height = this.getHeight();
		int width = this.getWidth();
		int pos;
		int thickness = height / 14;
		int baseLength = width / 11;
		int offset = baseLength / 2;
		if (top) {
			;
			pos = 3 * height / 7;
		}
		else {
			pos = 4 * height / 7;
		}
		int rest = (10 - size) * baseLength;
		if (alignment == CENTER) {
			offset = offset + rest / 2;
		}
		else if (alignment == RIGHT) {
			offset = offset + rest;
		}
		g.fillRect(offset, pos, size * baseLength, thickness);
	}

	public void view (Object o) {
		if (!(o instanceof Entity)) {
			return;
		}
		Entity t = (Entity) o;
		if (t.functionP(Markers.MILESTONE)) {
			firstSize = 4;
			secondSize = 4;
			lAlign = RIGHT;
			rAlign = LEFT;
		}
		else if (t.relationP()) {
			Relation frame = (Relation) t;
			if (frame.isA("before")) {
				firstSize = 4;
				secondSize = 4;
				lAlign = LEFT;
				rAlign = RIGHT;
			}
			else if (frame.isA("after")) {
				firstSize = 4;
				secondSize = 4;
				lAlign = RIGHT;
				rAlign = LEFT;
			}
			else if (frame.isA("equal")) {
				firstSize = 10;
				secondSize = 10;
				lAlign = LEFT;
				rAlign = LEFT;
			}
			else if (frame.isA("meets")) {
				firstSize = 5;
				secondSize = 5;
				lAlign = LEFT;
				rAlign = RIGHT;
			}
			else if (frame.isA("overlaps")) {
				firstSize = 7;
				secondSize = 7;
				lAlign = LEFT;
				rAlign = RIGHT;
			}
			else if (frame.isA("during") || frame.isA("while")) {
				firstSize = 6;
				secondSize = 10;
				rAlign = LEFT;
				lAlign = CENTER;
			}
			else if (frame.isA("starts")) {
				firstSize = 8;
				secondSize = 10;
				lAlign = LEFT;
				rAlign = LEFT;
			}
			else if (frame.isA("finishes")) {
				firstSize = 8;
				secondSize = 10;
				rAlign = LEFT;
				lAlign = RIGHT;
			}
		}
		else {
			System.err.println("Error:  calling TimeViewer.setParameters with a non-time frame.");
		}
		repaint();
		setViewable(true);
	}

	public void clearData() {
		setViewable(false);
	}

	public static void main(String[] args) {
		TimeViewer view = new TimeViewer();
		Entity tFrame = new Relation("finishes", new Entity(), new Entity());
		view.view(tFrame);
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}
}
