package gui;

import genesis.GenesisGetters;
import storyProcessor.StoryProcessor;

import java.awt.Color;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import utils.Html;
import utils.Mark;
import conceptNet.conceptNetModel.ConceptNetJustification;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Jan 24, 2010
 * @author phw
 */

public class TabbedTextViewer extends JTabbedPane implements WiredBox {

	public static final String TAB = "switch tab";

	public static final String SILENCE = "silence";

	public static final String CLEAR = "clear";

	public static final String ALL = "all";

	public static final String SELECTED_TAB = "selected tab";

	private TextViewer currentViewer;

	private GenesisGetters getters;

	public TabbedTextViewer(GenesisGetters getters, String name) {
		this();
		this.getters = getters;
		setName(name);
		this.getters.getWindowGroupManager().addJComponent(this);

	}

	private TabbedTextViewer() {
		setOpaque(true);
		setBackground(Color.WHITE);
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int index = getSelectedIndex();
				if (index >= 0) {
					String tabName = ((JTabbedPane) e.getSource()).getTitleAt(index);
					Connections.getPorts(TabbedTextViewer.this).transmit(SELECTED_TAB, new BetterSignal(SELECTED_TAB, tabName));
				}
			}
		});
		addMouseListener(new LocalMouseListener());
		Connections.getPorts(this).addSignalProcessor(TAB, this::blowout);
		Connections.getPorts(this).addSignalProcessor(this::process);
		Connections.getPorts(this).addSignalProcessor(CLEAR, this::clear);
	}

	class LocalMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isControlDown()) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					Html.multiplier = Html.multiplier * Html.ratio;
					Mark.say("Hello bigger", Html.multiplier);
				}
				else if (SwingUtilities.isRightMouseButton(e)) {
					Html.multiplier = Html.multiplier / Html.ratio;
					Mark.say("Hello smaller", Html.multiplier);
				}
			}
		}

	}

	public void blowout(Object o) {
		// int x = 0;
		// int y = 4 / x;
		switchTab(o);
	}
	
	public void process(Object o) {
		// Mark.say("Entering TabbedTextViewer.process", o.getClass(), o);
		if (o instanceof BetterSignal) {
			BetterSignal input = (BetterSignal) o;
			// If there are three arguments, first says where to put the panel and to go ahead and show it
			// Choices are GenesisConstants.LEFT, GenesisConstants.RIGHT, & GenesisConstants.BOTTOM
			if (input.size() == 3) {
				getters.setPanel(new BetterSignal(input.get(0, String.class), this.getName()));
				process(new BetterSignal(input.get(1, Object.class), input.get(2, Object.class)));
				return;
			}
			else if (input.size() == 2) {
				switchTab(input.get(0, Object.class));
				if (input.get(1, String.class) == CLEAR) {
					// Mark.say("Clearing");
					clear(o);
				}
				else {
					currentViewer.processViaDirectCall(input.get(1, Object.class));
				}
			}
			else {
				Mark.err("Wrong number of elements in BetterSignal in TabbedTextViewer");
			}
			return;
		}
		else if (o == SILENCE) {
			currentViewer = null;
			return;
		}

		else if (currentViewer == null) {
			// Mark.say("Current viewer is null!", o);
			return;
		}
		// No tab supplied
		// switchTab("Miscellaneous");
		currentViewer.processViaDirectCall(o);
		// Mark.say("No specific tab mentioned in input to TabbedTextViewer, using Miscellaneous tab for", o);
	}

	public void switchTab(Object o) {
		// Mark.say("Entering TabbedTextViewer.switchTab", o);
		if (o == SILENCE) {
			currentViewer = null;
			return;
		}
		// Argument expected to be a string, the name of a TextBox.
		String title = o.toString();
		for (int i = 0; i < this.getTabCount(); ++i) {
			if (title.toLowerCase().equals(getTitleAt(i).toLowerCase())) {
				this.setSelectedIndex(i);
				currentViewer = (TextViewer) getSelectedComponent();
				return;
			}
		}
		// Couldn't find it, make another
		TextViewer viewer = new TextViewer();
		viewer.addContentListener(new LocalMouseListener());
		currentViewer = viewer;
		currentViewer.setOpaque(true);
		currentViewer.setBackground(Color.WHITE);
		viewer.setName(title);
		this.addTab(title, viewer);
		this.setSelectedIndex(this.getTabCount() - 1);
	}

	public void clear(Object o) {
		if (ALL == o) {
			// Mark.say("Clearing all");
			this.removeAll();
		}
		else if (currentViewer != null) {
			currentViewer.clear();
		}
	}

	public void clear() {
		this.removeAll();
	}

}
