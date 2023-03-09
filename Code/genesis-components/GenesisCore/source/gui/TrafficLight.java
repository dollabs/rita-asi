/**
 * This displays a traffic light, which is used on the gauntlet main screen. it
 * requires the png files in the gui.images directory: stoplight-red,
 * stoplight-yellow, stoplight-green, stoplight-grey.
 */

package gui;

import gui.images.GuiImagesAnchor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import utils.Mark;

/**
 * @author Robert McIntyre June 25, 2009
 */
public class TrafficLight extends JPanel {

	private int grey = 0, red = 1, yellow = 2, green = 3; // ensures only one
														  // state at a time; 0
														  // is the grey state
														  // with no lights on.

	private Light traficLight = new Light(grey);

	private BufferedImage greyLight = null;
	{
		try {
			greyLight = ImageIO.read(new File(new GuiImagesAnchor().get("stoplight-grey.png")));
		}
		catch (IOException noGrey) {
			Mark.say("No grey traffic light");
		}
	}

	private BufferedImage redLight = null;
	{
		try {
			redLight = ImageIO.read(new File(new GuiImagesAnchor().get("stoplight-red.png")));
		}
		catch (IOException noRed) {
		}
	}

	private BufferedImage yellowLight = null;
	{
		try {
			yellowLight = ImageIO.read(new File(new GuiImagesAnchor().get("stoplight-yellow.png")));
		}
		catch (IOException noYellow) {
		}
	}

	private BufferedImage greenLight = null;
	{
		try {
			greenLight = ImageIO.read(new File(new GuiImagesAnchor().get("stoplight-green.png")));
		}
		catch (IOException noGreen) {
		}
	}

	public TrafficLight() {

		setLayout(new GridLayout(1, 1));
		setMinimumSize(new Dimension(0, 0));
		setPreferredSize(new Dimension(100, 200));
		add(traficLight);

	}

	class Light extends JComponent {

		int colorState = grey;

		private void setColor(int newColor) {
			colorState = newColor;
		}

		public Light(int color) {
			this.colorState = color;
		}

		public void paint(Graphics g) {
			int width = getWidth();
			int height = getHeight();
			if (colorState == grey) {
				g.drawImage(greyLight, 0, 0, width, height, Color.BLUE, null);
			}
			if (colorState == red) {
				g.drawImage(redLight, 0, 0, width, height, Color.BLUE, null);
			}
			if (colorState == yellow) {
				g.drawImage(yellowLight, 0, 0, width, height, Color.BLUE, null);
			}
			if (colorState == green) {
				g.drawImage(greenLight, 0, 0, width, height, Color.BLUE, null);
			}

		}

	}

	public boolean isGreen() {
		return (traficLight.colorState == green);

	}

	public void setGreen(boolean b) {
		if (b) {
			traficLight.setColor(green);
			repaint();
		}
	}

	public boolean isYellow() {
		return (traficLight.colorState == yellow);
	}

	public void setYellow(boolean b) {
		if (b) {
			traficLight.setColor(yellow);
			repaint();
		}
	}

	public boolean isRed() {
		return (traficLight.colorState == red);
	}

	public void setRed(boolean b) {
		if (b) {
			traficLight.setColor(red);
			repaint();
		}
	}

	public static void main(String[] ignore) {
		TrafficLight stopLight = new TrafficLight();
		// stopLight.setGreen(true);
		// stopLight.setYellow(true);
		// stopLight.setRed(true);
		JFrame frame = new JFrame();
		frame.getContentPane().add(stopLight);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
