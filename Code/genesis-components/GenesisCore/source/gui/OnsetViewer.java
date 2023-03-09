package gui;

/*
 * Created on May 7, 2010
 * @author phw
 */

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import utils.StringIntPair;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;

/*
 * Created on May 17, 2007 @author phw
 */
public class OnsetViewer extends JPanel implements WiredBox {

	ArrayList<StringIntPair> pairs = new ArrayList<StringIntPair>();

	public static String RESET_PORT = "reset port";

	public OnsetViewer() {
		setName("Onset viewer");
		setBackground(Color.WHITE);
		setOpaque(true);
		Connections.getPorts(this).addSignalProcessor("process");
		Connections.getPorts(this).addSignalProcessor(RESET_PORT, "reset");
	}

	public void reset(Object signal) {
		if (signal == Markers.RESET) {
			pairs.clear();
			repaint();
		}
	}

	public void process(Object x) {
		BetterSignal signal = BetterSignal.isSignal(x);
		if (signal == null) {
			return;
		}
		StringIntPair pair = new StringIntPair(signal.get(0, Object.class).toString(), 1);
		addPair(pairs, pair);
	}

	private void addPair(ArrayList<StringIntPair> pairs, StringIntPair pair) {
		for (StringIntPair element : pairs) {
			if (element.getLabel().equalsIgnoreCase(pair.getLabel())) {
				element.setValue(element.getValue() + pair.getValue());
				repaint();
				return;
			}
		}
		pairs.add(pair);
		repaint();
	}

	private void setPairs(ArrayList<StringIntPair> signal) {
		pairs = (ArrayList<StringIntPair>) signal;
		repaint();
	}

	private int maximum() {
		int max = 0;
		for (StringIntPair pair : pairs) {
			max = Math.max(max, pair.getValue());
		}
		return max;
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		int count = pairs.size();
		if (count == 0) {
			return;
		}
		Graphics2D g = (Graphics2D) graphics;
		int height = this.getHeight();
		int width = this.getWidth();
		int maximum = maximum();
		int maxBarWidth = width * 9 / 16;
		int labelWidth = width * 3 / 8;

		int halfBarHeight = height / count / 2;
		for (int i = 0; i < count; ++i) {
			StringIntPair pair = pairs.get(i);
			g.drawString(StringUtilities.prepareForDisplay(pair.getLabel()), labelWidth / 8, i * height / count + halfBarHeight);
		}

		for (int i = 0; i < count; ++i) {
			StringIntPair pair = pairs.get(i);
			int y = i * height / count;
			int h = height / count;
			g.setColor(Color.CYAN);
			int barWidth = 0;
			if (maximum > 0) {
				barWidth = pair.getValue() * maxBarWidth / maximum;
			}
			g.fillRect(labelWidth, y, barWidth, h);
			g.setColor(Color.BLACK);
			g.drawRect(labelWidth, y, barWidth, h);
			g.drawString(Integer.toString(pair.getValue()), 9 * labelWidth / 8, i * h + halfBarHeight);
			g.drawLine(0, y, width, y);
		}
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		OnsetViewer viewer = new OnsetViewer();
		ArrayList test = new ArrayList();
		// test.add(new StringIntPair("Hello", 4));
		// test.add(new StringIntPair("World", 3));
		// test.add(new StringIntPair("Hello World", 1));
		viewer.process(test);
		frame.getContentPane().add(viewer, BorderLayout.CENTER);
		frame.setBounds(0, 0, 500, 500);
		frame.setVisible(true);
	}

}
