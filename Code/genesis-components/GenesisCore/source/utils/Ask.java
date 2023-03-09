/**
 * 
 */
package utils;

import javax.swing.JOptionPane;

import com.ascent.gui.frame.ABasicFrame;

import connections.AbstractWiredBox;

/**
 * @author phw
 *
 */
public class Ask extends AbstractWiredBox {

	private static Ask ask = null;

	public static Ask getAsk() {
		if (ask == null) {
			ask = new Ask("Ask box");
		}
		return ask;
	}

	/**
	 * @param name
	 */
	private Ask(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public boolean ask(String question) {
		String[] options = new String[] { "Yes", "No", "I don't know" };
		int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), question, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (response == JOptionPane.YES_OPTION) {
			return true;
		}
		return false;
	}

	public void comment(String comment) {
		String[] options = new String[] { "Ok" };
		int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), comment, "Comment", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}

}
