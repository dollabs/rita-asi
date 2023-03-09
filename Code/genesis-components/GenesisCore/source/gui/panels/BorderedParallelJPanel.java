package gui.panels;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.*;

/*
 * Created on Jul 6, 2012
 * @author phw
 */

public class BorderedParallelJPanel extends ParallelJPanel {

	public BorderedParallelJPanel(String name) {
		Border lb = BorderFactory.createLineBorder(Color.black);
		TitledBorder border = BorderFactory.createTitledBorder(lb, name);
		border.setTitleColor(Color.BLACK);
		
		setBorder(border);
	}
}
