package gui;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.TreeSet;
import subsystems.recall.MatchContribution;
import subsystems.recall.MatchWrapper;
import utils.Mark;
import connections.Connections;

/*
 * Created on Jul 16, 2010
 * @author phw
 */

public class StoryRecallViewer extends WiredPanel {

	TreeSet<MatchWrapper> wrappers = new TreeSet<MatchWrapper>();

	double maxDimensionValue = 0.0;

	double graphWidth = 0.8;

	private DecimalFormat twoPlaces = new DecimalFormat("0.00");

	public StoryRecallViewer() {
		setBackground(Color.WHITE);
		this.setName("Story recall viewer");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object o) {
		if (!(o instanceof TreeSet)) {
			return;
		}
		wrappers = (TreeSet<MatchWrapper>) o;
		calculateMaximumDimensionValue(wrappers);
		repaint();
	}

	private void calculateMaximumDimensionValue(TreeSet<MatchWrapper> wrappers2) {
		maxDimensionValue = 0.0;
		for (MatchWrapper w : wrappers) {
			for (MatchContribution c : w.getContributions()) {
				maxDimensionValue = Math.max(maxDimensionValue, c.getValue());
			}
		}
	}

	public void paintComponent(Graphics x) {
		super.paintComponent(x);
		Graphics2D g = (Graphics2D) x;
		int height = (int) (0.95 * getHeight());
		int width = getWidth();
		// Draw on graph paper 100 x 100
		if (wrappers.isEmpty() || maxDimensionValue == 0) {
			return;
		}
		double rowHeight = height / wrappers.size();
		int row = 0;
		for (MatchWrapper w : wrappers) {

			drawWrapper(g, height, width, row, wrappers.size(), wrappers.first().getContributions().size(), w);

			++row;

		}
	}

	private void drawWrapper(Graphics2D g, int height, int width, int row, int rows, int columns, MatchWrapper w) {
		String name = w.getPrecedent().getTitle();
		String value = twoPlaces.format(w.getSimilarity());
		int rowHeight = height / rows;
		int rowOffset = (int) ((row + 1) * rowHeight);
		int columnWidth = (int) (graphWidth * width / columns);
		int barWidth = (int) (0.9 * columnWidth);
		int barOffset = (int) (0.1 * rowHeight);
		int maxBarHeight = (int) (0.8 * rowHeight);
		g.drawString(name, 10, rowOffset - (int) (0.5 * rowHeight));
		g.drawString(value, 10, rowOffset - (int) (0.5 * rowHeight) + 15);
		// Mark.say("Working with row", row, name);
		int bar = 0;
		int graphOffset = ((int) ((1.0 - graphWidth) * width));

		int textXOffset = 5;
		int textYOffset = 10;

		double max = 0;
		String winner = null;

		for (MatchContribution c : w.getContributions()) {
			String barName = c.getDimension();
			int barHeight = (int) (c.getValue() * maxBarHeight / maxDimensionValue);
			g.setColor(Color.CYAN);
			g.fillRect(graphOffset + bar * columnWidth, rowOffset - barOffset - barHeight, barWidth, barHeight);
			g.setColor(Color.BLACK);
			g.drawRect(graphOffset + bar * columnWidth, rowOffset - barOffset - barHeight, barWidth, barHeight);
			if (w.getContributions().size() < 20) {
				g.drawString(barName, graphOffset + bar * columnWidth + textXOffset, rowOffset - textYOffset - barOffset);
			}
			else {
				// Mark.say("Name", barName, barHeight);
			}
			if (barHeight > max) {
				max = barHeight;
				winner = barName;
			}
			++bar;
		}
		// Mark.say("Winner is", name, winner, max);
	}

}
