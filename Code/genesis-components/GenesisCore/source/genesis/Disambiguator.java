package genesis;

import javax.swing.JPanel;


import connections.*;
import constants.Markers;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/*
 * Inspired by proof-of-concept by Tom Larson, which he based on a nearness
 * metric. Created on Jan 4, 2012
 * @author phw
 */

public class Disambiguator extends JPanel implements WiredBox {

	boolean autoAddToLibrary = true;

	public static String ADD = "add to library";

	public static String CLEAR = "flush library";

	public static String RESOLVE = "resolve classifications";

	private int maxUnion = 0, minExampleBranch = 1, minHistoryBranch = 2;

	private int mode = minHistoryBranch;

	private Sequence library;

	private boolean debug = false;

	private boolean debugGeneralizing = true;

	private boolean showBestMatches = true;

	public Disambiguator() {
		Connections.getPorts(this).addSignalProcessor("process");
		Connections.getPorts(this).addSignalProcessor(ADD, "addToLibrary");
		Connections.getPorts(this).addSignalProcessor(CLEAR, "flushLibrary");
		Connections.getPorts(this).addSignalProcessor(RESOLVE, "resolve");
	}

	public void process(Object o) {
		if (true) {
			return;
		}
		if (o instanceof Entity) {
			Entity t = (Entity) o;
			boolean ambiguous = ambiguous(t);
			if (!ambiguous) {
				Mark.say("Adding to disambiguation library:", t.asString());
				addToLibrary(t);
			}
			else {
				Mark.say("Disambiguating ", t.asString());
				resolve(t);
			}
		}
	}

	private boolean ambiguous(Entity t) {
		Bundle b = t.getBundle();
		int count = 0;
		for (Thread x : b) {
			if (x.firstElement().equals("action") || x.firstElement().equals("thing")) {
				++count;
			}
			if (count > 1) {
				Mark.say("Too many threads for", this.extractType(x));
				return true;
			}
		}
		if (t.functionP()) {
			return ambiguous(t.getSubject());
		}
		else if (t.relationP()) {
			return ambiguous(t.getSubject()) || ambiguous(t.getObject());
		}
		else if (t.sequenceP()) {
			for (Entity x : t.getElements()) {
				if (ambiguous(x)) {
					return true;
				}
			}
		}
		return false;
	}

	public Sequence getLibrary() {
		if (library == null) {
			library = new Sequence();
		}
		return library;
	}

	public void addToLibrary(Object t) {
		if (t instanceof Entity) {
			getLibrary().addElement((Entity) t);
		}

	}

	public void flushLibrary(Object t) {
		getLibrary().getElements().clear();
	}

	public void resolve(Object o) {
		if (!(o instanceof Entity)) {
			return;
		}
		if (mode != minHistoryBranch) {
			Mark.err("No longer support selected mode");
		}
		Entity t = (Entity) o;
		int bestScore = 0;
		Entity bestReference = null;
		LList<PairOfEntities> bestBindingList = null;
		if (mode == minExampleBranch || mode == minHistoryBranch) {
			bestScore = 1000;
		}
		for (Entity memory : getLibrary().getElements()) {
			LList<PairOfEntities> match = match(t, memory);
			if (match == null) {
				continue;
			}
			int thisScore = 0;
			for (PairOfEntities p : match) {
				int score = getBestScore(p);
				// Something doesn't match at all
				if (score < 0) {
					continue;
				}
				thisScore += score;
				if (thisScore > bestScore) {
					Mark.say(debug, "No need to continue,", thisScore, ">", bestScore);
					Mark.say(debug, "Loser", thisScore, memory.asString());
					continue;
				}
			}
			// Mark.say("Bindings:", match, thisScore);
			if (thisScore < bestScore) {
				bestScore = thisScore;
				bestReference = memory;
				bestBindingList = match;
				Mark.say(debug, "Improvement", thisScore, memory.asString());
			}
		}
		// Have the winning memory now
		if (bestReference != null) {
			Mark.say(debug, "Best reference at", bestScore, "\n" + t.asString(), "is\n" + bestReference.asString(), "\n" + bestBindingList);
			disambiguate(bestBindingList);
			// addToLibrary(t);
			// At this point, disambiguation has occurred, so can generalize on
			// the experience
			doGeneralize(t, bestReference, bestBindingList);

		}
		else {
			Mark.err("Could not find reference for", t.asString());
		}
	}

	private Entity extractObject(Entity t) {
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity e : t.getObject().getElements()) {
				if (e.functionP(Markers.OBJECT_MARKER)) {
					return e.getSubject();
				}
			}
		}
		return null;
	}

	private void doGeneralize(Entity t, Entity bestReference, LList<PairOfEntities> l) {
		Entity result = generalize(t, l);
		// Make an announcement
		if (result != null && result.relationP()) {
			Thread thread = t.getSubject().getPrimedThread();
			Mark.say(debug, "The thread is", thread);
			
			String remark = "The " + extractType(thread) + " is a " + extractParent(thread);
			Entity o = extractObject(t);
			if (o != null) {
				thread = o.getPrimedThread();
				remark += "; the " + extractType(thread) + " is a " + extractParent(thread); 
			}
			Connections.getPorts(this).transmit(remark);
		}
		if (result != null) {
			Mark.say(debugGeneralizing, "Generalized", "\n", t.asString(), "\n", bestReference.asString(), "\nto\n", result.asString());
			if (autoAddToLibrary) {
				// addToLibrary(result);
			}
		}
	}

	private Entity generalize(Entity t, LList<PairOfEntities> l) {
		// Create new object, patterned on t, but with all binding lists
		// replaced
		// via climb tree
		Bundle b = climbTree(t, l);
		if (t.entityP()) {
			return new Entity(b);
		}
		else if (t.functionP()) {
			return new Function(b, generalize(t.getSubject(), l));
		}
		else if (t.relationP()) {
			return new Relation(b, generalize(t.getSubject(), l), generalize(t.getObject(), l));
		}
		else if (t.sequenceP()) {
			Sequence s = new Sequence(b);
			for (int i = 0; i < t.getElements().size(); ++i) {
				s.addElement(generalize(t.getElements().get(i), l));
			}
			return s;
		}
		Mark.err("Never should get here in Disambiguator.generalize");
		return null;
	}

	private Bundle climbTree(Entity t, LList<PairOfEntities> l) {
		Entity match = findMatch(t, l);
		if (match == null) {
			return (Bundle) (t.getBundle().clone());
		}
		Thread thread = new Thread();
		Bundle result = new Bundle(thread);
		for (int i = 0; i < Math.min(t.getPrimedThread().size(), match.getPrimedThread().size()); ++i) {
			String element = t.getPrimedThread().get(i);
			if (element.equals(match.getPrimedThread().get(i))) {
				thread.add(element);
			}
			else {
				break;
			}
		}
		return result;
	}

	private Entity findMatch(Entity t, LList<PairOfEntities> l) {
		for (PairOfEntities p : l) {
			if (p.getPattern() == t) {
				return p.getDatum();
			}
		}
		return null;
	}

	private void disambiguate(LList<PairOfEntities> bestBindingList) {
		for (PairOfEntities pair : bestBindingList) {
			Thread bestThread = this.getBestMatch(pair);
			int bestScore = this.getBestScore(pair);
			Mark.say(showBestMatches, "Best match", bestScore, pair.getDatum().getPrimedThread(), bestThread);
			if (bestThread.size() > 3) {
				reviseBundle(pair.getPattern(), pair.getDatum(), bestThread);
			}
		}
	}

	private void reviseBundle(Entity current, Entity reference, Thread thread) {
		current.getBundle().clear();
		current.getBundle().add(thread);
		// Mark.say("Best thread for", current.getType(), "referenced to",
		// reference.getType(), "is", thread.toString().trim());
	}

	private Object extractType(Thread best) {
		int index = best.indexOf("name");
		if (index > 0) {
			if (index - 1 >= 0) {
				return best.get(index - 1);
			}
		}
		else if (best.size() > 0) {
			return best.get(best.size() - 1);
		}
		return null;
	}

	private String extractParent(Thread best) {
		int index = best.indexOf("name");
		if (index > 0) {
			if (index - 2 >= 0) {
				return best.get(index - 2);
			}
		}
		else if (best.size() > 1) {
			return best.get(best.size() - 2);
		}
		return null;
	}

	/*
	 * Nearly same as getBestMatch
	 */
	private int getBestScore(PairOfEntities p) {
		Thread referenceThread = p.getDatum().getPrimedThread();
		Bundle candidateBundle = p.getPattern().getBundle();
		// Mark.say(debug, "Looking for best match for",
		// this.extractType(candidateBundle.get(0)));
		// boolean talk =
		// this.extractType(candidateBundle.get(0)).equals("stone") ||
		// this.extractType(candidateBundle.get(0)).equals("rock")
		// || this.extractType(candidateBundle.get(0)).equals("lake");
		// Mark.say(talk, "Looking for best match for",
		// this.extractType(candidateBundle.get(0)));
		Thread bestCandidateThread = null;
		int bestScore = -1;
		if (mode != minHistoryBranch) {
			Mark.err("No longer support selected mode");
		}
		int referenceSize = referenceThread.size();
		if (referenceSize == 0) {
			Mark.say("Reference size in Disambiguator.calculateScore mysteriously 0");
		}
		for (Thread candidateThread : candidateBundle) {
			int candidateSize = candidateThread.size();
			if (candidateSize == 0) {
				continue;
			}
			else if (!candidateThread.get(0).equals("action") && !candidateThread.get(0).equals("thing")) {
				continue;
			}
			int thisScore = calculateThreadScore(referenceThread, candidateThread, referenceSize, candidateSize);
			if (thisScore < 0) {
				continue;
			}
			else if (bestScore < 0 || thisScore < bestScore) {
				bestScore = thisScore;
				bestCandidateThread = candidateThread;
				// Improving
				Mark.say(debug, "Improving", bestScore, referenceThread, candidateThread);
			}
		}
		return bestScore;
	}

	/*
	 * Nearly same as getBestScore
	 */
	private Thread getBestMatch(PairOfEntities p) {
		Thread referenceThread = p.getDatum().getPrimedThread();
		Bundle candidateBundle = p.getPattern().getBundle();
		Thread bestCandidateThread = null;
		int bestScore = -1;
		if (mode != minHistoryBranch) {
			Mark.err("No longer support selected mode");
		}
		int referenceSize = referenceThread.size();
		if (referenceSize == 0) {
			Mark.say("Reference size in Disambiguator.calculateScore mysteriously 0");
		}
		for (Thread candidateThread : candidateBundle) {
			int candidateSize = candidateThread.size();
			if (candidateSize == 0) {
				continue;
			}
			else if (!candidateThread.get(0).equals("action") && !candidateThread.get(0).equals("thing")) {
				continue;
			}
			int thisScore = calculateThreadScore(referenceThread, candidateThread, referenceSize, candidateSize);
			if (thisScore < 0) {
				continue;
			}
			else if (bestScore < 0 || thisScore < bestScore) {
				// Improving
				bestScore = thisScore;
				bestCandidateThread = candidateThread;
			}
		}
		return bestCandidateThread;
	}

	private int calculateThreadScore(Thread referenceThread, Thread candidateThread, int referenceSize, int candidateSize) {
		// Walk down threads together until one is at end or fork point reached
		int union = 0;
		for (int i = 0; i < Math.min(referenceSize, candidateSize); ++i) {
			if (referenceThread.get(i).equals(candidateThread.get(i))) {
				// Keep going
				++union;
			}
			else {
				break;
			}
		}
		// Now check out unmatched part of reference thread
		int diff = 0;
		for (int i = union; i < referenceSize; ++i) {
			if (referenceThread.get(i).equals("name")) {
				break;
			}
			else {
				++diff;
			}
		}
		if (union == 0) {
			return -1;
		}
		return diff;
	}

	private LList<PairOfEntities> match(Entity t, Entity m) {
		return match(t, m, new LList<PairOfEntities>());
	}

	private LList<PairOfEntities> match(Entity t, Entity m, LList<PairOfEntities> lList) {
		if (lList == null) {
			return null;
		}
		else if (t.entityP() && m.entityP()) {
			return lList.cons(new PairOfEntities(t, m));
		}
		else if (t.functionP() && m.functionP()) {
			return match(t.getSubject(), m.getSubject(), lList.cons(new PairOfEntities(t, m)));
		}
		else if (t.relationP() && m.relationP()) {
			lList.cons(new PairOfEntities(t, m));
			return match(t.getSubject(), m.getSubject(), match(t.getObject(), m.getObject(), lList.cons(new PairOfEntities(t, m))));
		}
		else if (t.sequenceP() && m.sequenceP()) {
			if (t.getElements().size() != m.getElements().size()) {
				return null;
			}
			for (int i = 0; i < t.getElements().size(); ++i) {
				LList<PairOfEntities> augmentedList = match(t.getElements().get(i), m.getElements().get(i), lList);
				if (augmentedList == null) {
					// Mark.say("Failed to match sequence elements",
					// t.getElements().get(i).asString(),
					// m.getElements().get(i).asString());
					return null;
				}
				else {
					lList = augmentedList;
				}
			}
			return lList;
		}
		// Mark.say("Failed to match", t.asString(), m.asString());
		return null;
	}

	public static void main(String[] ignore) throws Exception {
		Mark.say("Starting");
		long translationTime = System.currentTimeMillis();

		Disambiguator disambiguator = new Disambiguator();

		String sentence1 = "The robin (flew fly travel) to a (tree organism)";
		String sentence2A = "The senator (flew fly travel) to a (city municipality)";

		String sentence2B = "The senator (flew fly travel) to a (conference meeting)";

		String sentence3A = "The hawk flew to the meeting";

		String sentence3B = "The hawk flew to the City";

		String sentence3C = "The hawk flew to the tree";

		String sentence4 = "The sparrow flew to the bush";

		String sentence5 = "The sparrow flew to the lake";

		String sentence6 = "The sparrow flew to the house";

		String sentence7 = "The cook has a (knife tool)";

		String sentence8 = "The soldier has a (knife weapon)";

		String sentence9 = "The cook has a knife";

		String sentence10 = "The general has a (knife weapon)";

		String sentence11 = "The general has a weapon";

		// // Sue's examples

		String sentence12 = "The (bus conveyance) (upset disturb) the (woman female).";

		String sentence13 = "The (woman female) (upset move) the bookcase.";

		String sentence14 = "The jerk upset the applecart.";

		String sentence15 = "The applecart upset the jerk.";

		// //

		disambiguator.addToLibrary(Translator.getTranslator().translate(sentence1).getElements().get(0));

		disambiguator.addToLibrary(Translator.getTranslator().translate(sentence2A).getElements().get(0));

		disambiguator.addToLibrary(Translator.getTranslator().translate(sentence7).getElements().get(0));

		disambiguator.addToLibrary(Translator.getTranslator().translate(sentence8).getElements().get(0));

		disambiguator.addToLibrary(Translator.getTranslator().translate(sentence12).getElements().get(0));

		disambiguator.addToLibrary(Translator.getTranslator().translate(sentence13).getElements().get(0));

		// disambiguator.addToLibrary(Translator.getTranslator().translate(sentence2B).getElements().get(0));

		for (Entity t : disambiguator.getLibrary().getElements()) {
			Mark.say(t.asString());
		}

		disambiguator.resolve(Translator.getTranslator().translate(sentence3A).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence3B).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence3C).getElements().get(0));

		Mark.say("++++++");

		disambiguator.resolve(Translator.getTranslator().translate(sentence3A).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence3B).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence3C).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence10).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence11).getElements().get(0));

		Mark.say("------");

		disambiguator.resolve(Translator.getTranslator().translate(sentence9).getElements().get(0));

		Mark.say("------ Sue's examples");

		disambiguator.resolve(Translator.getTranslator().translate(sentence14).getElements().get(0));

		disambiguator.resolve(Translator.getTranslator().translate(sentence15).getElements().get(0));

		Entity t = Translator.getTranslator().translate(sentence15).getElements().get(0);

		disambiguator.resolve(t);

		Mark.say("Ambiguous:", disambiguator.ambiguous(t));

		Mark.say("Stopping");

	}

}

/*
 * Examples from Sue Felshen John stuffed the turkey. = John put stuffing into
 * the turkey. The turkey stuffed John. = Eating the turkey made John feel full.
 * Mary furnished the apartment with satisfaction. = Mary felt satisfied as she
 * put furnishings in the apartment. The apartment furnished Mary with
 * satisfaction. = Possessing the apartment gave Mary satisfaction. Similar
 * examples with other verbs, e.g.,
 * "John supplied the wristwatches with promptitude." John shattered his
 * depression [by taking anti-depressants or something]. His depression
 * shattered John. Mary shattered her record. = She ran a faster race, or maybe
 * she dropped her old LP on the floor. Her record shattered Mary. = She lost
 * her job and became homeless due to her criminal record. John brushed Mary's
 * hair. = He used a brush to straighten her hair. Mary's hair brushed John. =
 * Her hair lightly contacted him as it moved. Mary upset the applecart. = She
 * knocked it over. The applecart upset Mary. = It bothered her that it was
 * illegally parked or whatever.
 */

