package translator;

import java.util.*;

import utils.Mark;
import utils.Z;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/*
 * Created on Jan 23, 2008 @author phw
 */

public class BasicRule implements Runnable {

	protected Sequence links;

	boolean succeeded = false;

	protected Entity firstLink;

	protected Entity firstLinkSubject;

	protected Entity firstLinkObject;

	protected Entity secondLink;

	protected Entity secondLinkSubject;

	protected Entity secondLinkObject;

	protected Entity thirdLink;

	protected Entity thirdLinkSubject;

	protected Entity thirdLinkObject;

	protected Entity dummy = new Entity();

	protected String sample = "";

	public String getSample() {
		return sample;
	}

	public Relation getFirstLink() {
		return (Relation) firstLink;
	}

	public Relation getSecondLink() {
		return (Relation) secondLink;
	}

	public Relation getThirdLink() {
		return (Relation) thirdLink;
	}

	public void setLinks(Entity firstLink) {
		this.firstLink = firstLink;
		if (firstLink != null && firstLink.relationP()) {
			firstLinkSubject = firstLink.getSubject();
			firstLinkObject = firstLink.getObject();
		}
		else if (firstLink != null && firstLink.functionP()) {
			firstLinkSubject = firstLink.getSubject();
			firstLinkObject = dummy;
		}
		else {
			firstLinkSubject = dummy;
			firstLinkObject = dummy;
		}
	}

	public void setLinks(Entity firstLink, Entity secondLink) {
		setLinks(firstLink);
		this.secondLink = secondLink;
		if (secondLink != null && secondLink.relationP()) {
			secondLinkSubject = secondLink.getSubject();
			secondLinkObject = secondLink.getObject();
		}
		else if (secondLink != null && secondLink.functionP()) {
			secondLinkSubject = secondLink.getSubject();
			secondLinkObject = dummy;
		}
		else {
			secondLinkSubject = dummy;
			secondLinkObject = dummy;
		}
	}

	public void setLinks(Entity firstLink, Entity secondLink, Entity thirdLink) {
		setLinks(firstLink, secondLink);
		this.thirdLink = thirdLink;
		if (thirdLink != null && thirdLink.relationP()) {
			thirdLinkSubject = thirdLink.getSubject();
			thirdLinkObject = thirdLink.getObject();
		}
		else if (thirdLink != null && thirdLink.functionP()) {
			thirdLinkSubject = thirdLink.getSubject();
			thirdLinkObject = dummy;
		}
		else {
			thirdLinkSubject = dummy;
			thirdLinkObject = dummy;
		}
	}

	public void setLinks(Sequence links) {
		this.links = links;
	}

	public Vector<Entity> getLinkElements() {
		return links.getElements();
	}

	public void addLink(Entity link) {
		getLinkElements().add(0, link);
	}

	public boolean addLinkAfter(Entity link, Entity after) {
		int index = getLinkElements().indexOf(after);
		if (index < 0) {
			return false;
		}
		getLinkElements().add(index + 1, link);
		return true;
	}

	public boolean addLinkBefore(Entity link, Entity before) {
		int index = getLinkElements().indexOf(before);
		if (index < 0) {
			return false;
		}
		getLinkElements().add(index, link);
		return true;
	}

	public void addLinkAtEnd(Entity link) {
		getLinkElements().add(link);
	}

	public void remove(Entity link) {
		Mark.green(Z.START_DEBUG, "   Removing link ", link);
		getLinkElements().remove(link);
	}

	protected void replaceEverywhere(Entity target) {
		Entity replacement = new Entity();
		target.transferThreadsFeaturesAndProperties(replacement);
		Mark.say("Replacing", target, "with", replacement);
		replaceEverywhere(replacement, target, links);
	}

	private void replaceEverywhere(Entity replacement, Entity target, Entity structure) {
		if (target == structure) {
			Mark.err("Bug--should not ever happen");
		}
		if (structure.entityP()) {
			// Do nothing, recursion complete
		}
		else if (structure.functionP()) {
			if (target == structure.getSubject()) {
				structure.setSubject(replacement);
			}
			else {
				replaceEverywhere(replacement, target, structure.getSubject());
			}
		}
		else if (structure.relationP()) {
			if (target == structure.getSubject()) {
				structure.setSubject(replacement);
			}
			else {
				replaceEverywhere(replacement, target, structure.getSubject());
			}
			if (target == structure.getObject()) {
				structure.setObject(replacement);
			}
			else {
				replaceEverywhere(replacement, target, structure.getObject());
			}
		}
		else {
			// Must be sequence
			Vector<Entity> elements = structure.getElements();
			for (int i = 0; i < elements.size(); ++i) {
				Entity x = elements.get(i);
				if (x == target) {
					elements.set(i, replacement);
				}
				else {
					replaceEverywhere(replacement, target, x);
				}
			}
		}
	}

	public void replace(Entity target, Entity replacement) {
		replace(replacement, target, links);
	}

	// public void replaceUsingName(Thing target, Thing replacement) {
	// replaceUsingName(replacement, target, links);
	// }

	public static void replace(Entity in, Entity out, Entity structure) {
		// System.out.println("\n\n\nReplacing \n" + out + "\n\nwith\n\n" + in +
		// "\n\nin \n\n" + structure);
		if (structure.entityP()) {
			return;
		}
		if (structure.relationP()) {
			Relation r = (Relation) structure;
			if (r.getSubject() == out) {
				r.setSubject(in);
			}
			else {
				replace(in, out, structure.getSubject());
			}
			if (r.getObject() == out) {
				r.setObject(in);
			}
			else {
				replace(in, out, structure.getObject());
			}
		}
		else if (structure.functionP()) {
			Function d = (Function) structure;
			if (d.getSubject() == out) {
				d.setSubject(in);
			}
			else {
				replace(in, out, structure.getSubject());
			}
		}
		else if (structure.sequenceP()) {
			Sequence s = (Sequence) structure;
			Vector<Entity> elements = s.getElements();
			Vector<Entity> clone = (Vector<Entity>) (elements.clone());
			for (int i = 0; i < clone.size(); ++i) {
				if (out == clone.get(i)) {
					elements.remove(i);
					elements.add(i, in);
				}
				else {
					replace(in, out, (elements.get(i)));
				}

			}
		}
	}

	// private static boolean sameName(Thing A, Thing B) {
	// if (A.getType().equals(B.getType())) {
	// return true;
	// }
	// return false;
	// }

	// public static void replaceUsingName(Thing in, Thing out, Thing structure) {
	// // Mark.say("Replacing " + out.getName() + " with " + in.getName(), "in", structure.asStringWithNames());
	// if (structure.entityP()) {
	// return;
	// }
	// if (structure.relationP()) {
	// Relation r = (Relation) structure;
	// if (sameName(r.getSubject(), out)) {
	// r.setSubject(in);
	// }
	// else {
	// replaceUsingName(in, out, structure.getSubject());
	// }
	// if (sameName(r.getObject(), out)) {
	// r.setObject(in);
	// }
	// else {
	// replaceUsingName(in, out, structure.getObject());
	// }
	// }
	// else if (structure.functionP()) {
	// Derivative d = (Derivative) structure;
	// if (sameName(d.getSubject(), out)) {
	// d.setSubject(in);
	// }
	// else {
	// replaceUsingName(in, out, structure.getSubject());
	// }
	// }
	// else if (structure.sequenceP()) {
	// Sequence s = (Sequence) structure;
	// Vector<Thing> elements = s.getElements();
	// Vector<Thing> clone = (Vector<Thing>) (elements.clone());
	// for (int i = 0; i < clone.size(); ++i) {
	// if (sameName(out, clone.get(i))) {
	// elements.remove(i);
	// elements.add(i, in);
	// }
	// else {
	// replaceUsingName(in, out, (elements.get(i)));
	// }
	//
	// }
	// }
	// }

	public void copyAndReplace(Entity target, Entity replacement) {
		ListIterator<Entity> iter = links.getElements().listIterator();
		while (iter.hasNext()) {
			Entity e = iter.next();
			if (e.entityP()) {
				continue;
			}

			if (contains(e, target)) {
				Entity copy = copyAndReplace(replacement, target, e);
				if (copy != null) {
					iter.add(copy);
				}
			}
		}
	}

	public Entity copyAndReplace(Entity in, Entity out, Entity structure) {
		if (structure == out) {
			return in;
		}
		else if (structure.relationP()) {
			Entity s = copyAndReplace(in, out, structure.getSubject());
			Entity o = copyAndReplace(in, out, structure.getObject());
			if (s != null || o != null) {
				Relation ret = (Relation) structure.clone();

				if (s != null) {
					ret.setSubject(s);
				}
				if (o != null) {
					ret.setObject(o);
				}

				return ret;
			}
		}
		else if (structure.functionP()) {
			Entity s = copyAndReplace(in, out, structure.getSubject());
			if (s != null) {
				Function ret = (Function) structure.clone();
				ret.setSubject(s);
				return ret;
			}
		}
		else if (structure.sequenceP()) {
			Sequence s = (Sequence) structure;
			Vector<Entity> newElements = new Vector<Entity>(s.getElements());
			boolean copy = false;
			for (int i = 0; i < s.getElements().size(); i++) {
				Entity result = copyAndReplace(in, out, s.getElement(i));
				if (result != null) {
					copy = true;
					newElements.set(i, result);
				}
			}
			if (copy) {
				Sequence ret = (Sequence) s.clone();
				ret.setElements(newElements);
				return ret;
			}
		}
		return null;
	}

	public boolean containsLink(Entity thing) {
		return this.links.getElements().contains(thing);
	}

	public static boolean contains(Entity structure, Entity target) {
		if (structure.entityP()) {
			return structure == target;
		}
		else if (structure.relationP()) {
			return structure == target || contains(structure.getSubject(), target) || contains(structure.getObject(), target);
		}
		else if (structure.functionP()) {
			return structure == target || contains(structure.getSubject(), target);
		}
		else if (structure.sequenceP()) {
			Sequence s = (Sequence) structure;
			if (structure == target) {
				return true;
			}
			for (Entity e : s.getElements()) {
				if (contains(e, target)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	public static void transferTypes(Entity source, Entity target) {
		target.getPrimedThread().clear();
		target.getPrimedThread().addAll(source.getPrimedThread());
		transferFeatures(source, target);
	}

	public static void transferFeatures(Entity source, Entity target) {
		if (source.getBundle().getThread("feature") == null) {
			return;
		}
		for (String type : source.getThread("feature")) {
			target.addType(type, "feature");
		}
	}

	public static void addTypeAfterReference(String reference, String addition, Entity t) {
		Thread v = t.getPrimedThread().copyThread();
		v.remove(addition);
		int index = v.indexOf(reference);
		if (index >= 0) {
			v.add(index + 1, addition);
		}
		t.setPrimedThread(v);
	}

	public static void addTypeBeforeLast(String addition, Entity t) {
		Thread v = t.getPrimedThread().copyThread();
		v.remove(addition);
		int index = v.size();
		if (index >= 0) {
			v.add(index - 1, addition);
		}
		t.setPrimedThread(v);
	}

	public static void addTypeAfterLast(String addition, Entity t) {
		Thread v = t.getPrimedThread().copyThread();
		v.remove(addition);
		v.add(addition);
		t.setPrimedThread(v);
	}

	// public static void addName(Entity t) {
	// // Mark.say("Adding name to", t.asString());
	// // Mark.say("Original", t.getPrimedThread());
	// Thread v = t.getPrimedThread().copyThread();
	// // Mark.say("Copy", v);
	// String theName = v.lastElement();
	// int index = v.size() - 1;
	//
	// if (index >= 1) {
	// if (!v.contains(Markers.NAME)) {
	// v.add(index, Markers.NAME);
	// // v.add(Markers.NAME);
	// // v.add(theName);
	// }
	// else {
	// // Use boolean please
	// // Mark.say(t.asString(), "already has an identified thread", v);
	// }
	//
	// }
	// t.addProperty(Markers.NAME, v.getType());
	// t.setPrimedThread(v);
	// // Mark.say("Exiting addName with", t.getPrimedThread());
	// }

	public static void addName(Entity t) {
		// Mark.say("Adding name to", t.asString());
		// Mark.say("Original", t.getPrimedThread());
		Thread v = t.getPrimedThread().copyThread();
		// Mark.say("Copy", v);
		String theName = v.lastElement();
		if (!v.contains(Markers.NAME)) {
			v.add(Markers.NAME);
			v.add(theName);
			// v.add(Markers.NAME);
			// v.add(theName);
		}

		t.addProperty(Markers.NAME, v.getType());
		t.setPrimedThread(v);
		// Mark.say("Exiting addName with", t.getPrimedThread());
	}

	public static void addProperName(Entity t) {
		// Mark.say("Adding proper name to", t.asString());
		// Mark.say("Original", t.getPrimedThread());
		// Thread v = t.getPrimedThread().copyThread();
		// Mark.say("Copy", v);
		// int index = v.size() - 1;
		// if (index >= 1) {
		// if (!v.contains(Markers.NAME)) {
		// // v.add(index, Markers.PROPER);
		// v.add(index, Markers.NAME);
		// }
		// }
		// t.setPrimedThread(v);
		t.addProperty(Markers.PROPER, t.getType());
	}

	public static void removeName(Entity t) {
		// Mark.say("Removing name to", t.asString());
		Thread v = t.getPrimedThread().copyThread();
		v.remove(Markers.NAME);
		t.setPrimedThread(v);
	}

	// protected void transferFeatures(Thing source, Thing destination) {
	// Thread features1 = source.getThread("feature");
	// Thread features2 = source.getThread("features");
	// Object object2 = map.get(destination);
	// if (object2 != null) {
	// destination = (Thing) object2;
	// }
	// if (features1 != null) {
	// String feature = features1.lastElement();
	// destination.addType(feature, "features");
	// }
	// if (features2 != null) {
	// for (Iterator<String> f = features2.iterator(); f.hasNext();) {
	// destination.addType(f.next(), "features");
	// }
	// }
	// }

	public void succeeded() {
		succeeded = true;
		// Mark.say("Setting succeeded to", succeeded);
	}

	public void failed() {
		succeeded = false;
		// Mark.say("Setting succeeded to", succeeded);
	}

	public boolean hasSucceeded() {
		return succeeded;
	}

	protected boolean firstInsideSecond(Entity first, Entity second) {
		if (first == second) {
			return true;
		}
		else if (second.relationP()) {
			return firstInsideSecond(first, second.getSubject()) || firstInsideSecond(first, second.getObject());
		}
		else if (second.functionP()) {
			return firstInsideSecond(first, second.getSubject());
		}
		else if (second.sequenceP()) {
			for (Iterator i = second.getElements().iterator(); i.hasNext();) {
				Entity t = (Entity) (i.next());
				if (firstInsideSecond(first, t)) {
					return true;
				}
			}
		}
		return false;
	}

	public void run() {
		failed();
	}

}
