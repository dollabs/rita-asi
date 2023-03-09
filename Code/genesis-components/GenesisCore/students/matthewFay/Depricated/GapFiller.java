package matthewFay.Depricated;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import matthewFay.Demo;
import matthewFay.Utilities.Pair;
import matthewFay.Depricated.SequenceAligner.AlignmentType;
import matthewFay.StoryAlignment.RankedSequenceAlignmentSet;
import matthewFay.StoryAlignment.SequenceAlignment;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/**
 * Receives signals of patterns, and sequences with gaps and sends out sequences with filled gaps
 * 
 * @author mpfay
 */
@SuppressWarnings({ "unchecked", "deprecation" })
@Deprecated
public class GapFiller extends AbstractWiredBox {

	public static final String GAP_ALIGNMENTS_PORT = "gapAlignments";

	private boolean verbose = false;

	private ArrayList<Sequence> patternSet;

	public static String FILL_GAP = "fillgap";

	public static String ADD_PATTERN = "pattern";

	public static String CLEAR_PATTERNS = "clear";

	// TODO: Save/Load Patterns
	public static String SAVE_PATTERNS = "save";

	public static String LOAD_PATTERNS = "load";

	public void savePatterns(Object o) {
		String fileName = "gapPatterns.dat";
		if (o instanceof String) {
			if (((String) o).length() > 2) fileName = (String) o;
		}
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(patternSet);
			oos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadPatterns(Object o) {
		String fileName = "gapPatterns.dat";
		if (o instanceof String) {
			if (((String) o).length() > 2) fileName = (String) o;
		}
		try {
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fin);
			Object patternSetObject = ois.readObject();
			patternSet = (ArrayList<Sequence>) patternSetObject;
			ois.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GapFiller() {
		super("GapFiller");
		Connections.getPorts(this).addSignalProcessor("processSignal");
		patternSet = new ArrayList<Sequence>();
	}

	public void processSignal(Object o) {
		if (!Radio.alignmentButton.isSelected()) return;
		Mark.say(verbose, "GapFiller recieved a signal!");

		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal == null) return;
		try {
			String command = signal.get(0, String.class);
			if (command == ADD_PATTERN) {
				// I'll bet 0 was wrong as argument [phw]
				Sequence pattern = signal.get(1, Sequence.class);
				Mark.say(verbose, "Adding video pattern");
				reportStory(pattern);
				addPattern(pattern);
			}
			else if (command == FILL_GAP) {
				Sequence gap = signal.get(1, Sequence.class);
				Mark.say(verbose, "Filling gap in video story");
				reportStory(gap);
				fillGap(gap);
			}
			else if (command == CLEAR_PATTERNS) {
				Mark.say(verbose, "Got clear indication");
				clearPatternBuffer();
			}
			else if (command == SAVE_PATTERNS) {
				if (signal.size() == 1)
					savePatterns("");
				else
					savePatterns(signal.get(1, String.class));
			}
			else if (command == LOAD_PATTERNS) {
				if (signal.size() == 1)
					loadPatterns("");
				else
					loadPatterns(signal.get(1, String.class));
			}
			else if (command == "type") {
				setAlignmentType(signal.get(1, AlignmentType.class));
			}
			else {
				Mark.say("Unknown command received at GapFiller");
			}

		}
		catch (Exception e) {
			Mark.say("Invalid Signal Recieved at GapFiller");
		}
	}

	private void reportStory(Sequence gap) {
		for (Entity t : gap.getElements()) {
			Mark.say(verbose, t.asString());
		}
	}

	AlignmentType alignmentType = AlignmentType.FASTER;

	public void setAlignmentType(AlignmentType alignmentType) {
		this.alignmentType = alignmentType;
	}

	public void addPattern(Object o) {
		Mark.say(verbose, "GapFiller recieved an addPattern signal!");
		if (o instanceof Sequence) {
			patternSet.add(SequenceSanitizer.sanitize((Sequence) o));
			return;
		}
		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal != null) try {
			Sequence pattern = signal.get(0, Sequence.class);
			if (pattern != null) patternSet.add(SequenceSanitizer.sanitize(pattern));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean gapCheck(Entity thing) {
		if (thing.getType().equals("appear") && thing.functionP() && thing.getSubject().isA("gap")) {
			return true;
		}
		return false;
	}

	public SequenceAlignment lastAlignment = null;

	public RankedSequenceAlignmentSet<Entity, Entity> lastAlignments = null;

	public ArrayList<Integer> gapsFilledAt = null;

	public Sequence fillGap(Object o) {
		Mark.say("GapFiller recieved a fillGap signal!");
		Sequence gapSequence = null;
		if (o instanceof Sequence) {
			gapSequence = (Sequence) o;
		}
		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal == null && gapSequence == null) return null;
		try {
			gapsFilledAt = new ArrayList<Integer>();
			Sequence finishedSequence = new Sequence();
			if (gapSequence == null) {
				gapSequence = signal.get(0, Sequence.class);

			}
			if (gapSequence == null) {
				Mark.say("No sequence recieved");
				return null;
			}

			if (patternSet.isEmpty()) {
				Mark.say("No Pattern to fill gap with");
				return null;
			}

			gapSequence = SequenceSanitizer.sanitize(gapSequence);
			Mark.say("Gap Sequence: ", gapSequence.asString());

			Mark.say("Stripped Gap Sequence: ", gapSequence.asString());

			SequenceAligner aligner = new SequenceAligner();
			// Do gap wise alignment - this may be tough...
			// Create temporary sequence for alignment
			Sequence partialGapSequence = new Sequence();
			// Add elts surrounding first gap.
			@SuppressWarnings("unused")
			int gapLocation = -1;
			int partialGapLocation = -1;
			boolean gapFound = false;
			int gapIterator = 0;
			boolean firstGap = true;
			while (gapIterator < gapSequence.getNumberOfChildren()) {
				// Prepare partialGapSequence //
				if (partialGapSequence.getNumberOfChildren() > 0) {
					// Purge partial gap sequence before the gap//
					Sequence temp = new Sequence();
					int i = partialGapLocation + 1;
					while (i < partialGapSequence.getNumberOfChildren()) {
						temp.addElement(partialGapSequence.getElement(i));
						i++;
					}
					partialGapSequence = temp;
					gapFound = false;
					gapLocation = -1;
					firstGap = false;
				}
				// Find next gap and surrounding elements
				while (gapIterator < gapSequence.getNumberOfChildren()) {
					Entity t = gapSequence.getElement(gapIterator);
					if (gapCheck(t)) {
						if (!gapFound) {
							gapLocation = gapIterator;
							partialGapLocation = partialGapSequence.getNumberOfChildren();
							gapFound = true;
						}
						else {
							// Second Gap Found
							// This caps the first gap
							break;
						}
					}
					partialGapSequence.addElement(t);
					gapIterator++;
				}

				// Mark.say("iterator: ",gapIterator);
				// Mark.say("gaplocation: ", gapLocation);
				// Mark.say("paritalGaplocation: ", partialGapLocation);
				// Mark.say("gapSequence: ", gapSequence.asString());
				// Mark.say("partialGapSequence: ",partialGapSequence.asString());

				// Current gap stored at gapLocation
				// partialGapSequence contains gap and surrounding elts
				// Fill partial gap sequence

				RankedSequenceAlignmentSet<Entity, Entity> alignments = aligner.findBestAlignments(patternSet, partialGapSequence, alignmentType);

				SequenceAligner.outputAlignment(alignments);
				Connections.getPorts(this).transmit(GAP_ALIGNMENTS_PORT, new BetterSignal(alignments));

				SequenceAlignment bestAlignment = alignments.get(0);
				LList<PairOfEntities> bindings = (LList<PairOfEntities>) bestAlignment.bindings;

				int endOfPastPrediction = 0;
				int startOfPrediction = 0;
				for (int i = 0; i < bestAlignment.size(); i++) {
					if (bestAlignment.get(i).b != null) {
						endOfPastPrediction = i;
						break;
					}
				}
				for (int i = bestAlignment.size() - 1; i >= 0; i--) {
					if (bestAlignment.get(i).b != null) {
						startOfPrediction = i;
						break;
					}
				}
				boolean gapPassed = false;
				for (int i = endOfPastPrediction; i <= startOfPrediction; i++) {
					Pair<Entity, Entity> pair = bestAlignment.get(i);
					// A = Pattern, B = Datum
					if (pair.a != null) {
						if (pair.b == null) {
							// ???????????????
							// Do check that it is not forecasting or past prediction
							// ???????????????

							// Implicit Gap Found - Fill it!
							gapsFilledAt.add(finishedSequence.getNumberOfChildren());
							Relation eltToAdd = (Relation) pair.a.deepClone();
							// TODO: Do Gap Replacement Things
							eltToAdd = (Relation) findAndReplace(eltToAdd, bindings);
							// Add to Sequence
							if (firstGap || gapPassed) finishedSequence.addElement(eltToAdd);
							pair.b = eltToAdd;
						}
						else {
							// Check for explicit gap
							if (gapCheck(pair.b)) {
								gapsFilledAt.add(finishedSequence.getNumberOfChildren());
								// Explicit Gap Found - Fill it!
								Entity eltToAdd = pair.a.deepClone();
								// Relation eltToAdd = (Relation) pair.a.deepClone();
								// TODO: Do Gap Replacement Things
								eltToAdd = findAndReplace(eltToAdd, bindings);
								// eltToAdd = (Relation) findAndReplace(eltToAdd, bindings);
								// Add to Sequence
								finishedSequence.addElement(eltToAdd);
								pair.b = eltToAdd;
								gapPassed = true;
							}
							else {
								// Not explicit gap, just add it
								if (firstGap || gapPassed) finishedSequence.addElement(pair.b.deepClone());
							}
						}
					}
					else {
						if (pair.b == null) {
							// Double NULL Found - Error?
							// Do Nothing
						}
						else {
							if (gapCheck(pair.b)) {
								// Error? Odd gap match//
							}
							else {
								if (firstGap || gapPassed) finishedSequence.addElement(pair.b.deepClone());
							}
						}
					}
				}

				lastAlignment = bestAlignment;
				lastAlignments = alignments;
			}

			Mark.say("Gap Filled, Transmitting: ", finishedSequence.asString());
			Connections.getPorts(this).transmit(new BetterSignal(finishedSequence, gapsFilledAt));
			return finishedSequence;

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void clearPatternBuffer() {
		patternSet.clear();
	}

	// Returns true when a thing is foudn at the end of a chain, false otherwise
	private Entity findAndReplace(Entity element, LList<PairOfEntities> bindings) {
		if (element.entityP()) {
			// Find Replacement and return it
			for (PairOfEntities pair : bindings) {
				if (pair.getPattern().isDeepEqual(element)) {
					return pair.getDatum();
				}
			}
			return new Entity();
		}
		if (element.relationP()) {
			element.setSubject(findAndReplace(element.getSubject(), bindings));
			element.setObject(findAndReplace(element.getObject(), bindings));
			return element;
		}
		if (element.functionP()) {
			element.setSubject(findAndReplace(element.getSubject(), bindings));
			return element;
		}
		if (element.sequenceP()) {
			int i = 0;
			Sequence s = (Sequence) element;
			while (i < element.getNumberOfChildren()) {
				Entity child = element.getElement(i);
				child = findAndReplace(child, bindings);
				s.setElementAt(child, i);
				i++;
			}
			return element;
		}

		return element;
	}

	public static void main(String args[]) {
		Sequence GapStory = Demo.ComplexGapStory();
		Sequence GiveStory = Demo.GiveStory();
		Sequence ComplexTakeStory = Demo.ComplexTakeStory();

		GapFiller gf = new GapFiller();

		gf.addPattern(GiveStory);
		gf.addPattern(ComplexTakeStory);
		Mark.say(GapStory.asString());
		Mark.say(gf.fillGap(GapStory).asString());
	}
}
