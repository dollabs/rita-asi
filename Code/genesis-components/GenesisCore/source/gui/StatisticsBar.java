package gui;

import java.awt.Color;

import javax.swing.*;

import utils.NewTimer;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;

/*
 * Created on Mar 2, 2010
 * @author phw
 */

public class StatisticsBar extends JPanel implements WiredBox {

	// Ports

	public final static String STORY_ELEMENTS = "story elements";

	public final static String COMMONSENSE_LABEL = "commonsence label";

	public final static String REFLECTION_LABEL = "reflection label";

	public final static String COMMONSENSE_COUNT_LABEL = "commonsence count label";

	public final static String REFLECTION_COUNT_LABEL = "reflection count label";

	public final static String CLEAR_COUNTS = "clear counts";

	public static final String FROM_COUNT_PRODUCER = "story processor input port";

	// Signal markers

	public static final Object STORY_ELEMENT_COUNT = "story element count";

	public final static String EXPLICIT_STATEMENT_COUNT = "explicit statement count";

	public static final Object INFERENCE_RULE_COUNT = "inference rule count";

	public final static Object INFERENCE_COUNT = "inference count";

	public final static Object CONCEPT_COUNT = "concept count";

	public static final Object CONCEPT_DISCOVERY_COUNT = "concept dsicovery count";

	public static final Object STORY_TIMER = "story timer";

	// Labels

	private JLabel totalElementsLabel = new JLabel();

	private JLabel explicitElementsLabel = new JLabel();

	private JLabel inferredElementsLabel = new JLabel();

	private JLabel ruleLabel = new JLabel();

	private JLabel conceptLabel = new JLabel();

	private JLabel inferenceLabel = new JLabel();

	private JLabel discoveryLabel = new JLabel();

	private JLabel storyTimerLabel = new JLabel();

	private JLabel totalTimerLabel = new JLabel();

	private int explicitElementCount;

	private int totalElementCount;



	public StatisticsBar() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// this.setBorder(BorderFactory.createTitledBorder("Statistics"));

		this.setBackground(Color.WHITE);
		this.setOpaque(true);

		// Total and explicit elements
		this.add(Box.createVerticalStrut(10));
		this.add(totalElementsLabel);
		this.add(Box.createVerticalStrut(10));
		this.add(explicitElementsLabel);
		// this.add(inferredElementsLabel);
		// this.add(Box.createVerticalStrut(10));

		// Rules and concepts
		this.add(Box.createVerticalStrut(20));
		this.add(ruleLabel);
		this.add(Box.createVerticalStrut(10));
		this.add(conceptLabel);

		// Inferences and discoveries
		this.add(Box.createVerticalStrut(20));
		this.add(inferenceLabel);
		this.add(Box.createVerticalStrut(10));
		this.add(discoveryLabel);

		// Timing
		this.add(Box.createVerticalStrut(20));
		this.add(storyTimerLabel);
		this.add(Box.createVerticalStrut(10));
		this.add(totalTimerLabel);

		setTotalElementCount(0);
		setExplicitElementCount(0);
		// setInferredElementCount(0);
		setCommonsenseRuleCount(0);
		setConceptPatternCount(0);
		setCommonsenseInferenceCount(0);
		setConceptDiscoveryCount(0);
		// Connections.getPorts(this).addSignalProcessor(STORY_ELEMENTS,
		// "setStoryLabel");
		// Connections.getPorts(this).addSignalProcessor(EXPLICIT_STATEMENTS,
		// "setExplicitStatements");
		// Connections.getPorts(this).addSignalProcessor(COMMONSENSE_LABEL,
		// "setCommonsenseLabel");
		// Connections.getPorts(this).addSignalProcessor(REFLECTION_LABEL, this::setReflectionLabel);
		// Connections.getPorts(this).addSignalProcessor(COMMONSENSE_COUNT_LABEL,
		// "setCommonsenseCount");
		// Connections.getPorts(this).addSignalProcessor(REFLECTION_COUNT_LABEL,
		// "setReflectionCount");
		Connections.getPorts(this).addSignalProcessor(CLEAR_COUNTS, this::clearCounts);

		Connections.getPorts(this).addSignalProcessor(FROM_COUNT_PRODUCER, this::distributeStoryProcessorData);

		clearCounts(Markers.RESET);
	}

	public void distributeStoryProcessorData(Object o) {
		if (o instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) o;
			if (signal.get(0, String.class) == EXPLICIT_STATEMENT_COUNT) {
				setExplicitElementCount(signal.get(1, Integer.class));
			}
			else if (signal.get(0, String.class) == STORY_ELEMENT_COUNT) {
				setTotalElementCount(signal.get(1, Integer.class));
			}
			else if (signal.get(0, String.class) == INFERENCE_RULE_COUNT) {
				setCommonsenseRuleCount(signal.get(1, Integer.class));
			}
			else if (signal.get(0, String.class) == INFERENCE_COUNT) {
				setCommonsenseInferenceCount(signal.get(1, Integer.class));
			}
			else if (signal.get(0, String.class) == CONCEPT_COUNT) {
				this.setConceptPatternCount(signal.get(1, Integer.class));
			}
			else if (signal.get(0, String.class) == CONCEPT_DISCOVERY_COUNT) {
				this.setConceptDiscoveryCount(signal.get(1, Integer.class));
			}
			else if (signal.get(0, String.class) == STORY_TIMER) {
				this.setStoryTimer(signal.get(1, String.class));
			}
			this.setTotalTimer(NewTimer.statisticsBarTimer.time());
		}
	}

	public void setTotalElementCount(Object o) {
		totalElementCount = (Integer) o;
		// setInferredElementCount(totalElementCount - explicitElementCount);
		totalElementsLabel.setText("Total elements: " + o.toString());
	}

	public void clearCounts(Object object) {
		if (object == Markers.RESET) {
			setCommonsenseRuleCount(0);
			setConceptPatternCount(0);
			setCommonsenseInferenceCount(0);
			setConceptDiscoveryCount(0);
			setExplicitElementCount(0);
			// setInferredElementCount(0);
			setTotalElementCount(0);
			setStoryTimer("0.0 sec");
			setTotalTimer("0.0 sec");
			NewTimer.statisticsBarTimer.reset();
		}
	}

	public void setExplicitElementCount(Object o) {
		explicitElementCount = (Integer) o;
		// setInferredElementCount(totalElementCount - explicitElementCount);
		explicitElementsLabel.setText("Explicit elements: " + o.toString());
	}

	// public void setInferredElementCount(Object o) {
	// inferredElementsLabel.setText("Inferred elements: " + o.toString());
	// }

	public void setCommonsenseRuleCount(Object o) {
		ruleLabel.setText("Rules: " + o.toString());
	}

	public void setConceptPatternCount(Object o) {
		conceptLabel.setText("Concepts: " + o.toString());
	}

	public void setCommonsenseInferenceCount(Object o) {
		inferenceLabel.setText("Inferred elements: " + o.toString());
	}

	public void setConceptDiscoveryCount(Object o) {
		discoveryLabel.setText("Discoveries: " + o.toString());
	}

	private void setStoryTimer(String string) {
		storyTimerLabel.setText("Story reading time: " + string);
	}

	private void setTotalTimer(String string) {
		totalTimerLabel.setText("Total time elapsed: " + string);
	}

}
