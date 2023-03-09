package constants;

import java.awt.event.*;

import javax.swing.JCheckBox;

/*
 * Created on May 17, 2015
 * @author phw
 */

public class Lockup implements ActionListener {
	JCheckBox b1;

	JCheckBox b2;

	public Lockup(JCheckBox b1, JCheckBox b2) {
		this.b1 = b1;
		this.b2 = b2;

		b1.addActionListener(this);
		b2.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == b1) {
			b2.setSelected(b1.isSelected());
		}
		else if (e.getSource() == b2) {
			b1.setSelected(b2.isSelected());
		}
	}
}
