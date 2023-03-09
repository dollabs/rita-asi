package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.List;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import matthewFay.Demo;
import matthewFay.Utilities.Pair;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.EntityHelper.MatchNode;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

/**
 * They should be available, however, I can understand why they might have been missed, for silly reasons (Java not
 * playing well with nested Generic types) it can non-obvious to find. Example code: SortableAlignmentList alignments =
 * Aligner.align(story1,story2); SequenceAlignment bestAlignment = (SequenceAlignment)alignments.get(0);
 * LList<PairOfEntities> bestBindings = bestAlignment.bindings; Explanation: The Aligner class is the best way to get
 * alignments of any arbitrary sequences. It's helper functions return a SortableAlignmentList of Alignments. The
 * alignments are of type Alignment<Entity,Entity>. However, there is actually a subclass of Alignment<Entity,Entity>
 * called SequenceAlignment which has the bindings you want. Simply cast the Alignment<Entity,Entity> to
 * SequenceAlignment and you'll have access to the bindings.
 */

public class Aligner {

	public Aligner() {
	}

	private static Aligner default_aligner = null;

	public static Aligner getAligner() {
		if (default_aligner == null) default_aligner = new Aligner();

		return default_aligner;
	}

	public SortableAlignmentList align(List<Entity> listA, List<Entity> listB) {
		LList<PairOfEntities> bindings = new LList<PairOfEntities>();
		return align(listA, listB, bindings);
	}

	public SortableAlignmentList align(List<Entity> listA, List<Entity> listB, List<PairOfEntities> bindings) {
		LList<PairOfEntities> binding_llist = new LList<>();
		for (PairOfEntities pair : bindings) {
			binding_llist = binding_llist.cons(pair);
		}
		return align(listA, listB, binding_llist);
	}

	public SortableAlignmentList align(List<Entity> listA, List<Entity> listB, LList<PairOfEntities> bindings) {
		Sequence seqA = new Sequence();
		for (Entity element : listA) {
			seqA.addElement(element);
		}
		Sequence seqB = new Sequence();
		for (Entity element : listB) {
			seqB.addElement(element);
		}
		return align(seqA, seqB, bindings);
	}

	public SortableAlignmentList align(Sequence seqA, Sequence seqB) {
		LList<PairOfEntities> bindings = new LList<PairOfEntities>();
		return align(seqA, seqB, bindings);
	}

	public SortableAlignmentList align(Sequence seqA, Sequence seqB, Sequence plotUnitsA, Sequence plotUnitsB) {
		LList<PairOfEntities> bindings = new LList<PairOfEntities>();
		return align(seqA, seqB, plotUnitsA, plotUnitsB, bindings);
	}

	public SortableAlignmentList align(Sequence seqA, Sequence seqB, Sequence plotUnitsA, Sequence plotUnitsB, LList<PairOfEntities> bindings) {
		bindings = bindings.append(getPlotUnitBindings(plotUnitsA, plotUnitsB, bindings));
		return align(seqA, seqB, bindings);
	}

	public LList<PairOfEntities> getPlotUnitBindings(Sequence plotUnitsA, Sequence plotUnitsB) {
		LList<PairOfEntities> bindings = new LList<PairOfEntities>();
		return getPlotUnitBindings(plotUnitsA, plotUnitsB, bindings);
	}

	private SequenceAlignment lastReflectionAlignment;

	public SequenceAlignment getLastReflectionAlignment() {
		return lastReflectionAlignment;
	}

	public LList<PairOfEntities> getPlotUnitBindings(Sequence plotUnitsA, Sequence plotUnitsB, LList<PairOfEntities> bindings) {
		LList<PairOfEntities> plotUnitBindings = new LList<PairOfEntities>();

		// First align the plot units by name/type
		NWStringListAligner plotUnitNameAligner = new NWStringListAligner();
		ArrayList<String> plotUnitTypes1 = new ArrayList<String>();
		ArrayList<String> plotUnitTypes2 = new ArrayList<String>();
		for (int i = 0; i < plotUnitsA.getNumberOfChildren(); i++) {
			plotUnitTypes1.add(plotUnitsA.getElement(i).getType());
		}
		for (int i = 0; i < plotUnitsB.getNumberOfChildren(); i++) {
			plotUnitTypes2.add(plotUnitsB.getElement(i).getType());
		}
		Alignment<String, String> plotUnitTypeAlignment = plotUnitNameAligner.align(plotUnitTypes1, plotUnitTypes2);

		// Now iterate through the lists of reflections
		int plotUnitIterator1 = 0;
		int plotUnitIterator2 = 0;
		for (int alignmentIterator = 0; alignmentIterator < plotUnitTypeAlignment.size(); alignmentIterator++) {
			Pair<String, String> pair = plotUnitTypeAlignment.get(alignmentIterator);
			if (pair.a == null && pair.b != null) {
				// Sequence reflection2 = (Sequence)refB.getElement(reflectionIterator2);
				// No match for plot unit from B
			}
			if (pair.a != null && pair.b == null) {
				// Sequence reflection1 = (Sequence)refA.getElement(reflectionIterator1);
				// No match for plot unit from A
			}
			if (pair.a != null && pair.b != null) {
				// ///////////
				// May not be the best sequences to align - may need to 'zoom in'
				// ///////////
				Sequence plotUnit1 = (Sequence) plotUnitsA.getElement(plotUnitIterator1);
				Sequence plotUnit2 = (Sequence) plotUnitsB.getElement(plotUnitIterator2);

				SortableAlignmentList alignments = align(plotUnit1, plotUnit2, bindings);

				// Check the scores of individual elements.
				if (alignments.size() > 0) {
					SequenceAlignment bestAlignment = (SequenceAlignment) alignments.get(0);
					bestAlignment.aName = pair.a + " - A";
					bestAlignment.bName = pair.b + " - B";
					// We need to prune out the empty bindings since the reflective comparison may be incomplete!
					for (PairOfEntities plotUnitPair : bestAlignment.bindings) {
						if (!plotUnitPair.getPattern().getType().equalsIgnoreCase("thing")
						        && !plotUnitPair.getDatum().getType().equalsIgnoreCase("thing")
						        && !plotUnitPair.getPattern().getType().equalsIgnoreCase("null")
						        && !plotUnitPair.getDatum().getType().equalsIgnoreCase("null"))
						    plotUnitBindings = plotUnitBindings.cons(plotUnitPair);
					}
					lastReflectionAlignment = bestAlignment;
				}
			}
			if (pair.a != null) plotUnitIterator1++;
			if (pair.b != null) plotUnitIterator2++;
		}

		return plotUnitBindings;
	}

	private MatchTree matchTree;

	public MatchTree getLastMatchTree() {
		return matchTree;
	}

	public SortableAlignmentList align(Sequence seqA, Sequence seqB, LList<PairOfEntities> bindings) {
		NWSequenceAlignmentScorer scorer = new NWSequenceAlignmentScorer(EntityHelper.sequenceToList(seqA), EntityHelper.sequenceToList(seqB));

		// matchTree = new MatchTree(EntityHelper.getAllEntities(seqA), EntityHelper.getAllEntities(seqB),scorer);
		matchTree = new MatchTree(EntityHelper.getAllAgents(seqA), EntityHelper.getAllAgents(seqB), scorer);

		matchTree.primeMatchTree(bindings);

		matchTree.generateMatchTree();

		SortableAlignmentList alignments = new SortableAlignmentList();

		for (MatchNode leaf : matchTree.leafNodes) {
			SequenceAlignment alignment = scorer.align(leaf);

			if (seqA.getNumberOfChildren() == 0 || seqB.getNumberOfChildren() == 0) {
				Mark.say("Bad News");
				return alignments;
			}

			// Try to extract names of stories
			if (seqA.getElement(0).relationP() && seqA.getElement(0).getSubject().entityP("you")) {
				if (seqA.getElement(0).getObject().functionP(Markers.STORY_MARKER)
				        || seqA.getElement(0).getObject().functionP(Markers.CONCEPT_MARKER)) {
					alignment.aName = seqA.getElement(0).getObject().getSubject().getType();
				}
			}
			if (seqB.getElement(0).relationP() && seqB.getElement(0).getSubject().entityP("you")) {
				if (seqB.getElement(0).getObject().functionP(Markers.STORY_MARKER)
				        || seqB.getElement(0).getObject().functionP(Markers.CONCEPT_MARKER)) {
					alignment.bName = seqB.getElement(0).getObject().getSubject().getType();
				}
			}

			alignments.add(alignment);
		}
		alignments.sort();

		return alignments;
	}

	// Todo: Add versions of this with reflections if needed//
	public SortableAlignmentList alignToPatterns(Sequence seqA, ArrayList<Sequence> patterns) {
		SortableAlignmentList patternMatches = new SortableAlignmentList();
		for (Sequence pattern : patterns) {
			SortableAlignmentList currentMatches = align(seqA, pattern);
			patternMatches.add(currentMatches.get(0));
		}
		patternMatches.sort();

		return patternMatches;
	}

	public static void main(String[] args) {
		Sequence seqA = Demo.ApproachStory();
		Sequence seqB = Demo.CarryStory();

		Mark.say("Step one complete");

		Aligner aligner = new Aligner();
		SortableAlignmentList sal = aligner.align(seqA, seqB);
		SequenceAlignment bestAlignment = (SequenceAlignment) sal.get(0);
		LList<PairOfEntities> bestBindings = bestAlignment.bindings;
		Mark.say("\nBest entity bindings");
		for (PairOfEntities p : bestBindings) {
			Mark.say("Entity binding:", p);
		}

		SequenceAlignment sequenceAlignment = (SequenceAlignment) bestAlignment;
		Mark.say("\nBest element bindings");
		sequenceAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));

	}
}
