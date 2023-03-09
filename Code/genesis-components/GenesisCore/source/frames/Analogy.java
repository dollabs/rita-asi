package frames;

import java.awt.*;
import java.rmi.server.UID;
import java.util.*;
import java.util.List;

import memory.utilities.Distances;
import utils.EntityUtils;
import utils.specialMath.Tableau;
import utils.specialMath.TransportationProblem;
import connections.*;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import gui.*;

public class Analogy extends Frame {
	public static final String	FRAMETYPE			= "analogy";
	public static final String	noMatch				= "NoMatch";
	private Map<Bundle, Entity>	matching			= new HashMap<Bundle, Entity>();
	private List<Entity>			orderedBaseThings	= new ArrayList<Entity>();
	public Entity				base, target;
	public Entity				thing				= new Sequence(Analogy.FRAMETYPE);

	public Analogy(Entity base, Entity target) {
		this.base = base;
		this.target = target;
		fillMatching(this.base, this.target);
	}

	public Analogy(Entity analogy) {
		base = analogy.getElement(0).getSubject();
		target = analogy.getElement(0).getObject();

		fillMatching(base, target);
	}

	private void fillMatching(Entity base, Entity target) {
//		System.out.println("base: "+base.getName() + " target: "+ target.getName());
		if (base.isA(Analogy.noMatch) || target.isA(Analogy.noMatch)) {
			putMatch(base, target);

			return;
		}
//		if (!Analogy.type(base).equals(Analogy.type(target))) {
////			System.out.println("base type: "+Analogy.type(base) + " target type: "+ Analogy.type(target));
//			return;
//		}
		
		if (base.getPrettyPrintType() != target.getPrettyPrintType()) {
//			System.out.println("base type: "+base.getPrettyPrintType() + " target type: "+ target.getPrettyPrintType());
			return;
		}
		
		putMatch(base, target);
		List<Entity> baseKids = EntityUtils.getOrderedChildren(base);
		List<Entity> targetKids = EntityUtils.getOrderedChildren(target);
		int bkSize = baseKids.size();
		int tkSize = targetKids.size();
		// at most one of these two for loops will run, equalizing the size of the kid lists
		for (int i = 0; i < tkSize - bkSize; i++) {
			baseKids.add(new Entity(Analogy.noMatch));
		}
		for (int i = 0; i < bkSize - tkSize; i++) {
			targetKids.add(new Entity(Analogy.noMatch));
		}
		assert baseKids.size() == targetKids.size();
		int size = baseKids.size();
		Tableau t = new Tableau(size, size);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				t.set(i, j, Analogy.distance(baseKids.get(i), targetKids.get(j)));
			}
		}
		t = TransportationProblem.doHungarian(t);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (t.isStarred(i, j)) {
					fillMatching(baseKids.get(i), targetKids.get(j));
				}
			}
		}
	}

//	private static String type(Thing t) {
//		String s = (String) EntityUtils.getRepType(t);
//		if (s.equals("unknown representation type")) {
//			s = String.valueOf(t.getPrettyPrintType());
//		}
//		return s;
//	}

	private static double distance(Entity a, Entity b) {
		return Distances.distance(a, b);
	}

	private void putMatch(Entity base, Entity target) {
		thing.addElement(new Relation(base, target));
		matching.put(base.getBundle(), target);
		orderedBaseThings.add(base);
	}

	public Collection<Entity> getBaseThings() {
		return orderedBaseThings;
	}

	public boolean contains(Entity t) {
		return matching.containsKey(t.getBundle()) && !getMatch(t).isA(Analogy.noMatch);
	}

	public Entity getMatch(Entity t) {
		return matching.get(t.getBundle());
	}

	// Use this analogy to translate a thing to become more targety and less basey
	public Entity targetify(Entity baseyThing) {
		if (contains(baseyThing)) {
			return getMatch(baseyThing).deepClone();
		}
		Entity result = baseyThing.deepClone();
		if (result instanceof Function) {
			Function d = (Function) result;
			d.setSubject(targetify(d.getSubject()));
		}
		if (result instanceof Relation) {
			Relation r = (Relation) result;
			r.setSubject(targetify(r.getSubject()));
			r.setObject(targetify(r.getObject()));
		}
		if (result instanceof Sequence) {
			Sequence s = (Sequence) result;
			Vector<Entity> elements = new Vector<Entity>();
			for (Entity t : s.getElements()) {
				elements.add(targetify(t));
			}
			s.setElements(elements);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entity t : orderedBaseThings) {
			sb.append(t.getName() + " -> " + getMatch(t).getName() + "\n");
		}
		return sb.toString();
	}

	@Override
	public Entity getThing() {
		return thing;
	}

	@SuppressWarnings("serial")
	public class AnalogyViewer extends WiredViewer {
		public AnalogyViewer() {
			Connections.getPorts(this).addSignalProcessor("view");
			setLayout(new GridLayout(0, 2));
			setBackground(Color.WHITE);
			setOpaque(true);
		}

		public void view(Object signal) {
			if (signal instanceof Entity) {
				removeAll();
				Entity thing = (Entity) signal;
				Frame frame = SmartFrameFactory.translate(thing);
				if (!(frame instanceof Analogy)) {
					System.err.println(frame.getClass().getCanonicalName());
				}
				Analogy a = (Analogy) frame;
				for (Entity base : a.getBaseThings()) {
					Entity target = a.getMatch(base);
					WiredPanel baseViewer = new GenericViewer();
					WiredPanel targetViewer = new GenericViewer();
					this.add(baseViewer);
					this.add(targetViewer);
					String bport = new UID().toString();
					String tport = new UID().toString();
					Connections.wire(bport, this, baseViewer);
					Connections.wire(tport, this, targetViewer);
					Connections.getPorts(this).transmit(bport, base);
					Connections.getPorts(this).transmit(tport, target);
				}
				revalidate();
				this.repaint();
			}
		}
	}

	@Override
	public WiredViewer getThingViewer() {
		return new AnalogyViewer();
	}
}
