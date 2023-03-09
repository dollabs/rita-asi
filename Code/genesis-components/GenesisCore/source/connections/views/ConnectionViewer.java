package connections.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import connections.Test;
import connections.WiredOnOffSwitch;

/*
 * Created on Feb 25, 2009
 * @author phw
 */

@SuppressWarnings("serial")
public class ConnectionViewer extends JPanel implements Observer, MouseInputListener {

	ArrayList<ViewerBox> boxes = new ArrayList<ViewerBox>();

	ArrayList<ViewerWire> wires = new ArrayList<ViewerWire>();

	int maxHeight = 0;

	int maxWidth = 0;

	private double multiplier = 1.0;

	private double offsetX;

	private double offsetY;

	private int circleRadius = 3;

	private int circleDiameter;

	private int oldSliderValue = 100;

	private JSlider slider;

	private float[] dashes = { 5.0f };

	private BasicStroke dashed = new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, dashes, 0.0f);

	private BasicStroke dotted = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 9f }, 0f);

	// private BasicStroke dotted = new BasicStroke(3, BasicStroke.CAP_ROUND,
	// BasicStroke.JOIN_MITER);
	// private BasicStroke dashed = new BasicStroke(3, BasicStroke.CAP_ROUND,
	// BasicStroke.JOIN_MITER);

	private BasicStroke cross = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

	public ConnectionViewer() {
		super();
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		this.addMouseListener(new BoxIdentifier());
		circleDiameter = 2 * circleRadius;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public ViewerBox getBox(MouseEvent e) {
		int x = (int) ((e.getX() - offsetX) / multiplier);
		int y = (int) ((e.getY() - offsetY) / multiplier);
		for (ViewerBox box : boxes) {
			if (x >= box.getX() && x <= box.getX() + box.getWidth() && y >= box.getY() && y <= box.getY() + box.getHeight()) {
				return box;
			}
		}
		return null;
	}

	private class BoxIdentifier extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			ViewerBox box = getBox(e);
			if (box == null) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					getSlider().setValue(120 * getSlider().getValue() / 100);
				}
				else if (e.getButton() == MouseEvent.BUTTON3) {
					getSlider().setValue(80 * getSlider().getValue() / 100);
				}
			}
			else {
				if (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1 && box.getSource() instanceof WiredOnOffSwitch) {
					WiredOnOffSwitch wiredOnOffSwitch = (WiredOnOffSwitch) box.getSource();
					wiredOnOffSwitch.setSelected(!(wiredOnOffSwitch.isSelected()));
				}
				else if (e.getButton() == MouseEvent.BUTTON3) {
					String message = "Class is " + box.getSourceClass().getSimpleName();
					JOptionPane.showMessageDialog(ConnectionViewer.this, message);
				}
				else if (e.getButton() == MouseEvent.BUTTON1) {
					box.setSelected(!box.isSelected());
					revalidate();
					repaint();
				}
			}
		}
	}

	public void addWire(ViewerWire viewerWire) {
		wires.add(viewerWire);
	}

	public void addBox(ViewerBox viewerBox) {
		boxes.add(viewerBox);
		maxWidth = Math.max(maxWidth, viewerBox.getX() + viewerBox.getWidth());
		maxHeight = Math.max(maxHeight, viewerBox.getY() + viewerBox.getHeight());
	}

	public Dimension getPreferredSize() {
		if (getSlider() != null) {
			int scale = this.getSlider().getValue();
			return new Dimension(maxWidth * scale / 100, maxHeight * scale / 100);
		}
		return super.getPreferredSize();
	}

	public void paintComponent(Graphics x) {
		super.paintComponent(x);

		Graphics2D g = (Graphics2D) x;
		int width = getWidth();
		int height = getHeight();
		g.setStroke(new BasicStroke(3f));

		AffineTransform transform = g.getTransform();
		g.translate(offsetX, offsetY);
		g.scale(multiplier, multiplier);
		// First, the boxes
		try {
			for (ViewerBox viewerBox : boxes) {
				if (viewerBox.isVisible()) {
					drawBox(g, viewerBox);
				}
			}
			// Now, the wires. Make sure colored wires are on top
			for (ViewerWire viewerWire : wires) {
				if (viewerWire.isVisible()) {
					drawWire(g, viewerWire, false);
				}
			}
			for (ViewerWire viewerWire : wires) {
				if (viewerWire.isVisible()) {
					drawWire(g, viewerWire, true);
				}
			}
		}
		catch (Exception e) {
			// Can throw conncurrent modification exception
		}
		if (mouseDown) {
			g.setTransform(transform);
			drawCross(x, width, height);
		}

	}

	private void drawCross(Graphics g, int width, int height) {
		int r = 5;
		int w = width / 2;
		int h = height / 2;
		g.setColor(Color.RED);

		g.drawLine(w - r, h, w + r, h);
		g.drawLine(w, h - 4, w, h + r);
		g.setColor(Color.BLACK);

	}

	private void drawBox(Graphics2D g, ViewerBox viewerBox) {
		String label = viewerBox.getText();
		int x = viewerBox.getX();
		int y = viewerBox.getY();
		int w = viewerBox.getWidth();
		int h = viewerBox.getHeight();
		if (viewerBox.getState() == ViewerBox.BLEW_OUT) {
			g.setColor(Color.RED);
		}
		else if (viewerBox.isSelected()) {
			g.setColor(Color.PINK);
		}
		else {
			g.setColor(viewerBox.getColor());
		}
		if (viewerBox.getSwitchState() == ViewerBox.NEITHER) {
			g.fillRect(x, y, w, h);
		}
		else {
			g.fillOval(x, y, w, h);
		}

		g.setColor(Color.BLACK);
		Rectangle rectangle = new Rectangle(x, y, w, h);
		Font font = g.getFont();
		g.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 5));
		drawLabel(g, label, rectangle);
		g.setFont(font);
		if (viewerBox.isToggleSwitch()) {
			Stroke handle = g.getStroke();
			g.setStroke(cross);
			g.setColor(Color.RED);
			int xOffset = (int) (0.5 * w);
			int headXOffset = (int) (0.15 * w);
			int headYOffset = (int) (0.2 * h);
			int yOffset = (int) (0.05 * h);
			g.drawLine(x + xOffset, y + yOffset, x + xOffset, y + h - yOffset);
			if (viewerBox.getSwitchState() == ViewerBox.ON_SWITCH) {
				g.drawLine(x + xOffset, y + yOffset, x + xOffset + headXOffset, y + headYOffset);
				g.drawLine(x + xOffset, y + yOffset, x + xOffset - headXOffset, y + headYOffset);
			}
			else {
				g.drawLine(x + xOffset, y + h - yOffset, x + xOffset + headXOffset, y + h - yOffset - headYOffset);
				g.drawLine(x + xOffset, y + h - yOffset, x + xOffset - headXOffset, y + h - yOffset - headYOffset);
			}
			g.setStroke(handle);
		}
		else if (viewerBox.getSwitchState() == ViewerBox.OFF_SWITCH) {
			Stroke handle = g.getStroke();
			g.setStroke(cross);
			g.setColor(Color.RED);
			int yOffset = (int) (0.15 * h);
			int y2Offset = yOffset;
			int xOffset = (int) (0.15 * w);
			int x2Offset = xOffset;
			g.drawLine(x + xOffset, y + yOffset, x + w - x2Offset, y + h - y2Offset);
			g.drawLine(x + xOffset, y + h - yOffset, x + w - x2Offset, y + y2Offset);
			g.setStroke(handle);
		}
		else if (viewerBox.getSwitchState() == ViewerBox.ON_SWITCH) {
			Stroke handle = g.getStroke();
			g.setStroke(cross);
			g.setColor(Color.RED);
			int yOffset = (int) (0.75 * h);
			int xOffset = (int) (0.15 * w);

			int headXOffset = (int) (0.2 * w);
			int headYOffset = (int) (0.15 * h);

			g.drawLine(x + xOffset, y + yOffset, x + w - xOffset, y + yOffset);
			g.drawLine(x + w - xOffset, y + yOffset, x + w - xOffset - headXOffset, y + yOffset + headYOffset);
			g.drawLine(x + w - xOffset, y + yOffset, x + w - xOffset - headXOffset, y + yOffset - headYOffset);
			g.setStroke(handle);
		}

		g.setColor(Color.BLACK);

		if (viewerBox.isNegative()) {
			// Stroke handle = g.getStroke();
			// g.setStroke(cross);
			g.setColor(Color.RED);
			// g.drawLine(x + w, y, x, y + h);
			// g.setStroke(handle);
		}
		if (viewerBox.getSwitchState() == ViewerBox.NEITHER) {
			if (viewerBox.isDotted()) {
				Stroke handle = g.getStroke();
				g.setStroke(dashed);
				g.drawRect(x, y, w, h);
				g.setStroke(handle);
			}
			else {
				g.drawRect(x, y, w, h);
			}
		}
		else {
			g.drawOval(x, y, w, h);
		}
	}

	private void drawLabel(Graphics g, String label, Rectangle rectangle) {
		String[] words = Pattern.compile(" ").split(label);
		FontMetrics fm = g.getFontMetrics();
		int width = rectangle.width;
		ArrayList<String> result = new ArrayList<String>();
		String row = "";
		int spaceWidth = fm.stringWidth(" ");
		int maxWidth = 0;
		for (String word : words) {
			int rowWidth = fm.stringWidth(row);
			int wordWidth = fm.stringWidth(word);
			if (rowWidth == 0) {
				row = word;
			}
			else if (rowWidth + spaceWidth + wordWidth < width) {
				row += " " + word;
			}
			else {
				result.add(row);
				int thisWidth = fm.stringWidth(row);
				if (thisWidth > maxWidth) {
					maxWidth = thisWidth;
				}
				row = word;
			}
		}
		if (!row.isEmpty()) {
			result.add(row);
		}
		int lineCount = result.size();
		int lineHeight = g.getFontMetrics().getHeight();
		int height = lineCount * lineHeight;

		if (maxWidth > rectangle.width - 4 || height > rectangle.height - 4) {
			Font font = g.getFont();
			g.setFont(new Font(font.getName(), Font.BOLD, (int) font.getSize() - 1));
			drawLabel(g, label, rectangle);
		}
		else {
			lineHeight = g.getFontMetrics().getHeight();
			int heightOffset = ((lineCount - 1) * lineHeight) / 2;
			for (int i = 0; i < lineCount; ++i) {
				String line = result.get(i);
				int stringWidth = g.getFontMetrics().stringWidth(line);
				int x = rectangle.x + rectangle.width / 2 - stringWidth / 2;
				int y = rectangle.y + rectangle.height / 2;

				g.drawString(line, x, y - heightOffset + i * lineHeight);
			}
		}
	}

	private void drawWire(Graphics2D g, ViewerWire viewerWire, boolean drawIfSpecial) {
		ViewerBox source = viewerWire.getSource();
		ViewerBox target = viewerWire.getTarget();
		int destinationIndex = viewerWire.getDestinationIndex();
		int destinationCount = viewerWire.getDestinationCount();
		// Mark.say("drawWire gets:", destinationIndex, destinationCount);
		// String sourcePortName = viewerWire.getSourcePortName();
		// String targetPortName = viewerWire.getTargetPortName();
		// Set<String> sourcePortNames = source.getOutputPortNames();
		// Set<String> targetPortNames = target.getInputPortNames();
		// int sourceCount = sourcePortNames.size();
		// int targetCount = targetPortNames.size();
		// int sourceIndex = computeIndex(sourcePortName, sourcePortNames);
		// int targetIndex = computeIndex(targetPortName, targetPortNames);
		// Mark.say("Indexes are", sourceIndex, targetIndex);
		Stroke handle = g.getStroke();

		boolean special = false;

		if (viewerWire.isDashed()) {
			g.setColor(Color.ORANGE);
			// Can't make following work
			// g.setStroke(dashed);
			// special = true;
		}
		if (viewerWire.getColor() != null) {
			g.setColor(viewerWire.getColor());
			special = true;
		}
		else if (source.isSelected() && target.isSelected()) {
			g.setColor(Color.ORANGE);
			special = true;
		}
		else if (viewerWire.getPermanentColor() != null) {
			g.setColor(viewerWire.getPermanentColor());
		}
		else if (source.isSelected()) {
			g.setColor(Color.BLUE);
			special = true;
		}
		else if (target.isSelected()) {
			g.setColor(Color.MAGENTA);
			special = true;
		}
		if (source.isSelected() || target.isSelected() || viewerWire.getColor() != null) {
			// g.setStroke(new BasicStroke(3));
		}
		if (viewerWire.isDotted()) {
			g.setColor(Color.CYAN);
			g.setStroke(dotted);
			special = true;
		}

		if (special != drawIfSpecial) {
			g.setColor(Color.BLACK);
			g.setStroke(handle);
			return;
		}

		int sourceX = source.getX() + source.getWidth();
		int targetX = target.getX();
		int sourceY = source.getY() + source.getHeight() / 2;
		int targetY = target.getY() + destinationIndex * target.getHeight() / (destinationCount + 1);

		// Some little dots
		// Simple wire, source to immediate left of target
		g.fillRect(targetX - circleRadius, targetY - circleRadius, circleDiameter, circleDiameter);
		if (targetX > sourceX && targetX - sourceX < 2 * target.getDeltaX()) {
			// System.out.println("Showing " + viewerWire + ": " + sourceX +
			// "/" + sourceY + "; " + targetX + "/" + targetY);
			g.drawLine(sourceX, sourceY, targetX, targetY);
			g.fillRect(sourceX - circleRadius, sourceY - circleRadius, circleDiameter, circleDiameter);
		}
		// Oh, hell, there is a loop, and target is to the left
		else {
			int y1 = source.getY();
			int x1 = source.getX() + source.getWidth() * 3 / 4;
			// Which is higher
			if (source.getY() < target.getY()) {
				// Use bottom exit
				y1 += source.getHeight();
			}
			// Up half of separation
			int x2 = x1;
			int y2 = y1;
			if (source.getY() < target.getY()) {
				y2 += source.getDeltaY() / 2;
			}
			else {
				y2 -= source.getDeltaY() / 2;
			}
			g.drawLine(x1, y1, x2, y2);
			g.fillOval(x1 - circleRadius, y1 - circleRadius, circleDiameter, circleDiameter);
			// Left of target
			int x3 = target.getX() - target.getDeltaX() / 4;
			int y3 = y2;
			g.drawLine(x2, y2, x3, y3);
			g.drawLine(x3, y3, targetX, targetY);
		}
		g.setColor(Color.black);
		g.setStroke(handle);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (ViewerBox.REDO == arg) {
			reconfigure(true);
		}
		else {
			repaint();
		}
	}

	private void reconfigure(boolean withOffsets) {
		int width = getWidth();
		int height = getHeight();
		if (maxWidth == 0 || maxHeight == 0) {
			return;
		}
		double scaleX = 1.0 * width / maxWidth;
		double scaleY = 1.0 * height / maxHeight;
		multiplier = Math.min(scaleY, scaleX) * 0.9 * getSlider().getValue() / 100;
		if (withOffsets) {
			offsetX = (width - maxWidth * multiplier) / 2;
			offsetY = (height - maxHeight * multiplier) / 2;
		}
		repaint();
	}

	public static void main(String[] args) {
		Test.main(args);
	}

	public static void viewNetwork() {
		// This function allows easy visual examination of your current network.
		// Just run Connections.viewNetwork() from the same place you've been
		// calling Connections.wire(). This is a convenience function for
		// interactive network construction.
		JFrame frame = new JFrame();
		ConnectionViewer viewer = Adapter.makeConnectionAdapter().getViewer();
		frame.getContentPane().add(new JScrollPane(viewer), BorderLayout.CENTER);
		frame.setBounds(0, 0, 800, 800);
		frame.setVisible(true);
	}

	public void clear() {
		boxes.clear();
		wires.clear();
		maxHeight = 0;
		maxWidth = 0;
		multiplier = 1.0;
		offsetX = 0;
		offsetY = 0;
		getSlider().setValue(100);
	}

	private class ScaleListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			scaleChanged(e);
			reconfigure(false);
		}

	}

	private void scaleChanged(ChangeEvent e) {
		int newSliderValue = getSlider().getValue();
		int halfWidth = getWidth() / 2;
		int halfHeight = getHeight() / 2;
		offsetX = (newSliderValue * (offsetX - halfWidth) / oldSliderValue) + halfWidth;
		offsetY = (newSliderValue * (offsetY - halfHeight) / oldSliderValue) + halfHeight;
		oldSliderValue = newSliderValue;
	}

	public JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider(20, 800, oldSliderValue);
			slider.setMajorTickSpacing(20);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setBackground(Color.WHITE);
			slider.addChangeListener(new ScaleListener());
		}
		return slider;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	private int pressedAtX = -1;

	private int pressedAtY = -1;

	private double offsetXWhenPressed = -1;

	private double offsetYWhenPressed = -1;

	private boolean mouseDown;

	@Override
	public void mousePressed(MouseEvent e) {
		pressedAtX = e.getX();
		pressedAtY = e.getY();
		offsetXWhenPressed = offsetX;
		offsetYWhenPressed = offsetY;
		mouseDown = true;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressedAtX = -1;
		pressedAtY = -1;
		offsetXWhenPressed = -1;
		offsetYWhenPressed = -1;
		mouseDown = false;
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (pressedAtX != -1 && pressedAtY != -1) {
			setOffsetX(e.getX() - pressedAtX + offsetXWhenPressed);
			setOffsetY(e.getY() - pressedAtY + offsetYWhenPressed);
		}
	}

	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
		repaint();
	}

	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

}
