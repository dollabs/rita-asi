package gui;

import java.awt.*;
import java.util.Vector;

import javax.swing.JFrame;


import connections.Ports;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Nov 3, 2006 @author phw
 */
public class ThreadViewer extends NegatableJPanel  {

	Entity theThing;

	private Ports ports;

	public ThreadViewer() {
		setBackground(Color.WHITE);
		
	}

	public void view(Object signal) {
		if (signal instanceof Entity) {
			Entity input = (Entity) signal;
			if (input.relationP()) {
				// A hack. Processing of thread to enter permanant thread
				// information should be in separate processor.
				Entity newThing = ((Relation) input).getObject();
				TaughtWords.getTaughtWords().add(newThing.getType(), newThing.getPrimedThread());
				setInput(newThing);
			}
		}
		setTruthValue(signal);
	}

	public static void main(String[] args) {
		ThreadViewer reader = new ThreadViewer();
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(reader, BorderLayout.CENTER);
		frame.setBounds(100, 100, 200, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		Entity thing = new Entity("rabbit");
		thing.addType("mammal");
		System.out.println(thing);
		reader.setInput(thing);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int h = getHeight();
		int w = getWidth();
		if (h == 0 || w == 0) {
			return;
		}
		g.drawRect(0, 0, w - 1, h - 1);
		if (theThing == null) {
			return;
		}
		int keepers = 4;
		Vector types = theThing.getTypes();
		Vector truncatedTypes = new Vector();
		if (types.size() > keepers) {
			truncatedTypes.addAll(types.subList(types.size() - keepers, types.size()));
			truncatedTypes.add(0, "...");
			truncatedTypes.add(0, types.get(0));
		}
		else {
			truncatedTypes = types;
		}
		int toBeShown = truncatedTypes.size();
		int spaces = toBeShown + 1;
		g.setFont(new Font("Georgia", Font.BOLD, Math.max(10, Math.min(14, h / 8))));
		int fontHeight = g.getFontMetrics().getHeight();
		for (int i = 0; i < toBeShown; ++i) {
			g.drawString(truncatedTypes.get(i).toString(), 10, (h / spaces) + i * h / spaces);
			if (i < toBeShown - 1) {
				drawArrow(g, 15, i, spaces, h);
			}
		}
	}

	private void drawArrow(Graphics g, int xOffset, int i, int spaces, int h) {
		g.setColor(Color.BLUE);
		int fontHeight = g.getFontMetrics().getHeight();
		int top = (h / spaces) + (i * h / spaces) + 5;
		int bottom = (h / spaces) + ((i + 1) * h / spaces) - fontHeight;
		if (bottom > top) {
			g.drawLine(xOffset, top, xOffset, bottom);
		}
		int barbPosition = Math.max(0, (Math.min(10, bottom - top)));
		g.drawLine(xOffset, top, xOffset - barbPosition / 2, top + barbPosition);
		g.drawLine(xOffset, top, xOffset + barbPosition / 2, top + barbPosition);
		g.setColor(Color.BLACK);
	}

	public void setInput(Object input, Object port) {
		if (input instanceof Entity) {
			setInput((Entity) input);
		}
	}

	protected void setInput(Entity thing) {
		theThing = thing;
		repaint();
	}

	public Ports getPorts() {
		if (ports == null) {
			ports = new Ports();
		}
		return ports;
	}

}
