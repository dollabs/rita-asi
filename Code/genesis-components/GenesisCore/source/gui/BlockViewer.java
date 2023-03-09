package gui;
import java.awt.*;

import javax.swing.*;


import connections.*;
import frames.BlockFrame;
import frames.entities.Entity;
import frames.entities.Relation;
public class BlockViewer extends JPanel implements WiredBox{
	private boolean	viewable		= false;
	private boolean	complete		= false;
	private boolean	contain			= false;
	private Color	blockerColor	= Color.gray;
	public BlockViewer() {
		this.setOpaque(false);
		Connections.getPorts(this).addSignalProcessor("setParameters");
	}
	private void drawThing(Graphics g) {
		int radius = this.getHeight() / 15;
		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;
		g.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
	}
	private void drawObstruction(Graphics g, boolean complete) {
		int x = 2 * this.getWidth() / 3;
		;
		int height = this.getHeight() / 9;
		int width = this.getWidth() / 9;
		g.fillRect(x, height, width, height);
		g.fillRect(x, 3 * height, width, height);
		g.fillRect(x, 5 * height, width, height);
		g.fillRect(x, 7 * height, width, height);
		if (complete) {
			g.fillRect(x, 2 * height, width, height);
			g.fillRect(x, 4 * height, width, height);
			g.fillRect(x, 6 * height, width, height);
		}
	}
	private void drawArrow(Graphics g) {
		int lineWidth = this.getWidth() / 5;
		int linePos = 2 * this.getHeight() / 3;
		g.drawLine(2 * lineWidth, linePos, 3 * lineWidth, linePos);
		g.drawLine(3 * lineWidth, linePos, 11 * lineWidth / 4, linePos + lineWidth / 8);
		g.drawLine(3 * lineWidth, linePos, 11 * lineWidth / 4, linePos - lineWidth / 8);
	}
	private void drawContainer(Graphics g, boolean complete) {
		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;
		int buffer = this.getHeight() / 5;
		int height = this.getHeight() / 9;
		int width = this.getHeight() / 25;
		g.fillRect(centerX - buffer, centerY - buffer, width, height);
		g.fillRect(centerX - buffer, centerY - buffer, height, width);
		g.fillRect(centerX + buffer - width, centerY + buffer - height, width, height);
		g.fillRect(centerX + buffer - height, centerY + buffer - width, height, width);
		g.fillRect(centerX + buffer - width, centerY - buffer, width, height);
		g.fillRect(centerX + buffer - height, centerY - buffer, height, width);
		g.fillRect(centerX - buffer, centerY + buffer - height, width, height);
		g.fillRect(centerX - buffer, centerY + buffer - width, height, width);
		if (complete) {
			g.fillRect(centerX - buffer, centerY - buffer + height, width, 2 * (buffer - height));
			g.fillRect(centerX + buffer - width, centerY - buffer + height, width, 2 * (buffer - height));
			g.fillRect(centerX - buffer + height, centerY - buffer, 2 * (buffer - height), width);
			g.fillRect(centerX - buffer + height, centerY + buffer - width, 2 * (buffer - height), width);
		}
	}
	@Override
	public void paintComponent(Graphics x) {
		super.paintComponent(x);
		Graphics2D g = (Graphics2D) x;
		int width = this.getWidth();
		int height = this.getHeight();
		g.drawRect(0, 0, width - 1, height - 1);
		if (width == 0 || height == 0) {
			return;
		}
		if (!this.isViewable()) {
			return;
		}
		g.drawRect(0, 0, width - 1, height - 1);
		g.setColor(Color.blue);
		this.drawThing(g);
		g.setColor(this.blockerColor);
		if (this.contain) {
			this.drawContainer(g, this.complete);
		} else {
			this.drawObstruction(g, this.complete);
			g.setColor(Color.red);
			this.drawArrow(g);
		}
	}
	public boolean isViewable() {
		return this.viewable;
	}
	public void setViewable(boolean b) {
		this.viewable = b;
	}
	public void setComplete() {
		this.complete = true;
	}
	public void setPartial() {
		this.complete = false;
	}
	public void setContain() {
		this.contain = true;
	}
	public void setObstruct() {
		this.contain = false;
	}
	private void clearData() {
		this.setViewable(false);
	}
	public void setParameters(Object input) {
		if (input instanceof Entity) {
			if (((Entity) input).isA(BlockFrame.FRAMETYPE)) {
				Relation frame = (Relation) input;
				if (BlockFrame.getBlockType(frame).equals("contains")) {
					this.setContain();
				} else if (BlockFrame.getBlockType(frame).equals("obstructs")) {
					this.setObstruct();
				} else {
					System.err.println("Invalid block type" + BlockFrame.getBlockType(frame));
				}
				if (BlockFrame.getMag(frame).equals("partial")) {
					this.setPartial();
				} else {
					this.setComplete();
				}
				this.setViewable(true);
				this.repaint();
			}
		} else if (input instanceof BlockFrame) {
			this.setParameters(((BlockFrame) input).getThing());
		} else {
			System.err.println("Error: calling BlockViewer.setParameters with a non-block frame.");
		}
	}
	public static void main(String[] args) {
		BlockViewer view = new BlockViewer();
		// BlockFrame bFrame = new BlockFrame(new Thing(), new Thing("car"), "obstructs", "partial");
		// view.getPlug().setInput(bFrame);
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}
}
