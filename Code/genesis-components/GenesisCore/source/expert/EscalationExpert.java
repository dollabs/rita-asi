package expert;

import generator.Generator;
import gui.TabbedTextViewer;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import storyProcessor.*;
import utils.*;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/*
 * @author phw
 */

public class EscalationExpert extends AbstractWiredBox {

	private ArrayList<Goldstein> goldsteinOffences = new ArrayList<Goldstein>();

	private ArrayList<Goldstein> weisOffences = new ArrayList<Goldstein>();

	private DecimalFormat onePlace = new DecimalFormat("0.0");

	public EscalationExpert() {
		super("Escalation expert");
		initializeGoldstein();
		initializeWeis();
		Connections.getPorts(this).addSignalProcessor(this::process);
	}

	public void process(Object o) {
		boolean debug = false;
		Mark.say(debug, "Escalation expert receiving");
		if (!(o instanceof ConceptAnalysis)) {
			return;
		}

		ConceptAnalysis analysis = (ConceptAnalysis) o;
		Sequence story = analysis.getStory();
		for (ConceptDescription reflection : analysis.getConceptDescriptions()) {
			String name = Punctuator.conditionName(reflection.getName());
			// Mark.say(debug, "Escalation expert received", name);
			if (!contains(name, "revenge") && !contains(name, "lesson") && !contains(name, "even")) {
				// Mark.say(debug, "Ignoring", name);
				continue;
			}
			Mark.say(debug, "Continuing using", reflection.getName(), reflection.getStoryElementsInvolved().getElements().size(), "elements");
			Entity instigator = null;
			Entity victim = null;
			Relation initialHarm = null;
			Relation reverseHarm = null;
			for (Entity t : reflection.getStoryElementsInvolved().getElements()) {
				// Mark.say(debug, "Instigator", (instigator != null ? instigator.asString() : "null"),
				// "; next element is", t.asString());
				Mark.say(debug, "Element:", t);
				if (instigator == null && t.relationP("harm")) {
					Mark.say(debug, "Looking at harm:", t.asStringWithIndexes());
					instigator = t.getSubject();
					victim = t.getObject();
					initialHarm = (Relation) t;
				}
				// Cannot look for instigator in next clause because harm to
				// harm
				// may be indirect through 3rd party
				else if (t.relationP("harm") && instigator == getObject(t)) {
					Mark.say(debug, "Looking at reverse harm:", t.asStringWithIndexes());
					reverseHarm = (Relation) t;
				}
			}
			if (initialHarm != null && reverseHarm != null) {
				Mark.say(debug, "Initial harm", initialHarm.asString());
				Mark.say(debug, "Consequent harm", reverseHarm.asString());
				processTheStory(name, story, initialHarm, reverseHarm);
			}
		}

	}

	// Expedient hack on switch to roles
	private Entity getObject(Entity t) {
		if (t.getObject().sequenceP(Markers.ROLE_MARKER)) {
			for (Entity e : t.getObject().getElements()) {
				if (e.functionP(Markers.OBJECT_MARKER)) {
					return e.getSubject();
				}
			}
		}
		return null;
	}

	private boolean contains(String name, String string) {
		if (name.toLowerCase().contains(string.toLowerCase())) {
			return true;
		}
		return false;
	}

	private void processTheStory(String name, Sequence story, Relation initialHarm, Relation reverseHarm) {
		Entity initialOffence = null;
		Entity reverseOffence = null;
		boolean debug = false;
		Mark.say(debug, "Processing the story " + story.getElements().size());

		Mark.say(debug, "Initial:", initialHarm);
		Mark.say(debug, "Reverse:", reverseHarm);
		for (Entity t : story.getElements()) {
			if (!t.relationP()) {
				continue;
			}
			else {
				if (t.isA(Markers.CAUSE_MARKER)) {
					if (t.getObject() == initialHarm) {
						Mark.say(debug, "Found cause", t.asStringWithIndexes(), initialHarm.asStringWithIndexes());
						initialOffence = backwardChain(t.getSubject().getElements(), story.getElements(), new ArrayList<Entity>());
						if (initialOffence != null) {
							Mark.say(debug, "Discovered initial cause", initialOffence.asStringWithIndexes());
							continue;
						}
					}
					else if (t.getObject() == reverseHarm) {
						Mark.say(debug, "Found result", t.asStringWithIndexes(), reverseHarm.asStringWithIndexes());
						reverseOffence = backwardChain(t.getSubject().getElements(), story.getElements(), new ArrayList<Entity>());
						if (reverseOffence != null && reverseOffence != initialOffence) {
							Mark.say(debug, "Discovered result cause", reverseOffence.asStringWithIndexes());
							break;
						}
					}
				}
			}
		}
		if (initialOffence == null) {
			Mark.say(debug, "Unable to find initial offence tracking back from", initialHarm.asString());
		}
		if (reverseOffence == null) {
			Mark.say(debug, "Unable to find retaliatory offence tracking back from", reverseHarm.asString());
		}
		if (initialOffence != null && reverseOffence != null) {
			BetterSignal gInitial = getOffence(initialOffence);
			BetterSignal gReverse = getOffence(reverseOffence);
			if (gInitial != null && gReverse != null) {
				String report = getExplanation(gInitial, initialOffence, gReverse, reverseOffence);
				BetterSignal message = new BetterSignal(Markers.GOLDSTEIN_TAB, Html.p(report));
				Mark.say(debug, "Message", report);
				Connections.getPorts(this).transmit(message);
				// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Concept analysis");
				// Connections.getPorts(this).transmit(Html.p(report));
			}
		}
	}

	private String getExplanation(BetterSignal gInitial, Entity initialOffence, BetterSignal gReverse, Entity reverseOffence) {
		boolean debug = false;
		Generator generator = Generator.getGenerator();
		double initialValue = gInitial.get(4, Double.class);
		double reverseValue = gReverse.get(4, Double.class);
		String report = "";

		report += generator.generateXPeriod(initialOffence) + " leads to " + generator.generate(reverseOffence);
		Mark.say(debug, "Verbs from entities", initialOffence.getType(), reverseOffence.getType());
		Mark.say(debug, "Verbs from results ", gInitial.get(1, String.class), gReverse.get(1, String.class));

		String explanation = "I note that " + gInitial.get(1, String.class) + " and " + gInitial.get(2, String.class) + " merge at "
		        + gInitial.get(3, String.class) + ", with " + gInitial.get(0, Integer.class) + " elements in common";
		explanation += ", and " + gReverse.get(1, String.class) + " and " + gReverse.get(2, String.class) + " merge at "
		        + gReverse.get(3, String.class) + ", with " + gReverse.get(0, Integer.class) + " elements in common.";
		// report += "The response to " + gInitial.getVerb();
		// if (!gInitial.getVerb().equals(initialOffence.getType())) {
		// report += " (" + initialOffence.getType() + ", " + bundleOverlap(initialOffence.getBundle(),
		// gInitial.getBundle()) + ")";
		// }
		// report += " with " + gReverse.getVerb();
		// if (!gReverse.getVerb().equals(reverseOffence.getType())) {
		// report += " (" + reverseOffence.getType() + ", " + bundleOverlap(reverseOffence.getBundle(),
		// gReverse.getBundle()) + ")";
		// }
		double delta = initialValue - reverseValue;
		double threshold = 1;
		Mark.say(debug, "Numbers are", initialValue, reverseValue, delta);

		String goldstein = "The Goldstein difference is " + onePlace.format(delta);
		goldstein += " (" + onePlace.format(initialValue);
		goldstein += ", " + onePlace.format(reverseValue);
		goldstein += "); using a threshold of " + onePlace.format(threshold) + ", it looks like ";
		if (delta > threshold) {
			goldstein += "an escalation.";
		}
		else if (delta < -threshold) {
			goldstein += "a de-escalation.";
		}
		else {
			goldstein += "a tit-for-tat.";
		}
		return report + Html.p(explanation) + Html.p(goldstein);
	}

	private BetterSignal getOffence(Entity s) {
		ArrayList<Goldstein> offenses = goldsteinOffences;
		Bundle wordBundle = BundleGenerator.getBundle(s.getType());
		BetterSignal bestOverlap = new BetterSignal(0, null, null, null);
		// Goldstein bestPackage = null;
		for (Goldstein offence : offenses) {
			BetterSignal signal = editDistance(wordBundle, offence.getBundle());
			if (s.getType().equalsIgnoreCase(offence.getVerb())) {
				// bestPackage = offence;
				bestOverlap = signal;
				bestOverlap.add(offence.getScore());
				break;
			}
			else if (signal.get(0, Integer.class) >= bestOverlap.get(0, Integer.class)) {
				bestOverlap = signal;
				bestOverlap.add(offence.getScore());
				// bestPackage = offence;
			}
		}
		// Mark.say("Winning word for " + s.getType() + " is ",
		// bestPackage.getVerb(), "(" + bestOverlap + ")");

		// reportBestThreads(wordBundle, bestPackage.getBundle());

		return bestOverlap;
	}

	private BetterSignal editDistance(Entity t1, Entity t2) {
		return editDistance(t1.getBundle(), t2.getBundle());
	}

	private BetterSignal editDistance(Bundle b1, Bundle b2) {
		int extremum = -1;
		BetterSignal signal = new BetterSignal(0, null, null, null);
		if (b1.isEmpty() || b2.isEmpty()) {
			return signal;
		}
		for (Thread t1 : b1) {
			for (Thread t2 : b2) {
				int count = 0;
				for (int i = 0; i < Math.min(t1.size(), t2.size()); ++i) {
					if (!(t1.get(i).equalsIgnoreCase(t2.get(i)))) {
						break;
					}
					// Uncommented version works best via Macbeth experiment only
					int editDistance;
					// editDistance = t1.size() - i + t2.size() - i;
					// if (extremum > editDistance || extremum < 0) {
					// extremum = editDistance;
					// signal = new BetterSignal(extremum, t1.lastElement(), t2.lastElement(), t1.get(i));
					// }
					editDistance = i;
					if (extremum < editDistance || extremum < 0) {
						extremum = editDistance;
						signal = new BetterSignal(extremum, t1.lastElement(), t2.lastElement(), t1.get(i));
					}

				}
			}
		}
		return signal;
	}

	public static void main(String[] ignore) {
		Entity t = new Entity("damage");
		Bundle b1 = BundleGenerator.getBundle(t.getType());
		Bundle b2 = BundleGenerator.getBundle("wound");
		Mark.say("Test", b1, b2, new EscalationExpert().editDistance(b1, b2));
	}

	private Entity backwardChain(Vector<Entity> queue, List<Entity> storyElements, ArrayList<Entity> extended) {
		Vector<Entity> newQueue = new Vector<Entity>();
		for (Entity front : queue) {
			// Mark.say("Front of queue is " + front.asString());
			if (front.isA("harm")) {
			}
			else {
				if (front.isAPrimed("action") && getOffence(front) != null) {
					return front;
				}
			}
			if (extended.contains(front)) {
				continue;
			}
			extended.add(front);
			for (Entity inference : storyElements) {
				if (!inference.relationP()) {
					continue;
				}
				else if (inference.getObject() == front) {
					newQueue.addAll(inference.getSubject().getElements());
				}
			}
		}
		if (newQueue.isEmpty()) {
			return null;
		}
		return backwardChain(newQueue, storyElements, extended);
	}

	private void initializeGoldstein() {
		try {
			String content = TextIO.readStringFromURL(EscalationExpert.class.getResource("goldstein.txt"));
			StringReader sr = new StringReader(content);
			BufferedReader br = new BufferedReader(sr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] candidate = line.split(" ");
				if (candidate.length == 2) {
					double score = Double.parseDouble(candidate[0]);
					String verb = candidate[1];
					goldsteinOffences.add(new Goldstein(score, verb, BundleGenerator.getBundle(verb)));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeWeis() {
		try {
			String content = TextIO.readStringFromURL(EscalationExpert.class.getResource("weis.txt"));
			StringReader sr = new StringReader(content);
			BufferedReader br = new BufferedReader(sr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] candidate = line.split(" ");
				if (candidate.length == 2) {
					double score = Double.parseDouble(candidate[0]);
					String verb = candidate[1];
					weisOffences.add(new Goldstein(score, verb, BundleGenerator.getBundle(verb)));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Goldstein {
		double score;

		String verb;

		Bundle bundle;

		public double getScore() {
			return score;
		}

		public String getVerb() {
			return verb;
		}

		public Bundle getBundle() {
			return bundle;
		}

		public Goldstein(double score, String verb, Bundle bundle) {
			super();
			this.score = score;
			this.verb = verb;
			this.bundle = bundle;
		}
	}

}
