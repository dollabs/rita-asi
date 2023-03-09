package gui;

import gui.images.GuiImagesAnchor;

import javax.swing.*;

/*
 * Created on Jul 6, 2012
 * @author phw
 */

public class OnOffLabel extends JLabel {

	public static ImageIcon redIcon = new ImageIcon(GuiImagesAnchor.class.getResource("red.png"));

	public static ImageIcon greenIcon = new ImageIcon(GuiImagesAnchor.class.getResource("green.png"));

	public OnOffLabel(String name) {
		this.setText(name);
		this.turnOff();
	}

	public void turnOn() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setIcon(redIcon);
			}
		});
	}

	public void turnOff() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setIcon(greenIcon);
			}
		});
	}

	public void setState(boolean state) {
		if (state) {
			turnOn();
		}
		else {
			turnOff();
		}
	}
}
