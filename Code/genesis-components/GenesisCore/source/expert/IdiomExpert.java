package expert;

import java.util.ArrayList;

import utils.Punctuator;
import utils.tools.Predicates;
import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/*
 * Created on Sep 20, 2009
 * @author phw
 */

public class IdiomExpert extends AbstractWiredBox {

	private boolean debug = false;

	// public static String MEMORY = "memory";

	// public static String CLEAR = "clear";

	public static String STORY_PROCESSOR_PORT = "story";

	public static String START = "start";

	public static String DESCRIBE = "describe";

	// public static String PAUSE = "pause";

	public static String STOP = "stop";

	// public static String START_PARSER_PART = "parser";

	public IdiomExpert() {
		super("Idiom expert");
		Connections.getPorts(this).addSignalProcessor(this::processIdioms);
	}

	// Picks off idioms. Sends everything downstream. Note that some idioms are
	// handled inside Start and never get here.
	public void processIdioms(Object x) {
		if (!(x instanceof Sequence)) {
			return;
		}
		Sequence s = (Sequence) x;

		Mark.say(debug, "Idiom handler sees", s.asStringWithIndexes());
		for (Entity element : s.getElements()) {
			if (element.relationP()) {
				Relation relation = (Relation) element;
				if (threadRestriction(relation)) {
					Entity restriction = relation.getSubject();
					restrictMeaning(restriction);
					transmit(restriction);
					continue;
				}
				else {
					transmit(element);
					continue;
				}
			}
			else if (element.functionP()) {
				Function d = (Function) element;
				if (d.isAPrimed(Markers.DESCRIBE_MARKER) && d.getSubject().isAPrimed("perspective")) {
					continue;
				}
				else if (d.isAPrimed(Markers.DESCRIBE_MARKER)) {
					Entity subject = d.getSubject();
					Connections.getPorts(this).transmit(DESCRIBE, d.getSubject().getType());
					continue;
				}
				else if (threadRestriction(d)) {
					restrictMeaning(d);
					transmit(d);
					continue;
				}
				else {
					transmit(element);
					continue;
				}
			}
			else {
				transmit(element);
				continue;
			}
		}
		// Mark.say("Idiom expert transmitting", s.asString());
		// Connections.getPorts(this).transmit(s);

	}

	private void restrictMeaning(Entity t) {
		Function derivative = (Function) t;
		Relation relation = (Relation) (derivative.getSubject());
		String type = relation.getSubject().getType();
		BundleGenerator.getInstance().getBundleMap().put(type, null);
		Bundle bundle = BundleGenerator.getInstance().getRawBundle(type);
		Mark.say("Raw bundle is", bundle);
		String restriction = relation.getObject().getType();
		restriction = Punctuator.removeQuotes(restriction);
		relation.getObject().setName(restriction);
		ArrayList<Thread> winners = new ArrayList<Thread>();
		for (Thread thread : bundle) {
			if (thread.contains(restriction)) {
				winners.add(thread);
			}
		}
		int size = winners.size();
		if (size == 0) {
			Mark.say("Ugh, you asked to restrict", relation.getSubject().getType(), "to", restriction, "but there is no thread with the restriction");
			Mark.say("Setting", type, "to", bundle);
			BundleGenerator.getInstance().getBundleMap().put(type, bundle);
			return;
		}
		else if (size > 1) {
			Mark.say("Ugh, you asked to restrict", relation.getSubject().getType(), "to", restriction, "but there is more than one thread with the restriction");
			Mark.say("I assume a", type, "is a", winners.get(0));
		}
		else {
			Mark.say("Ok, the winning thread is", winners.get(0));
		}
		Bundle newBundle = new Bundle();
		newBundle.add(winners.get(0));
		Mark.say("Setting", type, "to", newBundle);
		BundleGenerator.getInstance().getBundleMap().put(type, newBundle);
		// Mark.say("New bundle is", BundleGenerator.getBundle(type));

	}

	private boolean threadRestriction(Entity relation) {
		if (relation.relationP("has_attitude")) {
			if (relation.getObject().entityP(Markers.IMPERATIVE) && relation.getSubject().functionP(Markers.ASSUME)) {
				if (relation.getSubject().getSubject().relationP(Markers.MEANING_RESTRICTION)) {
					Mark.say("Using new start now.  Fix me");
					return true;
				}
			}
		}
		if (relation.functionP(Markers.ASSUME)) {
			if (relation.getSubject().relationP(Markers.MEANING_RESTRICTION)) {
				return true;
			}
		}
		return false;
	}

	private void transmit(Entity element) {
		Mark.say(debug, "Passing through", element.asStringWithIndexes());
		Connections.getPorts(this).transmit(element);
	}
}
