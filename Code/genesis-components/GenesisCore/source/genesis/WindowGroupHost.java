package genesis;

import java.awt.*;

import javax.swing.*;

/*
 * A JPanel that can be a host of a JComponent managed by a window group manager Created on Sep 6, 2009
 * @author phw
 */

public class WindowGroupHost extends JPanel {

	private JMenuBar bar = null;

	private JComponent guts = null;

	private String title = null;

	public WindowGroupHost() {
		super();
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		setOpaque(true);
	}

	public JComponent getGuts() {
		return guts;
	}

	public void setGuts(JComponent guts) {
		this.guts = guts;
		this.guts.setBackground(Color.WHITE);
		this.guts.setOpaque(true);
		refresh();
	}

	public JMenuBar getMenuBar() {
		return bar;
	}

	public void setMenuBar(JMenuBar menu) {
		this.bar = menu;
		refresh();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		refresh();
	}

	private void refresh() {
		removeAll();
		if (getMenuBar() != null) {
			this.add(getMenuBar(), BorderLayout.NORTH);
		}
		if (getTitle() != null) {
			this.add(new JLabel(getTitle()), BorderLayout.SOUTH);
		}
		if (guts != null) {
			add(guts, BorderLayout.CENTER);
		}
		revalidate();

	}

}
