package genesis;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import utils.Mark;

import com.ascent.gui.swing.utilities.TitledJPanel;

import constants.GenesisConstants;

/*
 * Manages a group of JComponents and hosts for those JComponents. Hosts are created by getters. JComponents are added
 * to the group of JComponents managed.
 * @created on Sep 5, 2009
 * @author phw
 */

public class WindowGroupManager {

	private ArrayList<ComponentWrapper> componentWrappers;

	private ArrayList<WindowGroupHost> hosts = new ArrayList<WindowGroupHost>();

	private MyActionListener actionListener;

	private MyOtherActionListener otherListener;

	private int selectionBarLimit = 5;

	private JPanel summaryPanel;

	private String name;

	public WindowGroupManager() {
	}

	/*
	 * Constructor that specifies the number of component names showed on selection bar
	 */
	public WindowGroupManager(int menuLimit) {
		this.selectionBarLimit = menuLimit;
		this.addJComponent(getPopper());
		this.addJComponent(getSummaryPanel());
	}

	JLabel popper;

	PopperFrame popperFrame;

	public void popPopperFrame(ComponentWrapper wrapper, ArrayList<WindowGroupHost> hosts) {
		popperFrame = new PopperFrame(wrapper, hosts);
		popperFrame.setSize(800, 500);
		popperFrame.setBackground(Color.WHITE);

		TitledJPanel popperPanel = new TitledJPanel(wrapper.getName());

		popperPanel.setMainPanel(wrapper.getComponent());

		resetMenu(wrapper, false, hosts);

		popperFrame.getContentPane().add(popperPanel);

		popperFrame.setVisible(true);

		Mark.say("Setting", wrapper, "to solo");
		wrapper.setSolo(true);

	}

	public JLabel getPopper() {
		if (popper == null) {
			popper = new JLabel("Popper label");
			popper.setName("Pop");
		}
		return popper;
	}

	private class PopperFrame extends JFrame {
		ComponentWrapper wrapper;

		ArrayList<WindowGroupHost> hosts;

		public PopperFrame(ComponentWrapper wrapper, ArrayList<WindowGroupHost> hosts) {
			this.wrapper = wrapper;
			this.hosts = hosts;

			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					resetMenu(wrapper, true, hosts);
					Mark.say("Setting", wrapper, "to not solo");
					wrapper.setSolo(false);
				}
			});

		}

		public ComponentWrapper getWrapper() {
			return wrapper;
		}

	}

	/*
	 * Basic constructor; adds the summary panel that shows view of each JComponent in the group.
	 */
	public WindowGroupManager(String name, int menuLimit) {
		this.name = name;
		this.selectionBarLimit = menuLimit;
		this.addJComponent(getSummaryPanel());
	}

	/*
	 * Adds a JComponent to the group.
	 */
	public void addJComponent(JComponent component) {
		if (!isAlreadyListed(component.getName())) {
			getComponentWrappers().add(new ComponentWrapper(component.getName(), component));
			refreshMenus();
		}
	}

	/*
	 * Generates a new host.
	 */
	public WindowGroupHost getHost() {
		WindowGroupHost windowGroupHost = new WindowGroupHost();
		hosts.add(windowGroupHost);
		refreshMenus();
		return windowGroupHost;
	}

	/*
	 * Generates a new host and adds specified JComponent.
	 */
	public WindowGroupHost getHost(String label) {
		WindowGroupHost host = getHost();
		try {
			setGuts(host, label);
		}
		catch (Exception e) {
			Mark.err("Exception in getHost(label)");
		}
		return host;
	}

	/*
	 * Sets host to hold JComponent in the group, specified by title.
	 */
	public void setGuts(WindowGroupHost theHost, String name) {
		JComponent oldGuts = theHost.getGuts();
		JComponent newGuts = null;
		String oldTitle = theHost.getTitle();
		String newTitle = null;

		ComponentWrapper wrapper = null;

		for (ComponentWrapper pair : getComponentWrappers()) {
			if (pair.getName() != null && pair.getName().equalsIgnoreCase(name)) {
				newGuts = pair.getComponent();
				newTitle = pair.getName();
				wrapper = pair;
				break;
			}
		}
		if (newGuts == null) {
			Mark.err("Unable to find panel labeled " + name);
			return;
		}
		if (wrapper != null && wrapper.isSolo()) {
			// Oops, this component is flying solo, so don't put it in the prescribed dashboard place
			return;
		}
		theHost.setTitle(newTitle);
		theHost.setGuts(newGuts);
		for (WindowGroupHost host : hosts) {
			if (host != theHost) {
				if (host.getGuts() == newGuts) {
					host.setGuts(oldGuts);
					host.setTitle(oldTitle);
				}
			}
		}
		for (WindowGroupHost host : hosts) {
			JComponent parent = (JComponent) (host.getParent());
			host.invalidate();
			host.revalidate();
			host.repaint();
		}
	}

	/*
	 * Sets host to hold JComponent in the group, specified by JComponent itself.
	 */
	public void setGuts(WindowGroupHost theHost, JComponent component) {
		JComponent oldGuts = theHost.getGuts();
		JComponent newGuts = null;
		String oldTitle = theHost.getTitle();
		String newTitle = null;
		for (ComponentWrapper pair : getComponentWrappers()) {
			if (pair.getComponent() != null && pair.getComponent().equals(component)) {
				newGuts = pair.getComponent();
				newTitle = pair.getName();
				break;
			}
		}
		if (newGuts == null) {
			Mark.err("Unable to find tab " + component);
			return;
		}
		theHost.setTitle(newTitle);
		theHost.setGuts(newGuts);
		for (WindowGroupHost host : hosts) {
			if (host != theHost) {
				if (host.getGuts() == newGuts) {
					host.setGuts(oldGuts);
					host.setTitle(oldTitle);
				}
			}
		}
		for (WindowGroupHost host : hosts) {
			// JComponent parent = (JComponent) (host.getParent());
			host.revalidate();
			host.repaint();
		}
		getSummaryPanel();
		summaryPanel.removeAll();
		for (ComponentWrapper pair : getComponentWrappers()) {
			JComponent view = pair.getComponent();
			if (summaryPanel.getParent() != view && summaryPanel != view) {
				JPanel thumbNail = new JPanel();
				JButton label = new JButton(new ActuationAction(pair, theHost));
				if (view.getName() == null) {
					Mark.say("Missing name for", view.getClass());
					label.setText("Hello world");
				}
				// label.setBackground(Color.YELLOW);
				label.setOpaque(true);
				thumbNail.setLayout(new BorderLayout());

				if (isViewed(view)) {
					JLabel panel = new JLabel("Showing", SwingConstants.CENTER);
					panel.setBackground(Color.GRAY);
					panel.setOpaque(true);
					thumbNail.add(panel, BorderLayout.CENTER);
				}
				else {
					thumbNail.add(view, BorderLayout.CENTER);
				}
				thumbNail.add(label, BorderLayout.SOUTH);
				getSummaryPanel().add(thumbNail);

			}
		}
	}

	/*
	 * Determines if JComponent is viewed in any host.
	 */
	private boolean isViewed(JComponent view) {
		for (WindowGroupHost host : hosts) {
			if (host.getGuts() == view) {
				return true;
			}
		}
		return false;
	}

	private void refreshMenus() {

		for (WindowGroupHost host : hosts) {
			JMenuBar bar = new JMenuBar();
			JMenu menu = new JMenu("|||");

			bar.add(menu);
			for (int i = 0; i < getComponentWrappers().size(); ++i) {
				ComponentWrapper pair = getComponentWrappers().get(i);
				JMenuItem item = new JMenuItem(pair.getName());
				menu.add(item);
				item.addActionListener(getMenuListener());
				if (i < selectionBarLimit + 1) {
					JMenu element = new JMenu(pair.getName());
					bar.add(element);
					element.addMouseListener(getOtherListener());
				}
			}
			host.setMenuBar(bar);
		}
	}

	private MyOtherActionListener getOtherListener() {
		if (otherListener == null) {
			otherListener = new MyOtherActionListener();
		}
		return otherListener;
	}

	private class MyOtherActionListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			Object item = e.getSource();
			JComponent newGuts = null;
			for (WindowGroupHost host : hosts) {
				JMenuBar menubar = host.getMenuBar();
				for (int index = 0; index < menubar.getComponents().length; ++index) {
					Object object = menubar.getComponents()[index];
					if (object instanceof JMenu) {
						JMenu menu = (JMenu) object;
						if (menu == item) {
							if (index == 1) {

								// Special case, indication is to pop panel out into a separate window

								Mark.say("Hello popper");

								JComponent oldGuts = host.getGuts();

								popGuts(host, oldGuts);

							}
							else {
								newGuts = getComponentWrappers().get(index - 1).getComponent();
								Mark.say("Setting host", host.getName(), "to", getComponentWrappers().get(index - 1).getName());
								Preferences.userRoot().put(host.getName(), getComponentWrappers().get(index - 1).getName());
								setGuts(host, newGuts);
							}
							break;
						}
					}
				}
			}
		}
	}

	private void popGuts(WindowGroupHost host, JComponent oldGuts) {

		// Find the wrapper
		ComponentWrapper wrapper = null;
		int index = getComponentWrappers().size();
		for (int i = 0; i < getComponentWrappers().size(); ++i) {
			wrapper = getComponentWrappers().get(i);
			if (oldGuts == wrapper.getComponent()) {
				index = i;
				Mark.say("Found", wrapper.getName(), index);
				break;
			}
		}

		// Find the menu item

		if (wrapper == null) {
			Mark.err("Could not find component to pop");
		}
		Mark.say("Popping", wrapper.getName());

		popPopperFrame(wrapper, hosts);

		host.setGuts(new JLabel(oldGuts.getName(), JLabel.CENTER));
		host.setTitle("Popped");

	}

	public static void resetMenu(ComponentWrapper wrapper, boolean polarity, ArrayList<WindowGroupHost> hosts) {
		for (WindowGroupHost host : hosts) {
			// Deal with menu
			for (int i = 0; i < host.getMenuBar().getMenuCount(); ++i) {
				JMenu item = host.getMenuBar().getMenu(i);
				if (wrapper.getName().equals(item.getText())) {
					Mark.say("Deactivating menu item", item.getText());
					item.setEnabled(polarity);
					break;
				}
			}
			// Deal with guts
			if (polarity) {
				JComponent guts = host.getGuts();
				if (guts instanceof JLabel) {
					JLabel label = (JLabel) guts;
					if (wrapper.getName().equals(label.getText())) {
						host.setGuts(wrapper.getComponent());
						host.setTitle(wrapper.getName());
					}
				}
			}
		}
	}

	private MyActionListener getMenuListener() {
		if (actionListener == null) {
			actionListener = new MyActionListener();
		}
		return actionListener;
	}

	/*
	 * Constructing getter for summary panel.
	 */
	public JPanel getSummaryPanel() {
		if (summaryPanel == null) {
			summaryPanel = new JPanel();
			summaryPanel.setLayout(new GridLayout(0, 10));
			summaryPanel.setName("Views");
		}
		return summaryPanel;
	}

	public static void main(String[] args) {
		WindowGroupManager group = new WindowGroupManager(GenesisConstants.BOTTOM, 9);
		JPanel pane1 = group.getHost();
		JPanel pane2 = group.getHost();
		JPanel pane3 = group.getHost();

		pane1.setBackground(Color.YELLOW);
		pane2.setBackground(Color.CYAN);
		pane3.setBackground(Color.MAGENTA);

		group.addJComponent(new JLabel("A"));
		group.addJComponent(new JLabel("B"));
		group.addJComponent(new JLabel("C"));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 0));
		panel.add(pane1);
		panel.add(pane2);
		panel.add(pane3);

		JFrame frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.setSize(500, 350);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}

	private class ComponentWrapper {
		private String componentName;

		private JComponent component;

		private boolean solo = false;

		public boolean isSolo() {
			return solo;
		}

		public void setSolo(boolean solo) {
			this.solo = solo;
		}

		public String getName() {
			return componentName;
		}

		public JComponent getComponent() {
			return component;
		}

		public ComponentWrapper(String tab, JComponent choice) {
			super();
			this.componentName = tab;
			this.component = choice;
		}
	}

	private class MyActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Object item = e.getSource();
			JComponent newGuts = null;
			for (WindowGroupHost host : hosts) {
				JMenuBar menubar = host.getMenuBar();
				for (int x = 0; x < menubar.getComponents().length; ++x) {
					Object object = menubar.getComponents()[x];
					if (object instanceof JMenu) {
						JMenu menu = (JMenu) object;
						for (int i = 0; i < menu.getMenuComponents().length; ++i) {
							Component component = menu.getMenuComponents()[i];
							if (component == item) {
								ComponentWrapper wrapper = getComponentWrappers().get(i);
								newGuts = wrapper.getComponent();
								// Mark.say("Setting host", host.getName(), "to",
								// getNameComponentPairs().get(i).getComponentName());
								Preferences.userRoot().put(host.getName(), getComponentWrappers().get(i).getName());
								setGuts(host, newGuts);
								break;
							}
						}
					}
				}
			}
		}
	}

	private class ActuationAction extends AbstractAction {
		ComponentWrapper pair;

		WindowGroupHost host;

		public ActuationAction(ComponentWrapper pair, WindowGroupHost host) {
			super(pair.getComponent().getName());
			this.pair = pair;
			this.host = host;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Mark.say("Setting host", host.getName(), "to", pair.getComponentName());
			Preferences.userRoot().put(host.getName(), pair.getName());
			setGuts(host, pair.getComponent());
		}
	}

	public ArrayList<ComponentWrapper> getComponentWrappers() {
		if (componentWrappers == null) {
			componentWrappers = new ArrayList<ComponentWrapper>();
		}
		return componentWrappers;
	}

	private boolean isAlreadyListed(String name) {
		Optional optional = getComponentWrappers().stream().filter(f -> name.equalsIgnoreCase(f.getName())).findFirst();
		return optional.isPresent();
	}

}
