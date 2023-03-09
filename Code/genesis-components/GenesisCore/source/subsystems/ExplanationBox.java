package subsystems;

import java.awt.Color;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;

import genesis.GenesisControls;
import utils.*;

/*
 * Created on Feb 20, 2017
 * @author phw
 */

public class ExplanationBox extends JTextPane {
	public ExplanationBox() {
		this.setContentType("text/html");
		Border lb = BorderFactory.createLineBorder(Color.black);
		TitledBorder border = BorderFactory.createTitledBorder(lb, "Explanation");
		border.setTitleColor(Color.BLACK);
		this.setBorder(border);

	}

	public ExplanationBox(String description) {
		this();

		try {
			this.setText("<html>" + description + "</html>");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
