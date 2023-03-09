package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

import storyProcessor.ConceptDescription;
import utils.Punctuator;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;

/*
 * Created on Mar 5, 2010
 * @author phw
 */

public class ConceptBar extends JPanel implements WiredBox {
	private static boolean debug = true;

	public static final String CLEAR_CONCEPT_BUTTONS = "clearButtons";

	public static final String CONCEPT_BUTTON = "addConceptButton";

	public static final String RESET = "reset";

	public static final String TO_ELABORATION_VIEWER = "elaboration display port";

	public static final String TO_STATISTICS_BAR = "statistics bar output port";

	private static ColorUIResource normalColorResource = new ColorUIResource(new JButton().getBackground());

	public void addConceptButton(Object input) {
		if (input == Markers.RESET) {
			clear();
		}
		else if (input instanceof JComponent) {
			Mark.say(debug, "Actuated button");
			JComponent c = (JComponent) input;
			add(c);
			// c.validate();
			// this.validate();
			this.validate();
			this.repaint();
			// Connections.getPorts(this).transmit(StatisticsBar.REFLECTION_COUNT_LABEL, getComponents().length);
			Mark.say("Data1", getComponents().length);
			Connections.getPorts(this).transmit(ConceptBar.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.CONCEPT_DISCOVERY_COUNT,
			        getComponents().length));
		}
		else if (input instanceof ConceptDescription) {
			add(makeButton((ConceptDescription) input));
			this.validate();
			this.repaint();
			// Connections.getPorts(this).transmit(StatisticsBar.REFLECTION_COUNT_LABEL, getComponents().length);
			// Mark.say("Data2", getComponents().length);
			Connections.getPorts(this).transmit(ConceptBar.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.CONCEPT_DISCOVERY_COUNT,
			        getComponents().length));
		}
	}

	// // Borrowed from old plot unit processor

	private JButton makeButton(ConceptDescription completion) {
		JButton button = new JButton(Punctuator.conditionName(completion.getName()));
		button.addActionListener(new MyButtonListener(button, completion));
		return button;
	}

	private class MyButtonListener implements ActionListener {
		private JButton button;

		private ColorUIResource normalColorResource;

		private ConceptDescription completion;

		private final ConceptDescription emptyCompletion = new ConceptDescription();

		public MyButtonListener(JButton button, ConceptDescription completion) {
			this.button = button;
			this.completion = completion;
			normalColorResource = new ColorUIResource(button.getBackground());
			// Doesn't do anything
			// yellowColorResource = new ColorUIResource(Color.YELLOW);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (button.getBackground() != Color.GREEN) {
				ConceptBar.this.resetButtons(new Object());
				Connections.getPorts(ConceptBar.this).transmit(TO_ELABORATION_VIEWER, completion);
				Connections.getPorts(ConceptBar.this).transmit(ConceptBar.RESET, ConceptBar.RESET);
				button.setBackground(Color.GREEN);
			}
			else {
				button.setBackground(normalColorResource);
				ConceptBar.this.resetButtons();
				Connections.getPorts(ConceptBar.this).transmit(TO_ELABORATION_VIEWER, emptyCompletion);
			}
		}
	}

	// //

	public void clearButtons(Object input) {
		if (input == Markers.RESET) {
			clear();
		}
	}

	public void clear() {
		this.removeAll();
		this.validate();
		this.repaint();
		Connections.getPorts(this).transmit(StatisticsBar.REFLECTION_COUNT_LABEL, getComponents().length);
	}

	public void resetButtons(Object input) {
		for (Component c : getComponents()) {
			if (c instanceof JButton) {
				c.setBackground(normalColorResource);
			}
		}
	}

	public void resetButtons() {
		for (Component c : getComponents()) {
			if (c instanceof JButton) {
				c.setBackground(normalColorResource);
			}
		}
	}

	public ConceptBar() {
		this.setLayout(new RepairedGridLayout(1, 0));
		setPreferredSize(new Dimension(0, 20));
		setBackground(Color.WHITE);
		setOpaque(true);
		Connections.getPorts(this).addSignalProcessor(CONCEPT_BUTTON, this::addConceptButton);
		Connections.getPorts(this).addSignalProcessor(CLEAR_CONCEPT_BUTTONS, this::clearButtons);
		Connections.getPorts(this).addSignalProcessor(RESET, this::resetButtons);
	}
}
