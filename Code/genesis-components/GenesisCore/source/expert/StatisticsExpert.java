package expert;

import genesis.GenesisGetters;

import java.text.DecimalFormat;

import matthewFay.StoryAlignment.*;
import matthewFay.Utilities.Pair;
import mentalModels.MentalModel;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.*;
import frames.entities.Sequence;

/*
 * Created on Jun 30, 2015
 * @author phw
 */

public class StatisticsExpert extends AbstractWiredBox {

	public static final String COMPUTE = "Compute";

	public static final String COMMENTARY = "Commentary";

	private DecimalFormat formatter = new DecimalFormat("#.##");

	public StatisticsExpert(String name, CheckBoxWithMemory checkBox) {
		this(name);
		this.setGateKeeper(checkBox);
	}

	public StatisticsExpert(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(COMPUTE, this::process);
	}

	public void process(Object ignore) {
		MentalModel mm1 = GenesisGetters.getMentalModel1();
		MentalModel mm2 = GenesisGetters.getMentalModel2();
		Sequence story1 = mm1.getStoryProcessor().getStory();
		Sequence story2 = mm2.getStoryProcessor().getStory();
		if (!story1.getElements().isEmpty() && !story2.getElements().isEmpty()) {
			double rand = computeR(story1, story2);
			double f = computeF(story1, story2);
			String result = "Rand index = " + formatter.format(rand);
			result += "<br/>F number =    " + formatter.format(f);
			// Mark.say("Result", result);
			Connections.getPorts(this).transmit(COMMENTARY, new BetterSignal(GenesisConstants.RIGHT, Markers.STATISTICS_TAB, result));

		}
		else {
			Mark.say("Only one story, no statistics");
		}
	}

	public static double computeF(Sequence story1, Sequence story2) {
		SequenceAlignment sequenceAlignment = computeAlignment(story1, story2);
		int l = 0;
		int b = 0;
		int r = 0;

		for (Pair p : sequenceAlignment) {
			Object left = p.a;
			Object right = p.b;
			if (left != null && right != null) {
				++b;
			}
			else if (left != null) {
				++l;
			}
			else {
				++r;
			}
		}
		return StatisticsCalculator.computeF(l, b, r);
	}

	public static double computeR(Sequence story1, Sequence story2) {
		SequenceAlignment sequenceAlignment = computeAlignment(story1, story2);
		int l = 0;
		int b = 0;
		int r = 0;

		for (Pair p : sequenceAlignment) {
			Object left = p.a;
			Object right = p.b;
			if (left != null && right != null) {
				++b;
			}
			else if (left != null) {
				++l;
			}
			else {
				++r;
			}
		}
		return StatisticsCalculator.computeR(l, b, r, 0);
	}

	private static SequenceAlignment computeAlignment(Sequence story1, Sequence story2) {
		SortableAlignmentList sal = Aligner.getAligner().align(story1, story2);
		SequenceAlignment bestAlignment = (SequenceAlignment) sal.get(0);
		SequenceAlignment sequenceAlignment = (SequenceAlignment) bestAlignment;
		return sequenceAlignment;
	}
}
