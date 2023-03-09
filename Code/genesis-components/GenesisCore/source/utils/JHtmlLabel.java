package utils;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/*
 * Created on Dec 8, 2012
 * @author phw
 */

@SuppressWarnings("serial")
public class JHtmlLabel extends JLabel {
	
	private String front = "<html>";
	
	private String back = "</html>";
	
	public JHtmlLabel(String... wrappers) {
		super("", SwingConstants.CENTER);
		for (String x : wrappers) {
			front += "<" + x + ">";
			back = "</" + x + ">" + back;
		}
	}
	
	public void setText(String text) {
		super.setText(front + text + back);
	}

}
