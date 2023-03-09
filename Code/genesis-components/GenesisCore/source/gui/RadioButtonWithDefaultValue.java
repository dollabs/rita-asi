package gui;

import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.JRadioButton;

import utils.Mark;

/*
 * Created on Jul 8, 2012
 * @author phw
 */

public class RadioButtonWithDefaultValue extends JRadioButton {

	private static ArrayList<RadioButtonWithDefaultValue> buttons;

	private boolean defaultValue = false;

	public RadioButtonWithDefaultValue() {
		setOpaque(false);

		getButtons().add(this);
	}

	public RadioButtonWithDefaultValue(String text) {
		super(text);
		setOpaque(false);

		getButtons().add(this);
		addItemListener(new RadioButtonListener(this));
	}

	public RadioButtonWithDefaultValue(String text, boolean selected) {
		super(text, selected);
		setOpaque(false);

		getButtons().add(this);
		defaultValue = selected;
		addItemListener(new RadioButtonListener(this));

	}

	class RadioButtonListener implements ItemListener {
		RadioButtonWithDefaultValue button;

		public RadioButtonListener(RadioButtonWithDefaultValue button) {
			this.button = button;
		}

		public void itemStateChanged(ItemEvent e) {
			String name = button.getText();
			Mark.say("Setting preference for", name, "to", button.isSelected());
			// Not stored at present
			// Preferences.userRoot().putBoolean(name, button.isSelected());
		}
	}

	public static ArrayList<RadioButtonWithDefaultValue> getButtons() {
		if (buttons == null) {
			buttons = new ArrayList<>();
		}
		// Mark.say("Check box count", checkBoxes.size());
		return buttons;
	}

	public void reset() {
		// Mark.say("Setting radio button", this.getText(), "default to", defaultValue);
		setSelected(defaultValue);
	}

}
