package connections;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;

import utils.Mark;

/*
 * Created on Nov 9, 2008
 * @author phw
 */

@SuppressWarnings("serial")
public class CheckBoxWithMemory extends JCheckBox {
	


	private boolean defaultValue;

	private boolean memory = true;

	private static ArrayList<CheckBoxWithMemory> checkBoxes;

	public static ArrayList<CheckBoxWithMemory> getCheckBoxes() {
		if (checkBoxes == null) {
			checkBoxes = new ArrayList<>();
		}
		// Mark.say("Check box count", checkBoxes.size());
		return checkBoxes;
	}

	public CheckBoxWithMemory(String name) {
		this(name, true);
	}

	public CheckBoxWithMemory(String name, boolean state) {
		this(name, state, true);
	}

	public CheckBoxWithMemory(String name, boolean state, boolean m) {
		super(name);
		memory = m;
		setOpaque(false);
		addItemListener(new CheckBoxListener(this));
		// Mark.say("Booting up", name, "with", Preferences.userRoot().getBoolean(name, state));
		if (memory) {
			setSelected(Preferences.userRoot().getBoolean(name, state));
		}
		defaultValue = isSelected();

		getCheckBoxes().stream().forEach(e -> {
			if (e.getText().equals(this.getText())) {
				// Mark.say("Duplicate check box name", this.getText());
			}
		});
		getCheckBoxes().add(this);
	}

	class CheckBoxListener implements ItemListener {
		CheckBoxWithMemory button;

		public CheckBoxListener(CheckBoxWithMemory button) {
			this.button = button;
		}

		public void itemStateChanged(ItemEvent e) {
			String name = button.getText();
			// Mark.say("Setting preference for", name, "to", button.isSelected());
			Preferences.userRoot().putBoolean(name, button.isSelected());
			defaultValue = isSelected();
		}
	}

	public void reset() {
		if (defaultValue != isSelected()) {
			setSelected(!defaultValue);
			doClick();
		}
	}

	public void setDefault(boolean handle) {
		defaultValue = handle;

	}
}
