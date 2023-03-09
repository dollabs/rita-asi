package translator;

import javax.swing.JFrame;



/*
 * Created on Dec 6, 2007 @author phw
 */

public class LocalDemo extends Demo {
	public static void main(String[] ignore) {
		final Demo d = new Demo();
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(d);
		frame.setSize(800, 600);
		frame.setVisible(true);
		RuleSet.reportSuccess = true;

	}
}
