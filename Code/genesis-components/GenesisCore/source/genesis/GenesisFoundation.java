package genesis;

import gui.EasyPrint;

import java.awt.*;
import java.awt.print.*;
import java.net.URL;

import javax.swing.JPanel;

/*
 * Created on Mar 27, 2009
 * @author phw
 */

public class GenesisFoundation extends JPanel implements Printable {
	public int print(Graphics graphics, PageFormat format, int pageIndex) {
		return EasyPrint.easyPrint(this, graphics, format, pageIndex);
	}

	public void printMe() {
		EasyPrint.easyPrint(this);
	}
	
	protected static void setPreferredWidth (Component c, int w) {
		int h = c.getPreferredSize().height;
		c.setPreferredSize(new Dimension(w, h));
	}
	
	protected static void setPreferredHeight (Component c, int h) {
		int w = c.getPreferredSize().width;
		c.setPreferredSize(new Dimension(w, h));
	}
	
	protected static void setMinimumWidth (Component c, int w) {
		int h = c.getMinimumSize().height;
		c.setMinimumSize(new Dimension(w, h));
	}
	
	protected static void setMinimumHeight (Component c, int h) {
		int w = c.getMinimumSize().width;
		c.setMinimumSize(new Dimension(w, h));
	}

}
