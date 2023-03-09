package translator;

import genesis.GenesisGetters;

import java.util.*;

import utils.Mark;

import connections.Connections;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Matcher;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Nov 22, 2007 @author phw
 */

public class HardWiredTranslator extends RuleSet {

	public final static String LEFT = "left";

	public final static String RIGHT = "right";

	public HardWiredTranslator() {
		this(null);
	}

	/*
	 * Establishes the initial values of the parse and the transformation list
	 */
	public HardWiredTranslator(GenesisGetters gauntlet) {
		super();
		this.gauntlet = gauntlet;
		Connections.getPorts(this).addSignalProcessor(PARSE, "setInput");
		Connections.getPorts(this).addSignalProcessor(LEFT, "setInputLeft");
		Connections.getPorts(this).addSignalProcessor(RIGHT, "setInputRight");
		Connections.getPorts(this).addSignalProcessor(PROCESS, "setInputAndStep");
		Connections.getPorts(this).addSignalProcessor(STEP, "step");
	}

	public void setInputLeft(Object object) {
		if (!(object instanceof Sequence)) {
			return;
		}
		Entity result = interpret(object);
		// System.out.println("Translator result left: " + result);
		Connections.getPorts(this).transmit(LEFT, result);
	}

	public void setInputRight(Object object) {
		if (!(object instanceof Sequence)) {
			return;
		}
		Entity result = interpret(object);
		// System.out.println("Translator result right: " + result);
		Connections.getPorts(this).transmit(RIGHT, result);
	}

	public Entity interpret(Object o) {
		if (o instanceof Sequence) {
			parse = (Sequence) o;

			setInput(parse);
			while (rachet()) {
			}
			Sequence result = getTransformations().get(getTransformations().size() - 1);
			Vector<Entity> v = result.getElements();
			for (Entity t : v) {
				if (t.relationP()) {
					Relation r = (Relation) t;
					if (r.getSubject().isA("root")) {
						return r.getObject();
					}
				}
			}
		}
		return null;
	}

	public boolean rachet() {
		int size = getTransformations().size();
		transform();
		if (getTransformations().size() == size) {
			return false;
		}
		return true;
	}

	// Stuff below here obsolescent; may be used by Demo for debugging

	private class LocalGoClass extends java.lang.Thread {
		public void run() {
			while (step()) {
				if (gauntlet != null && Switch.stepParser.isSelected()) {
					try {
						sleep(delta);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// private class ShowResult implements Runnable {
	// public void run() {
	// Connections.getPorts(HardWiredTranslator.this).transmit(PROGRESS,
	// getTransformations().get(getTransformations().size() - 1));
	// }
	//
	// }

	private static int delta = 500;

	public static String PARSE = "parse", STEP = "step", PROCESS = "process", RUN = "run";

	public static String PROGRESS = "progress";

	public static void main(String[] ignore) {

		Entity subject = new Entity("bird");
		Entity object = new Entity("fly");
		Relation relation = new Relation(subject, object);

		Sequence parse = new Sequence();
		parse.addElement(relation);

		HardWiredTranslator hardWiredTranslator = new HardWiredTranslator(null);
		hardWiredTranslator.setInput(parse);
		hardWiredTranslator.step(parse);

		System.out.println("Result: ");

		Matcher matcher = new Matcher();

	}

	private GenesisGetters gauntlet;

	// The link parse provided by the parser
	private Sequence parse;

	// A sequence of transformations of the parse.
	private ArrayList<Sequence> transformations;

	private boolean transmittable = false;

	public Sequence getParse() {
		if (parse == null) {
			parse = new Sequence();
		}
		return parse;
	}

	public ArrayList<Sequence> getTransformations() {
		if (transformations == null) {
			transformations = new ArrayList<Sequence>();
		}
		return transformations;
	}

	public void go() {
		new LocalGoClass().start();
	}

	private void removeParts(Entity thing, Vector v) {
		Vector links = (Vector) v.clone();
		for (Iterator<Entity> i = links.iterator(); i.hasNext();) {
			Entity t = i.next();
			if (!t.isA("parse-link")) {
				v.remove(t);
			}
		}
		// System.out.println("Removing " + Matcher.renderThing(thing));
		// System.out.println("A: " + v.size());
		// v.removeElement(thing);
		// System.out.println("B: " + v.size());
		// if (thing.relationP()) {
		// Relation r = (Relation)thing;
		// removeParts(r.getSubject(), v);
		// removeParts(r.getObject(), v);
		// }
		// else if (thing.functionP()) {
		// Derivative d = (Derivative)thing;
		// removeParts(d.getSubject(), v);
		// }
		// else if (thing.sequenceP()) {
		// Sequence s = (Sequence)thing;
		// Vector<Thing> e = s.getElements();
		// for (Iterator<Thing> i = e.iterator(); i.hasNext();) {
		// removeParts(i.next(), v);
		// }
		// }
	}

	public void setInput(Object o) {
		if (o instanceof Sequence) {
			parse = (Sequence) o;
			getTransformations().clear();
			getTransformations().add(parse);
		}
	}

	public void setInputAndStep(Object o) {
		if (o instanceof Sequence) {
			parse = (Sequence) o;
			setInput(parse);
			go();
		}
	}

	public void setParse(Sequence parse) {
		this.parse = parse;
	}

	public void setTransformations(ArrayList<Sequence> transformations) {
		this.transformations = transformations;
	}

	public boolean step() {
		if (rachet()) {
			// SwingUtilities.invokeLater(new ShowResult());
			Connections.getPorts(HardWiredTranslator.this).transmit(PROGRESS, getTransformations().get(getTransformations().size() - 1));
			return true;
		}
		else {
			Sequence result = getTransformations().get(getTransformations().size() - 1);
			Vector v = result.getElements();

			Sequence sequence = new Sequence(Markers.SEMANTIC_INTERPRETATION);
			for (Iterator i = v.iterator(); i.hasNext();) {
				Relation t = (Relation) i.next();
				if (t.getSubject().isA("root")) {
					sequence.addElement(t.getObject());
				}
			}
			Connections.getPorts(this).transmit(sequence);
			return false;
		}
	}

	public boolean step(Object o) {
		return step();
	}

	/*
	 * Attempts to extend the transformation list.
	 */
	public void transform() {
		int lastIndex = getTransformations().size() - 1;
		Sequence sequence = getTransformations().get(lastIndex);
		Sequence result = transform(sequence);
		if (result != null) {
			getTransformations().add(result);
		}
	}

	private boolean transform(BasicRule runnable, Sequence s) {
		runnable.setLinks(s);
		if (s.getElements().size() < 1) {
			return false;
		}
		if (runnable instanceof BasicRule3) {
			// Looks at three links
			for (int i = 0; i < s.getElements().size(); ++i) {
				for (int j = i + 1; j != i && j < s.getElements().size(); ++j) {
					for (int k = j + 1; k != j && k != i && k < s.getElements().size(); ++k) {
						runnable.setLinks(s.getElements().get(i), s.getElements().get(j), s.getElements().get(k));
						runnable.run();
						if (runnable.hasSucceeded()) {
							if (RuleSet.reportSuccess) {
								Mark.say("Rule " + runnable.getClass().getName() + " succeeded");
							}
							return true;
						}
					}
				}
			}
		}
		else if (runnable instanceof BasicRule2) {
			// Looks at two links
			for (int i = 0; i < s.getElements().size(); ++i) {
				for (int j = i + 1; j != i && j < s.getElements().size(); ++j) {
					runnable.setLinks(s.getElements().get(i), s.getElements().get(j));
					runnable.run();
					if (runnable.hasSucceeded()) {
						if (RuleSet.reportSuccess) {
							Mark.say("Rule " + runnable.getClass().getName() + " succeeded");
						}
						return true;
					}
				}
			}
		}
		else if (runnable instanceof BasicRule) {
			// Looks at single link
			for (int i = 0; i < s.getElements().size(); ++i) {
				runnable.setLinks(s.getElements().get(i));
				runnable.run();
				if (runnable.hasSucceeded()) {
					if (RuleSet.reportSuccess) {
						Mark.say("Rule " + runnable.getClass().getName() + " succeeded");
					}
					return true;
				}
			}

		}
		return false;

	}

	/*
	 * Examines a particular transform and attempts to transform it using
	 * recognition rules
	 */
	private Sequence transform(Sequence s) {
		for (Rule rule : getRuleSet()) {
			// New approach using runnables
			BasicRule runnable = rule.getRunnable();
			if (runnable != null) {
				if (transform(runnable, s)) {
					// System.out.println("Runnable " + rule.getName() + "
					// executed");
					return s;
				}
			}
		}
		// System.out.println("Match NOT found");
		return null;
	}

}
