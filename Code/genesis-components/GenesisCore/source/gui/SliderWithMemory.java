package gui;

import java.awt.*;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.*;

import utils.Mark;

/*
 * Created on May 27, 2017
 * @author phw
 */

public class SliderWithMemory extends JPanel implements ChangeListener {

	JSlider slider;

	public JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider();
		}
		return slider;
	}

	JLabel label;

	public SliderWithMemory(String name) {
		super();
		this.setName(name);
		label = new JLabel(name, JLabel.CENTER);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(getSlider());
		this.add(label);
		this.setUp();
		getSlider().setValue(Preferences.userRoot().getInt(getName(), 50));
		getSlider().addChangeListener(this);

	}



	private void setUp() {
		// slider.setBackground(Color.WHITE);
		getSlider().setOpaque(false);
		// label.setBackground(Color.WHITE);
		// label.setOpaque(true);
		this.setBackground(Color.WHITE);
	}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			Preferences.userRoot().putInt(SliderWithMemory.this.getName(), source.getValue());
			Mark.say("Value is", getSlider().getValue());

		}
	}




}
