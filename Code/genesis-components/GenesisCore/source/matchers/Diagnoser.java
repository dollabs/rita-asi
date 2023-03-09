package matchers;

import java.util.*;
import java.util.stream.Collectors;

import com.sun.javafx.geom.Shape;

import frames.entities.Entity;
import frames.entities.Sequence;
import javafx.scene.shape.Rectangle;
import matthewFay.StoryAlignment.*;
import matthewFay.Utilities.Pair;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/**
 * An extension of Matthew Fay's aligner aimed at allowing some flexibility in order and in scoring. Intended, in part,
 * for medical situations in which symtoms are not reported in any particular order.
 * <p>
 * Created on Jun 2, 2016
 * 
 * @author phw
 */

public class Diagnoser {

	private static Diagnoser diagnoser;

	public static Diagnoser getDiagnoser() {
		if (diagnoser == null) {
			diagnoser = new Diagnoser();
		}
		return diagnoser;
	}

	private Diagnoser() {
	}

	public Score diagnose(Sequence problem) {
		Sequence bestStory = null;
		Score bestScore = null;
		double maxScore = 0.0;
		for (Sequence diagnosis : Cases.getCases()) {
			Score score = realignStories(problem, diagnosis);
			double thisScore = score.getSituationCoverage();
			if (thisScore > maxScore) {
				maxScore = thisScore;
				bestScore = score;
			}
		}
		Mark.say("Patient", bestScore.getProblem().getType() + "'s problem appears to be", bestScore.getPrecedent().getType());
		return bestScore;
	}

	public Score realignStories(Sequence situation, Sequence precedent) {
		boolean debug = false;
		Mark.say(debug, "Original situation");
		situation.stream().forEachOrdered(e -> Mark.say(debug, "Element", e));

		Aligner aligner = new Aligner();

		Score elementaryScore = alignStories(situation, precedent);

		double situationScore = elementaryScore.getSituationCoverage();
		double precedentScore = elementaryScore.getPrecedentCoverage();
		SequenceAlignment bestAlignment = elementaryScore.getAlignment();
		LList<PairOfEntities> bestBindings = elementaryScore.getBindings();

		List<Pair> losers = elementaryScore.getAlignment().stream().filter(x -> unmatchedSituationElement(x)).collect(Collectors.toList());

		losers.stream().forEachOrdered(l -> Mark.say(debug, "Loser:", l.a));

		Sequence trial = (Sequence) (situation.clone());

		for (Pair pair : losers) {

			Entity mover = (Entity) (pair.a);

			trial.removeElement(mover);

			for (int i = 0; i < trial.size(); ++i) {

				Sequence possibility = ((Sequence) (trial.clone()));
				possibility.addElement(i, mover);
				SortableAlignmentList aList = aligner.align(possibility, precedent);
				SequenceAlignment bAlignment = (SequenceAlignment) aList.get(0);

				double mCount = bAlignment.stream().map(x -> score(x)).reduce(0.0, (a, b) -> a + b);

				double sScore = mCount / situation.size();
				double pScore = mCount / precedent.size();

				if (pScore > precedentScore) {
					Mark.say(debug, "Moved", mover, "to", i);

					situationScore = sScore;
					precedentScore = pScore;
					bestAlignment = bAlignment;
					bestBindings = bestAlignment.bindings;
					Mark.say(debug, "Stats:", mCount, situation.size(), precedent.size());
					trial = possibility;
				}

			}
		}
		Mark.say(debug, "Revised situation");
		trial.stream().forEachOrdered(e -> Mark.say(debug, "Element", e));
		return new Score(situationScore, precedentScore, bestBindings, bestAlignment, situation, precedent);
	}

	public Score alignStories(Sequence situation, Sequence precedent) {

		boolean debug = false;

		Aligner aligner = new Aligner();

		// Copy, so don't screw up sources when aligning with movement
		Sequence s = (Sequence) (situation.clone());
		Sequence p = (Sequence) (precedent.clone());

		SortableAlignmentList alignmentList = aligner.align(s, p);
		
		SequenceAlignment bestAlignment = (SequenceAlignment) alignmentList.get(0);
		LList<PairOfEntities> bestBindings = bestAlignment.bindings;
		
		double matchCount = bestAlignment.stream().map(x -> score(x)).reduce(0.0, (a, b) -> a + b);
		double situationScore = matchCount / s.size();
		double precedentScore = matchCount / p.size();
		
		Mark.say(debug, "Stats:", matchCount, s.size(), p.size());
				
		return new Score(situationScore, precedentScore, bestBindings, bestAlignment, situation, precedent);
	}

	private boolean unmatchedSituationElement(Pair p) {
		if (p.a != null && p.b == null) {
			return true;
		}
		return false;
	}

	private double score(Pair p) {
		boolean debug = false;
		if (p.a != null && p.b != null) {
			Mark.say(debug, "Matched", p.a, "/", p.b);
			return 1.0;
		}
		else if (p.a != null) {
			Mark.say(debug, "No match for", p.a);
		}
		else if (p.b != null) {
			Mark.say(debug, "No match for", p.b);
		}
		return 0.0;
	}

	public class Score {
		double situationCoverage;

		double precedentCoverage;

		LList<PairOfEntities> bindings;

		SequenceAlignment alignment;

		Sequence precedent;

		Sequence problem;


		public Score(double situationCoverage, double precedentCoverage, LList<PairOfEntities> bindings, SequenceAlignment alignment, Sequence problem, Sequence precedent) {
			super();
			this.situationCoverage = situationCoverage;
			this.precedentCoverage = precedentCoverage;
			this.bindings = bindings;
			this.alignment = alignment;
			this.precedent = precedent;
			this.problem = problem;
		}

		public String toString() {
			return "<" + situationCoverage + ", " + precedentCoverage + ">";
		}

		public double getSituationCoverage() {
			return situationCoverage;
		}

		public Sequence getProblem() {
			return problem;
		}

		public double getPrecedentCoverage() {
			return precedentCoverage;
		}

		public LList<PairOfEntities> getBindings() {
			return bindings;
		}

		public SequenceAlignment getAlignment() {
			return alignment;
		}

		public Sequence getPrecedent() {
			return precedent;
		}

	}

	public static void main(String[] ignore) {
		new Diagnoser().TestA();
	}

	public void TestB() {
		List<Shape> shapes = new ArrayList<>();
	}

	public void TestC() {
		Sequence e1 = (Sequence) Translator.getTranslator().translate("George has cat.");
		Sequence e2 = (Sequence) Translator.getTranslator().translate("George has hammer.");

		SortableAlignmentList result = new Aligner().align(e1, e2);
		Mark.say("Alignment count", result.size());
		SequenceAlignment bestAlignment = (SequenceAlignment) result.get(0);
		bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		Mark.say("Alignment score:", bestAlignment.score);

		e1 = (Sequence) Translator.getTranslator().translate("George has headache.");
		e2 = (Sequence) Translator.getTranslator().translate("George has hammer.");

		result = new Aligner().align(e1, e2);
		Mark.say("Alignment count", result.size());
		bestAlignment = (SequenceAlignment) result.get(0);
		bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		Mark.say("Alignment score:", bestAlignment.score);

		e1 = (Sequence) Translator.getTranslator().translate("George has headache.");
		e2 = (Sequence) Translator.getTranslator().translate("George has angina.");

		result = new Aligner().align(e1, e2);
		Mark.say("Alignment count", result.size());
		bestAlignment = (SequenceAlignment) result.get(0);
		bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		Mark.say("Alignment score:", bestAlignment.score);

		e1 = (Sequence) Translator.getTranslator().translate("George has constipation.");
		e2 = (Sequence) Translator.getTranslator().translate("George has angina.");

		result = new Aligner().align(e1, e2);
		Mark.say("Alignment count", result.size());
		bestAlignment = (SequenceAlignment) result.get(0);
		bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		Mark.say("Alignment score:", bestAlignment.score);

		e1 = (Sequence) Translator.getTranslator().translate("George has cat.");
		e2 = (Sequence) Translator.getTranslator().translate("George has angina.");

		result = new Aligner().align(e1, e2);
		Mark.say("Alignment count", result.size());
		bestAlignment = (SequenceAlignment) result.get(0);
		bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		Mark.say("Alignment score:", bestAlignment.score);

		Mark.say("Now using basic matcher");
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(e1, e2);
		Mark.say("Bindings:", bindings);

		// Score score = diagnoser.realignStories(seqA, seqX);
		//
		// Mark.say("Patient with flu to flu:", score);
		//
		// score = diagnoser.realignStories(seqB, seqY);
		//
		// Mark.say("Patient with cold to cold:", score);
		//
		// score = diagnoser.realignStories(seqA, seqY);
		//
		// Mark.say("Patient with flu to cold:", score);
		//
		// score = diagnoser.realignStories(seqB, seqX);
		//
		// Mark.say("Patient with cold to flu:", score);
		//
		// Mark.say("\n\n");
		//
		// ////
		//
		// score = diagnoser.realignStories(seqC, seqZ);
		//
		// Mark.say("Patient C to cardiac:", score);
		//
		// score = diagnoser.realignStories(seqC, seqW);
		//
		// Mark.say("Patient C to clot:", score);
		//
		// ////
		//
		// score = diagnoser.realignStories(seqD, seqZ);
		//
		// Mark.say("Patient D to cardiac:", score);
		//
		// score = diagnoser.realignStories(seqD, seqW);
		//
		// Mark.say("Patient D to clot:", score);
		//



	}

	public void TestA() { //

		Diagnoser diagnoser = Diagnoser.getDiagnoser();

		// Create demo stories.
		Sequence seqA = Cases.patientA();
		Sequence seqB = Cases.patientB();
		Sequence seqC = Cases.patientC();
		Sequence seqD = Cases.patientD();

		Sequence seqX = Cases.FluStory();
		Sequence seqY = Cases.ColdStory();
		Sequence seqZ = Cases.CardiacStory();
		Sequence seqW = Cases.ClotStory();

		diagnoser.diagnose(seqA);
		diagnoser.diagnose(seqB);
		diagnoser.diagnose(seqC);
		diagnoser.diagnose(seqD);

		// SortableAlignmentList result = new Aligner().align(seqC, seqX);
		// Mark.say("Alignment count", result.size());
		// SequenceAlignment bestAlignment = (SequenceAlignment) result.get(0);
		// bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		// Mark.say("Alignment score:", bestAlignment.score);

	}

}
