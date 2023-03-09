package gui.panels;

import java.awt.*;

import javax.swing.*;

/*
 * Created on Jan 10, 2006 @author Patrick
 */

public class BorderingPanel extends JPanel {

	BorderedFrameLayout layout = new BorderedFrameLayout(10);

	public BorderingPanel() {
		super();
		setBackground(Color.WHITE);
		setLayout(layout);
	}

	public BorderingPanel(int percent) {
		this();
		setBorderPercent(percent);
	}

	public BorderingPanel(Component center) {
		this();
		add(center);
	}

	public BorderingPanel(Component center, int percent) {
		this(center);
		setBorderPercent(percent);
	}

	public Component add(Component center) {
		super.add(center);
		return center;
	}

	public void setBorderPercent(int i) {
		layout.setBorderPercent(i);
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		BorderingPanel bf = new BorderingPanel();
		bf.setBackground(Color.WHITE);
		bf.setBorderPercent(20);
		JPanel panel = new JPanel();
		panel.setBackground(Color.RED);
		bf.add(panel);
		// frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(bf);

		frame.setBounds(0, 0, 500, 500);
		frame.show();
	}

}
